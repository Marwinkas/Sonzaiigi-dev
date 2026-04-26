using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SonzaiigiApi.Data;
using SonzaiigiApi.Models;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp.Formats.Webp;
using Amazon.S3;
using Amazon.S3.Model;
using System.Security.Claims;
using StackExchange.Redis;
using System.Text.Json;
using TagLib;

namespace SonzaiigiApi.Controllers;

[ApiController]
[Route("api")]
[Authorize]
public class MessagesController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly AmazonS3Client _s3Client;
    private readonly IConnectionMultiplexer _redis;

    public MessagesController(AppDbContext context, IConnectionMultiplexer redis)
    {
        _context = context;
        _redis = redis;
        var s3Config = new AmazonS3Config { ServiceURL = "https://f7601d9aad1781510d16d7d8b23115c1.r2.cloudflarestorage.com", ForcePathStyle = true, AuthenticationRegion = "us-east-1" };
        _s3Client = new AmazonS3Client("5d5fbed17394421e895304f94c018e12", "f5d29455dd07539e769356f4596b89f804fd5a50ca250272bc1a3208fbada811", s3Config);
    }

    private bool HasPermission(ConversationMember member, string perm)
    {
        if (member.IsOwner) return true;
        if (member.IndividualOverrides.TryGetValue(perm, out bool over)) return over;
        bool isDefaultAllowed = perm == "sendMessages" || perm == "canForward" || perm == "addReactions" || perm == "attachFiles";
        if (!member.MemberRoles.Any()) return isDefaultAllowed;
        return member.MemberRoles.Any(mr => mr.Role.Permissions.TryGetValue(perm, out bool p) && p);
    }
    [HttpGet("messages/{conversationId}")]
    public async Task<IActionResult> GetMessages(int conversationId, [FromQuery] int offset = 0, [FromQuery] int limit = 30)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(myId);

        var group = await _context.Conversations.Include(c => c.BannedUsers).Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == conversationId);
        if (group == null) return NotFound();
        if (group.BannedUsers.Any(b => b.UserId == myId) || !group.Members.Any(m => m.UserId == myId)) return Forbid("Вы не состоите в этом чате или забанены.");

        var messages = await _context.Messages.Where(m => m.ConversationId == conversationId && !m.DeletedMessages.Any(dm => dm.UserId == myId))
            .OrderByDescending(m => m.CreatedAt).Skip(offset).Take(limit + 1)
            .Include(m => m.User)
            .Include(m => m.Parent).ThenInclude(p => p.User)
            .Include(m => m.Reactions)
            .Include(m => m.Attachments) // ✨ Подгружаем массив вложений
            .ToListAsync();

        bool hasMore = messages.Count > limit;
        if (hasMore) messages.RemoveAt(messages.Count - 1);
        messages.Reverse();

        var myMember = group.Members.First(m => m.UserId == myId);
        int myReadId = myMember.LastReadMessageId;
        int maxOtherReadId = group.Members.Where(m => m.UserId != myId).Max(m => (int?)m.LastReadMessageId) ?? 0;

        // ✨ ПЕРЕДАЕМ ИХ В FormatMessage
        return Ok(new
        {
            messages = messages.Select(m => FormatMessage(m, myId, me?.Name, myReadId, maxOtherReadId)),
            has_more = hasMore,
            can_reply = !group.IsGroup || HasPermission(myMember, "sendMessages")
        });
    }
    [HttpPost("messages/{conversationId}/upload-chunk")]
    public async Task<IActionResult> UploadChunk(int conversationId, [FromForm] IFormFile chunk, [FromForm] int chunkIndex, [FromForm] string uploadId)
    {
        var tempPath = Path.Combine(Path.GetTempPath(), $"upload_{uploadId}");
        using (var stream = new FileStream(tempPath, chunkIndex == 0 ? FileMode.Create : FileMode.Append))
        {
            await chunk.CopyToAsync(stream);
        }
        return Ok();
    }

    [HttpPost("messages/{conversationId}")]
    public async Task<IActionResult> SendMessage(int conversationId, [FromForm] string? text, [FromForm] int? parent_id, [FromForm] string? gif_url, [FromForm] string? poll_json, [FromForm] List<IFormFile>? poll_images,
    [FromForm] List<string>? uploaded_file_ids, [FromForm] List<string>? original_file_names, [FromForm] List<string>? content_types)
    {
        var userId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(userId);

        var group = await _context.Conversations
            .Include(c => c.Members).ThenInclude(m => m.User)
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .FirstOrDefaultAsync(c => c.Id == conversationId);

        if (group == null) return NotFound();
        var myMember = group.Members.FirstOrDefault(m => m.UserId == userId);
        if (myMember == null) return Forbid();

        bool hasFiles = uploaded_file_ids != null && uploaded_file_ids.Any();

        if (group.IsGroup && !HasPermission(myMember, "sendMessages")) return StatusCode(403, new { message = "Вам запрещено отправлять сообщения" });
        if (group.IsGroup && (hasFiles || gif_url != null) && !HasPermission(myMember, "attachFiles")) return StatusCode(403, new { message = "Вам запрещено прикреплять медиафайлы" });

        if (group.IsGroup && group.SlowMode > 0 && !HasPermission(myMember, "bypassSlowMode"))
        {
            var lastMessage = await _context.Messages.Where(m => m.ConversationId == conversationId && m.UserId == userId).OrderByDescending(m => m.CreatedAt).FirstOrDefaultAsync();
            if (lastMessage != null)
            {
                var secondsPassed = (DateTime.UtcNow - lastMessage.CreatedAt).TotalSeconds;
                if (secondsPassed < group.SlowMode)
                {
                    var waitTime = Math.Ceiling(group.SlowMode - secondsPassed);
                    return StatusCode(429, new { message = $"Работает медленный режим. Подожди еще {waitTime} сек." });
                }
            }
        }

        var message = new Message { ConversationId = conversationId, UserId = userId, Body = text ?? "", ParentId = parent_id, GifUrl = gif_url, CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };

        // === ОБРАБОТКА ОПРОСОВ ===
        if (!string.IsNullOrEmpty(poll_json))
        {
            var poll = JsonSerializer.Deserialize<PollData>(poll_json, new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
            if (poll != null)
            {
                if (poll_images != null && poll_images.Count > 0)
                {
                    var uploadTasks = new List<Task>();
                    foreach (var opt in poll.Options)
                    {
                        if (opt.ImageIndex.HasValue && opt.ImageIndex.Value >= 0 && opt.ImageIndex.Value < poll_images.Count)
                        {
                            var pFile = poll_images[opt.ImageIndex.Value];
                            if (pFile.ContentType.StartsWith("image/"))
                            {
                                using var img = await Image.LoadAsync(pFile.OpenReadStream());
                                var baseGuid = Guid.NewGuid().ToString("N");
                                using var view = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(1200, 1200), Mode = ResizeMode.Max }));
                                var msView = new MemoryStream();
                                await view.SaveAsWebpAsync(msView, new WebpEncoder { Quality = 82 });
                                msView.Position = 0;
                                var viewFileName = $"dev/chat_{conversationId}/{baseGuid}_poll_view.webp";
                                opt.ImageUrl = viewFileName;
                                opt.ImageViewUrl = viewFileName;
                                uploadTasks.Add(_s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = viewFileName, InputStream = msView, ContentType = "image/webp", DisablePayloadSigning = true }).ContinueWith(_ => msView.Dispose()));
                            }
                        }
                    }
                    if (uploadTasks.Any()) await Task.WhenAll(uploadTasks);
                }
                message.PollJson = JsonSerializer.Serialize(poll);
            }
        }

        // === ОБРАБОТКА МАССИВА ФАЙЛОВ ===
        if (hasFiles)
        {
            for (int i = 0; i < uploaded_file_ids!.Count; i++)
            {
                var uploaded_file_id = uploaded_file_ids[i];
                var original_file_name = original_file_names != null && original_file_names.Count > i ? original_file_names[i] : "file";
                var content_type = content_types != null && content_types.Count > i ? content_types[i] : "application/octet-stream";

                var rawTempPath = Path.Combine(Path.GetTempPath(), $"upload_{uploaded_file_id}");
                if (!System.IO.File.Exists(rawTempPath)) continue;

                var extension = Path.GetExtension(original_file_name).ToLower();
                var tempOriginalPath = rawTempPath + extension;

                if (System.IO.File.Exists(tempOriginalPath)) System.IO.File.Delete(tempOriginalPath);
                System.IO.File.Move(rawTempPath, tempOriginalPath);

                var baseFileName = $"dev/chat_{conversationId}/{Guid.NewGuid():N}";

                // ✨ УМНОЕ ОПРЕДЕЛЕНИЕ ТИПОВ ФАЙЛОВ И АНИМАЦИИ ✨
                var audioExtensions = new[] { ".mp3", ".ogg", ".wav", ".flac", ".m4a", ".aac", ".amr", ".opus" };
                bool isAudio = content_type?.StartsWith("audio/") == true || audioExtensions.Contains(extension);
                bool isAnimated = false;

                if (!isAudio)
                {
                    if (extension == ".gif" || content_type == "image/gif")
                    {
                        isAnimated = true;
                    }
                    else if (extension == ".webp" || content_type == "image/webp")
                    {
                        try
                        {
                            // Используем LoadAsync, так как именно он дает доступ к коллекции кадров (Frames)
                            using var img = await Image.LoadAsync(tempOriginalPath);
                            if (img.Frames.Count > 1) isAnimated = true;
                        }
                        catch { /* Если не удалось прочитать, оставляем как обычную картинку */ }
                    }
                }

                var isVideo = !isAudio && !isAnimated && content_type?.StartsWith("video/") == true;
                var isImage = !isAudio && !isAnimated && !isVideo && content_type?.StartsWith("image/") == true;

                var attachment = new MessageAttachment { Name = original_file_name };

                try
                {
                    // ✨ КОНВЕРТАЦИЯ GIF И ANIMATED WEBP В WEBM ✨
                    if (isAnimated)
                    {
                        var tempWebmPath = Path.Combine(Path.GetTempPath(), $"{Guid.NewGuid()}.webm");

                        try
                        {
                            // 1. Быстро достаем первый кадр в качестве превьюшки (через ImageSharp)
                            using var img = await Image.LoadAsync(tempOriginalPath);
                            using var view = img.Frames.CloneFrame(0).Clone(x => x.Resize(new ResizeOptions { Size = new Size(1200, 1200), Mode = ResizeMode.Max }));
                            attachment.Width = view.Width;
                            attachment.Height = view.Height;

                            using var msThumb = new MemoryStream();
                            await view.SaveAsWebpAsync(msThumb, new WebpEncoder { Quality = 80 });
                            msThumb.Position = 0;
                            var thumbKey = $"{baseFileName}_vthumb.webp";
                            await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = thumbKey, InputStream = msThumb, ContentType = "image/webp", DisablePayloadSigning = true });

                            // 2. Конвертируем сам GIF/WebP в WebM
                            // Используем кодек VP9 (стандарт для WebM). Параметры прозрачности сохраняются.
                            var ffmpegArgs = $"-i \"{tempOriginalPath}\" -c:v libvpx-vp9 -b:v 0 -crf 30 -an -y \"{tempWebmPath}\"";
                            using (var process = new System.Diagnostics.Process { StartInfo = new System.Diagnostics.ProcessStartInfo { FileName = "ffmpeg", Arguments = ffmpegArgs, UseShellExecute = false, CreateNoWindow = true } })
                            {
                                process.Start();
                                await process.WaitForExitAsync();
                            }

                            // Если конвертация сбойнёт, страхуем заливкой исходника
                            var finalPathToUpload = System.IO.File.Exists(tempWebmPath) ? tempWebmPath : tempOriginalPath;
                            var finalExt = System.IO.File.Exists(tempWebmPath) ? ".webm" : extension;
                            var finalMime = System.IO.File.Exists(tempWebmPath) ? "video/webm" : content_type;

                            var key = $"{baseFileName}_master{finalExt}";
                            using var stream = new FileStream(finalPathToUpload, FileMode.Open);
                            await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = key, InputStream = stream, ContentType = finalMime, DisablePayloadSigning = true });

                            attachment.Type = AttachmentType.Gif; // Оставляем тип Gif (или Video), чтобы фронтенд знал, что нужно зацикливать без звука
                            attachment.Url = key;
                            attachment.ThumbnailUrl = thumbKey;

                            if (i == 0) message.Image = $"{baseFileName}|{finalExt}";
                        }
                        finally
                        {
                            if (System.IO.File.Exists(tempWebmPath)) System.IO.File.Delete(tempWebmPath);
                        }
                    }
                    else if (isImage) // Сюда теперь дойдут только статичные PNG, JPEG, WEBP и др.
                    {
                        using var img = await Image.LoadAsync(tempOriginalPath);

                        using var view = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(1200, 1200), Mode = ResizeMode.Max }));

                        attachment.Width = view.Width;
                        attachment.Height = view.Height;
                        using var msView = new MemoryStream();
                        await view.SaveAsWebpAsync(msView, new WebpEncoder { Quality = 82 });
                        msView.Position = 0;
                        var thumbKey = $"{baseFileName}_view.webp";
                        await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = thumbKey, InputStream = msView, ContentType = "image/webp", DisablePayloadSigning = true });

                        using var master = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(3840, 3840), Mode = ResizeMode.Max }));
                        using var msMaster = new MemoryStream();
                        string masterExt = original_file_name.EndsWith(".png", StringComparison.OrdinalIgnoreCase) ? ".png" : ".jpg";
                        if (masterExt == ".png") await master.SaveAsPngAsync(msMaster); else await master.SaveAsJpegAsync(msMaster, new SixLabors.ImageSharp.Formats.Jpeg.JpegEncoder { Quality = 90 });
                        msMaster.Position = 0;
                        var masterKey = $"{baseFileName}_master{masterExt}";
                        await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = masterKey, InputStream = msMaster, ContentType = masterExt == ".png" ? "image/png" : "image/jpeg", DisablePayloadSigning = true });

                        attachment.Type = AttachmentType.Image;
                        attachment.Url = masterKey;
                        attachment.ThumbnailUrl = thumbKey;

                        if (i == 0) message.Image = $"{baseFileName}|{masterExt}";
                    }
                    else if (isVideo)
                    {
                        string durationStr = "00:00";
                        string thumbKey = $"{baseFileName}_vthumb.webp";

                        try
                        {
                            var probeArgs = $"-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"{tempOriginalPath}\"";
                            using var probeProcess = new System.Diagnostics.Process { StartInfo = new System.Diagnostics.ProcessStartInfo { FileName = "ffprobe", Arguments = probeArgs, UseShellExecute = false, RedirectStandardOutput = true, CreateNoWindow = true } };
                            probeProcess.Start();
                            string output = await probeProcess.StandardOutput.ReadToEndAsync();
                            if (double.TryParse(output.Trim(), System.Globalization.CultureInfo.InvariantCulture, out double seconds))
                            {
                                durationStr = $"{(int)seconds / 60:D2}:{(int)seconds % 60:D2}";
                            }
                        }
                        catch { }

                        var frameTempPath = Path.Combine(Path.GetTempPath(), $"{Guid.NewGuid()}.jpg");
                        try
                        {
                            var ffmpegArgs = $"-i \"{tempOriginalPath}\" -ss 00:00:00.500 -vframes 1 -q:v 2 \"{frameTempPath}\"";
                            using (var proc = new System.Diagnostics.Process { StartInfo = new System.Diagnostics.ProcessStartInfo { FileName = "ffmpeg", Arguments = ffmpegArgs, UseShellExecute = false, CreateNoWindow = true } })
                            {
                                proc.Start();
                                await proc.WaitForExitAsync();
                            }

                            if (System.IO.File.Exists(frameTempPath))
                            {
                                using var img = await Image.LoadAsync(frameTempPath);
                                using var view = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(1280, 1280), Mode = ResizeMode.Max }));

                                attachment.Width = view.Width;
                                attachment.Height = view.Height;
                                using var msThumb = new MemoryStream();
                                await view.SaveAsWebpAsync(msThumb, new WebpEncoder { Quality = 80 });
                                msThumb.Position = 0;
                                await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = thumbKey, InputStream = msThumb, ContentType = "image/webp", DisablePayloadSigning = true });
                            }
                        }
                        finally { if (System.IO.File.Exists(frameTempPath)) System.IO.File.Delete(frameTempPath); }

                        var key = $"{baseFileName}_video{extension}";
                        using var stream = new FileStream(tempOriginalPath, FileMode.Open);
                        await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = key, InputStream = stream, ContentType = content_type, DisablePayloadSigning = true });

                        double sizeMb = new FileInfo(tempOriginalPath).Length / 1048576.0;
                        attachment.Type = AttachmentType.Video;
                        attachment.Url = key;
                        attachment.ThumbnailUrl = thumbKey;
                        attachment.Duration = durationStr;
                        attachment.Size = sizeMb < 0.1 ? $"{new FileInfo(tempOriginalPath).Length / 1024.0:F1} KB" : $"{sizeMb:F1} MB";

                        if (i == 0) { message.VideoHls = key; message.DocumentSize = attachment.Size; }
                    }
                    else if (isAudio)
                    {
                        var tempAacPath = Path.Combine(Path.GetTempPath(), $"{Guid.NewGuid()}.m4a");
                        string trackTitle = Path.GetFileNameWithoutExtension(original_file_name);
                        string trackArtist = me?.Name ?? "Пользователь";
                        int durationSecs = 0;
                        string? coverFileName = null;

                        try
                        {
                            using (var tfile = TagLib.File.Create(tempOriginalPath))
                            {
                                if (!string.IsNullOrEmpty(tfile.Tag.Title)) trackTitle = tfile.Tag.Title;
                                if (!string.IsNullOrEmpty(tfile.Tag.FirstPerformer)) trackArtist = tfile.Tag.FirstPerformer;
                                durationSecs = (int)tfile.Properties.Duration.TotalSeconds;

                                if (tfile.Tag.Pictures.Length > 0)
                                {
                                    var pic = tfile.Tag.Pictures[0];
                                    using var img = Image.Load(pic.Data.Data);
                                    using var thumb = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(150, 150), Mode = ResizeMode.Crop }));
                                    using var msThumb = new MemoryStream();
                                    await thumb.SaveAsWebpAsync(msThumb, new WebpEncoder { Quality = 80 });
                                    msThumb.Position = 0;
                                    coverFileName = $"dev/chat_{conversationId}/{Guid.NewGuid():N}_cover.webp";
                                    await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = coverFileName, InputStream = msThumb, ContentType = "image/webp", DisablePayloadSigning = true });
                                }
                            }
                        }
                        catch { }

                        string durationStr = $"{durationSecs / 60:D2}:{durationSecs % 60:D2}";
                        double sizeMb = new FileInfo(tempOriginalPath).Length / 1048576.0;
                        string sizeStr = $"{sizeMb:F1} MB";

                        // ✨ Добавили -v error, чтобы FFmpeg не мусорил в логи и не переполнял буферы
                        var ffmpegArgs = $"-v error -i \"{tempOriginalPath}\" -vn -c:a aac -b:a 256k -y \"{tempAacPath}\"";
                        using (var process = new System.Diagnostics.Process { StartInfo = new System.Diagnostics.ProcessStartInfo { FileName = "ffmpeg", Arguments = ffmpegArgs, UseShellExecute = false, CreateNoWindow = true } })
                        {
                            process.Start();

                            // ✨ Правильное ожидание с таймаутом
                            var exitTask = process.WaitForExitAsync();
                            var timeoutTask = Task.Delay(TimeSpan.FromMinutes(2));

                            var completedTask = await Task.WhenAny(exitTask, timeoutTask);

                            if (completedTask == timeoutTask)
                            {
                                try { process.Kill(); } catch { }
                                return StatusCode(500, "Timeout audio");
                            }
                        }

                        var uploadTasks = new List<Task>();
                        var aacUploadStream = new FileStream(tempAacPath, FileMode.Open, FileAccess.Read, FileShare.Read, 4096, true);
                        var streamKey = $"{baseFileName}_stream.m4a";
                        uploadTasks.Add(_s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = streamKey, InputStream = aacUploadStream, ContentType = "audio/mp4", DisablePayloadSigning = true }).ContinueWith(_ => aacUploadStream.Dispose()));

                        var masterUploadStream = new FileStream(tempOriginalPath, FileMode.Open, FileAccess.Read, FileShare.Read, 4096, true);
                        var masterKey = $"{baseFileName}_master{extension}";
                        uploadTasks.Add(_s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = masterKey, InputStream = masterUploadStream, ContentType = "audio/mpeg", DisablePayloadSigning = true }).ContinueWith(_ => masterUploadStream.Dispose()));

                        await Task.WhenAll(uploadTasks);

                        attachment.Type = AttachmentType.Audio;
                        attachment.Url = masterKey;
                        attachment.ThumbnailUrl = streamKey;
                        attachment.Size = sizeStr;
                        attachment.Duration = durationStr;
                        attachment.ExtraInfo = $"{trackTitle}|{trackArtist}|{coverFileName}";

                        if (i == 0)
                        {
                            message.AudioTitle = trackTitle;
                            message.AudioArtist = trackArtist;
                            message.AudioDuration = durationStr;
                            message.AudioSize = sizeStr;
                            message.AudioCover = coverFileName;
                            message.Audio = $"{baseFileName}|{extension}";
                        }
                    }
                    else
                    {
                        var key = $"{baseFileName}_doc{extension}";
                        using var stream = new FileStream(tempOriginalPath, FileMode.Open);
                        await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = key, InputStream = stream, ContentType = content_type, DisablePayloadSigning = true });

                        double sizeMb = new FileInfo(tempOriginalPath).Length / 1048576.0;
                        attachment.Type = AttachmentType.File;
                        attachment.Url = key;
                        attachment.Size = sizeMb < 0.1 ? $"{new FileInfo(tempOriginalPath).Length / 1024.0:F1} KB" : $"{sizeMb:F1} MB";

                        if (i == 0) { message.DocumentUrl = key; message.DocumentName = original_file_name; message.DocumentSize = attachment.Size; }
                    }

                    message.Attachments.Add(attachment);
                }
                finally
                {
                    if (System.IO.File.Exists(tempOriginalPath)) System.IO.File.Delete(tempOriginalPath);
                    if (isAudio && System.IO.File.Exists(Path.Combine(Path.GetTempPath(), $"{Guid.NewGuid()}.m4a"))) System.IO.File.Delete(Path.Combine(Path.GetTempPath(), $"{Guid.NewGuid()}.m4a"));
                }
            }
        }

        _context.Messages.Add(message);
        await _context.SaveChangesAsync();

        message = await _context.Messages.Include(m => m.User).Include(m => m.Attachments).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).FirstAsync(m => m.Id == message.Id);

        // ✨ НУЖНО ПЕРЕДАТЬ ПРАВИЛЬНЫЕ ID ПРОЧТЕНИЯ ДЛЯ НОВОГО СООБЩЕНИЯ
        int myReadId = myMember.LastReadMessageId;
        int maxOtherReadId = group.Members.Where(m => m.UserId != userId).Max(m => (int?)m.LastReadMessageId) ?? 0;

        var formattedMessage = FormatMessage(message, userId, me?.Name, myReadId, maxOtherReadId);

        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "new_message", chat_id = conversationId, message = formattedMessage }));

        var usersWhoIgnoredSender = await _context.Users
            .Where(u => u.MutedUsers.Any(mu => mu.Id == userId) ||
                        u.BlockedUsers.Any(bu => bu.Id == userId))
            .Select(u => u.Id)
            .ToListAsync();

        string pushTextContent = message.Body;
        if (string.IsNullOrWhiteSpace(pushTextContent))
        {
            if (!string.IsNullOrEmpty(poll_json)) pushTextContent = "📊 Опрос";
            else if (!string.IsNullOrEmpty(gif_url)) pushTextContent = "🎞 GIF";
            else if (hasFiles) pushTextContent = "📎 Вложение";
            else pushTextContent = "Новое сообщение";
        }

        string pushTitle = group.IsGroup
            ? (group.Name ?? "Группа")
            : (me?.Name ?? "Sonzaiigi");

        string pushBody = group.IsGroup
            ? $"{me?.Name}: {pushTextContent}"
            : pushTextContent;

        var recipients = group.Members
        .Where(m =>
            m.UserId != userId &&
            m.IsMuted == false &&
            m.User != null &&
            !string.IsNullOrEmpty(m.User.FcmToken) &&
            !usersWhoIgnoredSender.Contains(m.UserId)
        )
        .Select(m => m.User)
        .ToList();

        if (recipients.Any())
        {
            var fcmMessages = recipients.Select(u => new FirebaseAdmin.Messaging.Message()
            {
                Token = u!.FcmToken,
                Data = new Dictionary<string, string>
            {
                { "chat_id", conversationId.ToString() },
                { "message_id", message.Id.ToString() },
                { "title", pushTitle },
                { "body", pushBody }
            }
            }).ToList();

            _ = FirebaseAdmin.Messaging.FirebaseMessaging.DefaultInstance.SendEachAsync(fcmMessages).ContinueWith(t => {
                if (t.IsFaulted) Console.WriteLine($"❌ FCM Error: {t.Exception?.GetBaseException().Message}");
                else Console.WriteLine($"✅ FCM: Sent to {t.Result.SuccessCount} users");
            });
        }

        return Ok(formattedMessage);
    }
    [HttpGet("chats")]
    public async Task<IActionResult> GetChats()
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.Include(u => u.Following).Include(u => u.Followers).Include(u => u.BlockedUsers).Include(u => u.BlockedBy).FirstAsync(u => u.Id == myId);

        var conversations = await _context.Conversations.Include(c => c.BannedUsers).Include(c => c.Members).ThenInclude(m => m.User)
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .Include(c => c.Messages.OrderByDescending(m => m.CreatedAt).Take(1)).ThenInclude(m => m.Attachments) // ✨ Подгружаем аттачменты для ласт сообщения
            .OrderByDescending(c => c.Messages.Any() ? c.Messages.Max(m => m.CreatedAt) : c.CreatedAt)
            .Where(c => c.Members.Any(m => m.UserId == myId) && !c.BannedUsers.Any(b => b.UserId == myId)).ToListAsync();

        var result = new List<object>();

        foreach (var conv in conversations)
        {
            var lastMsg = conv.Messages.FirstOrDefault();
            
            int pingsCount = await _context.Messages.CountAsync(m => m.ConversationId == conv.Id && m.UserId != myId && !m.IsRead && m.Body.Contains("@" + me.Username));
            var myMember = conv.Members.First(m => m.UserId == myId);
            int unreadCount = await _context.Messages.CountAsync(m =>
            m.ConversationId == conv.Id &&
            m.UserId != myId &&
            m.Id > myMember.LastReadMessageId);
            if (conv.IsGroup)
            {
                var perms = new { sendMessages = HasPermission(myMember, "sendMessages"), attachFiles = HasPermission(myMember, "attachFiles"), addReactions = HasPermission(myMember, "addReactions"), canForward = HasPermission(myMember, "canForward"), pinMessages = HasPermission(myMember, "pinMessages"), deleteOthersMessages = HasPermission(myMember, "deleteOthersMessages") };
                result.Add(new { id = conv.Id, is_group = true, name = conv.Name, avatar = GetFileUrl(conv.Avatar), description = conv.Description, invite_token = conv.InviteToken, lastMessage = lastMsg?.Body ?? "Нет сообщений", time = lastMsg?.CreatedAt, can_reply = perms.sendMessages, user = (object?)null, unread_count = unreadCount, pings_count = pingsCount, permissions = perms });
                continue;
            }

            var otherUser = conv.Members.FirstOrDefault(m => m.UserId != myId)?.User;
            if (otherUser == null) continue;
            bool canReply = me.Following.Any(u => u.Id == otherUser.Id) && me.Followers.Any(u => u.Id == otherUser.Id) && !me.BlockedUsers.Any(u => u.Id == otherUser.Id) && !me.BlockedBy.Any(u => u.Id == otherUser.Id);
            if (!canReply && lastMsg == null) continue;

            result.Add(new { id = conv.Id, is_group = false, name = otherUser.Name, username = otherUser.Username, user = new { otherUser.Id, otherUser.Name, otherUser.Username }, avatar = GetFileUrl(otherUser.Avatar), lastMessage = lastMsg?.Body ?? "Нет сообщений", time = lastMsg?.CreatedAt, can_reply = canReply, permissions = new { sendMessages = true, attachFiles = true, addReactions = true, canForward = true, pinMessages = true, deleteOthersMessages = false } });
        }
        return Ok(new { chats = result });
    }
    private string? GetUserHighestRoleColor(int userId, int conversationId)
    {
        var highestRole = _context.ConversationMemberRoles
            .Where(mr => mr.Member.UserId == userId && mr.Member.ConversationId == conversationId)
            .Select(mr => mr.Role)
            .OrderBy(r => r.Hierarchy) // Чем меньше число в Hierarchy, тем выше роль
            .FirstOrDefault();

        return highestRole?.Color; // Вернет HEX (например, "#38BDF8") или null
    }
    [HttpPost("groups/join/{token}")]
    public async Task<IActionResult> JoinGroup(string token)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var conversation = await _context.Conversations.Include(c => c.Members).Include(c => c.BannedUsers).FirstOrDefaultAsync(c => c.InviteToken == token);
        if (conversation == null || conversation.IsPrivate) return BadRequest(new { message = "Ссылка недействительна или группа приватная" });
        if (conversation.BannedUsers.Any(b => b.UserId == myId)) return StatusCode(403, new { message = "Вы забанены в этой группе" });

        var existingMember = conversation.Members.FirstOrDefault(m => m.UserId == myId);
        if (existingMember == null)
        {
            conversation.Members.Add(new ConversationMember { UserId = myId });
            await _context.SaveChangesAsync();
            await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "member_joined", chat_id = conversation.Id }));

            if (conversation.SysMsgJoin)
            {
                var me = await _context.Users.FindAsync(myId);
                var msg = new Message { ConversationId = conversation.Id, UserId = myId, Body = "👋 Присоединился(лась) к чату", CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };
                _context.Messages.Add(msg);
                await _context.SaveChangesAsync();
                await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "new_message", chat_id = conversation.Id, message = FormatMessage(msg, myId, me?.Name) }));
            }
        }
        return Ok(new { chat_id = conversation.Id });
    }

    [HttpPost("messages/{id}/react")]
    public async Task<IActionResult> React(int id, [FromBody] ReactDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var msg = await _context.Messages.Include(m => m.Reactions).FirstOrDefaultAsync(m => m.Id == id);
        if (msg == null) return NotFound();

        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == msg.ConversationId);
        if (group != null && group.IsGroup)
        {
            var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
            if (myMember != null && !HasPermission(myMember, "addReactions")) return StatusCode(403, new { message = "Запрещено ставить реакции" });
        }

        var existing = msg.Reactions.FirstOrDefault(r => r.UserId == myId);
        if (existing != null) { if (existing.Emoji == dto.Emoji) _context.Reactions.Remove(existing); else existing.Emoji = dto.Emoji; }
        else
        {
            if (msg.Reactions.Select(r => r.Emoji).Distinct().Count() >= 4 && !msg.Reactions.Any(r => r.Emoji == dto.Emoji)) return BadRequest("Max 4 reactions");
            msg.Reactions.Add(new Reaction { UserId = myId, MessageId = id, Emoji = dto.Emoji });
        }

        await _context.SaveChangesAsync();
        var reactionsData = msg.Reactions.GroupBy(r => r.Emoji).Select(g => new { emoji = g.Key, count = g.Count(), userIds = g.Select(r => r.UserId).ToList() }).ToList();
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "reaction_update", chat_id = msg.ConversationId, message_id = id, reactions = reactionsData }));
        return Ok(reactionsData.Select(r => new { r.emoji, r.count, reacted_by_me = r.userIds.Contains(myId) }));
    }
    [HttpPost("messages/forward")]
    public async Task<IActionResult> Forward([FromBody] ForwardDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(myId);
        var original = await _context.Messages.Include(m => m.User).Include(m => m.Attachments).FirstOrDefaultAsync(m => m.Id == dto.Message_Id);
        if (original == null) return NotFound();

        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == original.ConversationId);
        if (group != null && group.IsGroup)
        {
            var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
            if (myMember != null && !HasPermission(myMember, "canForward")) return StatusCode(403, new { message = "Запрещено пересылать сообщения из этого чата" });
        }

        foreach (var convId in dto.Conversation_Ids)
        {
            var newMessage = new Message { ConversationId = convId, UserId = myId, Body = original.Body, Image = original.Image, GifUrl = original.GifUrl, VideoHls = original.VideoHls, DocumentUrl = original.DocumentUrl, DocumentName = original.DocumentName, Audio = original.Audio, ForwardedFrom = dto.Include_Author ? original.User?.Name : null, CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };

            // ✨ КОПИРУЕМ ВЛОЖЕНИЯ
            foreach (var att in original.Attachments)
            {
                newMessage.Attachments.Add(new MessageAttachment { Type = att.Type, Url = att.Url, Name = att.Name, Size = att.Size, Duration = att.Duration, ThumbnailUrl = att.ThumbnailUrl, ExtraInfo = att.ExtraInfo });
            }

            _context.Messages.Add(newMessage);
            await _context.SaveChangesAsync();
            await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "new_message", chat_id = convId, message = FormatMessage(newMessage, myId, me?.Name) }));
        }
        return Ok(new { success = true });
    }

    [HttpPost("messages/{id}/pin")]
    public async Task<IActionResult> TogglePin(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var msg = await _context.Messages.FindAsync(id);
        if (msg == null) return NotFound();

        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == msg.ConversationId);
        if (group != null && group.IsGroup)
        {
            var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
            if (myMember != null && !HasPermission(myMember, "pinMessages")) return StatusCode(403, new { message = "Нет прав" });
        }

        msg.PinnedAt = msg.PinnedAt.HasValue ? null : DateTime.UtcNow;
        await _context.SaveChangesAsync();
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "message_pinned", chat_id = msg.ConversationId, message_id = id, pinned_at = msg.PinnedAt }));
        return Ok(new { pinned_at = msg.PinnedAt });
    }

    [HttpDelete("messages/{id}")]
    public async Task<IActionResult> DeleteMessage(int id, [FromQuery] bool forEveryone = false)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var msg = await _context.Messages.Include(m => m.Attachments).FirstOrDefaultAsync(m => m.Id == id); // ✨ Include Attachments
        if (msg == null) return NotFound();

        if (forEveryone)
        {
            var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == msg.ConversationId);
            var myMember = group?.Members.FirstOrDefault(m => m.UserId == myId);

            if (msg.UserId != myId && (myMember == null || !HasPermission(myMember, "deleteOthersMessages")))
                return StatusCode(403, new { message = "Нет прав на удаление чужих сообщений" });

            if (!string.IsNullOrEmpty(msg.Image)) try { await _s3Client.DeleteObjectAsync("artworks", msg.Image); } catch { }
            // ✨ УДАЛЯЕМ НОВЫЕ ВЛОЖЕНИЯ ИЗ S3
            foreach (var att in msg.Attachments)
            {
                if (!string.IsNullOrEmpty(att.Url)) try { await _s3Client.DeleteObjectAsync("artworks", att.Url); } catch { }
                if (!string.IsNullOrEmpty(att.ThumbnailUrl)) try { await _s3Client.DeleteObjectAsync("artworks", att.ThumbnailUrl); } catch { }
            }

            _context.Messages.Remove(msg);
            await _context.SaveChangesAsync();
            await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "message_deleted", chat_id = msg.ConversationId, message_id = id }));
        }
        else
        {
            if (!await _context.DeletedMessages.AnyAsync(dm => dm.MessageId == id && dm.UserId == myId))
            {
                _context.DeletedMessages.Add(new DeletedMessage { MessageId = id, UserId = myId, DeletedAt = DateTime.UtcNow });
                await _context.SaveChangesAsync();
            }
        }
        return Ok(new { success = true });
    }

    [HttpPost("conversations/{id}/read/{maxMessageId}")]
    public async Task<IActionResult> MarkAsRead(int id, int maxMessageId)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        // Находим ТВОЕ присутствие в этом чате
        var myMember = await _context.ConversationMembers
            .FirstOrDefaultAsync(m => m.ConversationId == id && m.UserId == myId);

        // Если ты прочитал сообщение, ID которого БОЛЬШЕ, чем то, что ты читал раньше
        if (myMember != null && myMember.LastReadMessageId < maxMessageId)
        {
            myMember.LastReadMessageId = maxMessageId; // Запоминаем твой прогресс
            await _context.SaveChangesAsync();

            // Отправляем по вебсокету уведомление, что кто-то прочитал чат
            await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"),
                JsonSerializer.Serialize(new { type = "messages_read", chat_id = id, reader_id = myId, max_id = maxMessageId }));
        }
        return Ok();
    }
    [HttpGet("messages/{conversationId}/pins")]
    public async Task<IActionResult> GetPins(int conversationId)
    {
        var pins = await _context.Messages.Where(m => m.ConversationId == conversationId && m.PinnedAt != null)
            .OrderByDescending(m => m.PinnedAt).Select(m => new { id = m.Id, text = m.Body ?? "Вложение", pinned_at = m.PinnedAt }).ToListAsync();
        return Ok(pins);
    }

    [HttpGet("messages/{conversationId}/context/{id}")]
    public async Task<IActionResult> LoadContext(int conversationId, int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(myId);

        var before = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).Include(m => m.Attachments)
            .Where(m => m.ConversationId == conversationId && m.Id < id && !m.DeletedMessages.Any(dm => dm.UserId == myId))
            .OrderByDescending(m => m.Id).Take(20).ToListAsync();

        var after = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).Include(m => m.Attachments)
            .Where(m => m.ConversationId == conversationId && m.Id > id && !m.DeletedMessages.Any(dm => dm.UserId == myId))
            .OrderBy(m => m.Id).Take(20).ToListAsync();

        var combined = new List<Message>();
        before.Reverse();
        combined.AddRange(before);

        var fullMsg = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).Include(m => m.Attachments).FirstOrDefaultAsync(m => m.Id == id);
        if (fullMsg != null) combined.Add(fullMsg);
        combined.AddRange(after);

        return Ok(new { messages = combined.Select(m => FormatMessage(m, myId, me?.Name)), has_more_up = before.Count >= 20, has_more_down = after.Count >= 20 });
    }

    [HttpGet("messages/{conversationId}/more-down")]
    public async Task<IActionResult> LoadMoreDown(int conversationId, [FromQuery] int after_id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(myId);

        var messages = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).Include(m => m.Attachments)
            .Where(m => m.ConversationId == conversationId && m.Id > after_id && !m.DeletedMessages.Any(dm => dm.UserId == myId))
            .OrderBy(m => m.Id).Take(19).ToListAsync();

        return Ok(new { messages = messages.Select(m => FormatMessage(m, myId, me?.Name)), has_more_down = messages.Count >= 19 });
    }
    [HttpPost("messages/start/{userId}")]
    public async Task<IActionResult> StartConversation(int userId)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var conversation = await _context.Conversations.Include(c => c.Members).Where(c => !c.IsGroup && c.Members.Any(m => m.UserId == myId) && c.Members.Any(m => m.UserId == userId)).FirstOrDefaultAsync();
        if (conversation == null)
        {
            conversation = new Conversation { IsGroup = false, CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };
            conversation.Members.Add(new ConversationMember { UserId = myId });
            conversation.Members.Add(new ConversationMember { UserId = userId });
            _context.Conversations.Add(conversation);
            await _context.SaveChangesAsync();
        }
        return Ok(new { chatId = conversation.Id });
    }

    [HttpDelete("conversations/{id}")]
    public async Task<IActionResult> DestroyConversation(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var conversation = await _context.Conversations.Include(c => c.Members).FirstOrDefaultAsync(c => c.Id == id);
        if (conversation == null || !conversation.Members.Any(u => u.UserId == myId)) return Forbid();

        var msgsWithImages = await _context.Messages.Include(m => m.Attachments).Where(m => m.ConversationId == id).ToListAsync();
        foreach (var msg in msgsWithImages)
        {
            if (!string.IsNullOrEmpty(msg.Image)) try { await _s3Client.DeleteObjectAsync("artworks", msg.Image); } catch { }
            foreach (var att in msg.Attachments)
            {
                if (!string.IsNullOrEmpty(att.Url)) try { await _s3Client.DeleteObjectAsync("artworks", att.Url); } catch { }
                if (!string.IsNullOrEmpty(att.ThumbnailUrl)) try { await _s3Client.DeleteObjectAsync("artworks", att.ThumbnailUrl); } catch { }
            }
        }

        var msgs = await _context.Messages.Where(m => m.ConversationId == id).ToListAsync();
        _context.Messages.RemoveRange(msgs);
        await _context.SaveChangesAsync();
        return Ok(new { success = true });
    }

    [HttpPatch("messages/{id}")]
    public async Task<IActionResult> UpdateMessage(int id, [FromBody] UpdateMsgDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var msg = await _context.Messages.FindAsync(id);
        if (msg == null || msg.UserId != myId) return Forbid();

        msg.Body = dto.Text;
        msg.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "message_updated", chat_id = msg.ConversationId, message_id = id, text = msg.Body }));

        return Ok(new { id = msg.Id, text = msg.Body });
    }

    [HttpPost("groups/create")]
    public async Task<IActionResult> CreateGroup([FromForm] CreateGroupDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(myId);
        if (me == null || string.IsNullOrWhiteSpace(dto.Name)) return BadRequest();

        var conversation = new Conversation { IsGroup = true, Name = dto.Name, Description = dto.Description, InviteToken = Guid.NewGuid().ToString("N").Substring(0, 15), CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };

        if (dto.Avatar != null)
        {
            using var img = await Image.LoadAsync(dto.Avatar.OpenReadStream());
            img.Mutate(x => x.Resize(new ResizeOptions { Size = new Size(512, 512), Mode = ResizeMode.Crop }));
            using var ms = new MemoryStream();
            await img.SaveAsWebpAsync(ms, new WebpEncoder { Quality = 80 });
            ms.Position = 0;
            var fileName = $"dev/groups/{Guid.NewGuid():N}.webp";
            await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = fileName, InputStream = ms, ContentType = "image/webp", DisablePayloadSigning = true });
            conversation.Avatar = fileName;
        }

        var ownerRole = new GroupRole { Name = "Владелец", Color = "#f59e0b", Hierarchy = 0 };
        var adminRole = new GroupRole { Name = "Админ", Color = "#ef4444", Hierarchy = 1 };
        adminRole.PermissionsJson = JsonSerializer.Serialize(new Dictionary<string, bool> { { "manageChat", true }, { "sendMessages", true }, { "kickMembers", true }, { "banMembers", true } });

        conversation.Roles.Add(ownerRole);
        conversation.Roles.Add(adminRole);

        _context.Conversations.Add(conversation);
        await _context.SaveChangesAsync();

        var ownerMember = new ConversationMember { ConversationId = conversation.Id, UserId = myId, IsOwner = true };
        ownerMember.MemberRoles.Add(new ConversationMemberRole { RoleId = ownerRole.Id });

        _context.ConversationMembers.Add(ownerMember);
        _context.GroupAuditLogs.Add(new GroupAuditLog { ConversationId = conversation.Id, UserId = myId, Action = "создал(а) группу" });
        await _context.SaveChangesAsync();

        return Ok(new { success = true, chat_id = conversation.Id });
    }

    private string? GetFileUrl(string? path)
    {
        if (string.IsNullOrEmpty(path)) return null;
        if (path.StartsWith("http")) return path;
        return $"https://cdn.sonzaiigi.com/{path.TrimStart('/')}";
    }
    [HttpPost("messages/{messageId}/vote")]
    public async Task<IActionResult> VotePoll(int messageId, [FromBody] VoteDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var msg = await _context.Messages.FindAsync(messageId);
        if (msg == null || string.IsNullOrEmpty(msg.PollJson)) return NotFound();

        var poll = JsonSerializer.Deserialize<PollData>(msg.PollJson);
        if (poll == null) return BadRequest();

        if (!poll.IsMultipleChoice)
        {
            foreach (var opt in poll.Options) opt.Voters.Remove(myId);
        }

        var targetOption = poll.Options.FirstOrDefault(o => o.Id == dto.OptionId);
        if (targetOption != null)
        {
            if (targetOption.Voters.Contains(myId)) targetOption.Voters.Remove(myId);
            else targetOption.Voters.Add(myId);
        }

        msg.PollJson = JsonSerializer.Serialize(poll);
        await _context.SaveChangesAsync();
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "poll_update", chat_id = msg.ConversationId, message_id = messageId, poll = poll }));

        return Ok(poll);
    }

    private object FormatMessage(Message m, int myId, string? myName, int myReadId = 0, int maxOtherReadId = 0)
    {
        bool isReplyToMe = m.Parent != null && m.Parent.UserId == myId;
        bool isMention = !string.IsNullOrEmpty(m.Body) && !string.IsNullOrEmpty(myName) && m.Body.Contains($"@{myName}");

        var attachmentsList = new List<object>();

        // ✨ ИСПРАВЛЕНИЕ ДУБЛИКАТОВ: Если есть новые вложения, берем ТОЛЬКО ИХ
        if (m.Attachments != null && m.Attachments.Any())
        {
            foreach (var att in m.Attachments)
            {
                attachmentsList.Add(new
                {
                    type = att.Type.ToString().ToLower(),
                    url = GetFileUrl(att.Url),
                    thumb = GetFileUrl(att.ThumbnailUrl),
                    width = att.Width,   // ✨ Добавили
                    height = att.Height, // ✨ Добавили
                    name = att.Name,
                    size = att.Size,
                    duration = att.Duration,
                    extra_info = att.ExtraInfo
                });
            }
        }
        else // Если вложений нет, значит это старое сообщение из БД, читаем из старых колонок
        {
            if (!string.IsNullOrEmpty(m.Image))
            {
                var parts = m.Image.Split('|');
                var masterExt = parts.Length > 1 ? parts[1] : ".jpg";
                attachmentsList.Add(new
                {
                    type = masterExt == ".gif" ? "gif" : "image",
                    url = GetFileUrl($"{parts[0]}_master{masterExt}"),
                    thumb = masterExt == ".gif" ? GetFileUrl($"{parts[0]}_master.gif") : GetFileUrl($"{parts[0]}_view.webp")
                });
            }
            if (!string.IsNullOrEmpty(m.VideoHls))
                attachmentsList.Add(new { type = "video", url = GetFileUrl(m.VideoHls), size = m.DocumentSize });
            if (!string.IsNullOrEmpty(m.DocumentUrl))
                attachmentsList.Add(new { type = "file", url = GetFileUrl(m.DocumentUrl), name = m.DocumentName, size = m.DocumentSize });
            if (!string.IsNullOrEmpty(m.Audio))
            {
                var pParts = m.Audio.Split('|');
                var pMasterExt = pParts.Length > 1 ? pParts[1] : ".wav";
                attachmentsList.Add(new { type = "audio", url = GetFileUrl($"{pParts[0]}_master{pMasterExt}"), thumb = GetFileUrl($"{pParts[0]}_stream.m4a"), name = m.AudioTitle, duration = m.AudioDuration, extra_info = $"{m.AudioArtist}|{m.AudioCover}" });
            }
        }

        // --- СТАРЫЕ ПОЛЯ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ ANDROID (Оставляем) ---
        string? imageThumb = null, imageView = null, imageMaster = null;
        string? audioStream = null, audioMaster = null, videoMaster = null;

        if (!string.IsNullOrEmpty(m.VideoHls)) videoMaster = GetFileUrl(m.VideoHls);
        if (!string.IsNullOrEmpty(m.Audio))
        {
            var parts = m.Audio.Split('|');
            audioStream = GetFileUrl($"{parts[0]}_stream.m4a");
            audioMaster = GetFileUrl($"{parts[0]}_master{(parts.Length > 1 ? parts[1] : ".wav")}");
        }
        if (!string.IsNullOrEmpty(m.Image))
        {
            var parts = m.Image.Split('|');
            var masterExt = parts.Length > 1 ? parts[1] : ".jpg";
            if (masterExt == ".gif")
            {
                imageThumb = GetFileUrl($"{parts[0]}_master.gif");
                imageView = GetFileUrl($"{parts[0]}_master.gif");
                imageMaster = GetFileUrl($"{parts[0]}_master.gif");
            }
            else
            {
                imageThumb = GetFileUrl($"{parts[0]}_view.webp");
                imageView = GetFileUrl($"{parts[0]}_view.webp");
                imageMaster = GetFileUrl($"{parts[0]}_master{masterExt}");
            }
        }
        bool isRead = m.UserId == myId
    ? (m.Id <= maxOtherReadId)
    : (m.Id <= myReadId);



        // ✨ ФОРМИРУЕМ ПОЛНОЦЕННЫЙ БЛОК ОТВЕТА С МЕДИА ✨
        object? replyToObj = null;
        if (m.Parent != null)
        {
            string? parentThumb = null;
            string? parentVideo = null;

            // Ищем в новых вложениях
            var firstAtt = m.Parent.Attachments?.FirstOrDefault();
            if (firstAtt != null)
            {
                if (firstAtt.Type == AttachmentType.Video) parentVideo = GetFileUrl(firstAtt.ThumbnailUrl ?? firstAtt.Url);
                else parentThumb = GetFileUrl(firstAtt.ThumbnailUrl ?? firstAtt.Url);
            }
            else // Ищем в старых
            {
                if (!string.IsNullOrEmpty(m.Parent.VideoHls)) parentVideo = GetFileUrl(m.Parent.VideoHls);
                else if (!string.IsNullOrEmpty(m.Parent.Image))
                {
                    var p = m.Parent.Image.Split('|');
                    parentThumb = GetFileUrl(p.Length > 1 && p[1] == ".gif" ? $"{p[0]}_master.gif" : $"{p[0]}_view.webp");
                }
            }

            replyToObj = new
            {
                id = m.Parent.Id,
                name = m.Parent.User?.Name ?? "Кто-то",
                text = string.IsNullOrEmpty(m.Parent.Body) ? "Вложение" : m.Parent.Body,
                image_thumb = parentThumb,
                video_master = parentVideo
            };
        }

        return new
        {
            id = m.Id,
            text = m.Body,
            senderId = m.UserId,
            senderColor = GetUserHighestRoleColor(m.UserId, m.ConversationId),
            senderUsername = m.User?.Username ?? "Unknown",
            senderName = m.User?.Name ?? "Unknown",
            senderAvatar = GetFileUrl(m.User?.Avatar),
            time = m.CreatedAt,
            is_pinned = m.PinnedAt != null,
            is_mention = isMention || isReplyToMe,
            forwarded_from = m.ForwardedFrom,
            is_read = isRead,

            attachments = attachmentsList,

            image = GetFileUrl(m.Image),
            gif_url = m.GifUrl,
            image_thumb = imageThumb,
            image_view = imageView,
            image_master = imageMaster,
            document_url = GetFileUrl(m.DocumentUrl),
            document_name = m.DocumentName,
            document_size = m.DocumentSize,
            audio_stream = audioStream,
            audio_master = audioMaster,
            audio_title = m.AudioTitle,
            audio_artist = m.AudioArtist,
            audio_duration = m.AudioDuration,
            video_master = videoMaster,
            audio_size = m.AudioSize,
            audio_cover = GetFileUrl(m.AudioCover),

            poll = string.IsNullOrEmpty(m.PollJson) ? null : JsonSerializer.Deserialize<object>(m.PollJson),
            reactions = m.Reactions?.GroupBy(r => r.Emoji).Select(g => new { emoji = g.Key, count = g.Count(), reacted_by_me = g.Any(r => r.UserId == myId), userIds = g.Select(x => x.UserId).ToList() }),

            // ✨ Используем наш новый объект с картинками!
            reply_to = replyToObj
        };
    }
    [HttpPost("users/fcm-token")]
    public async Task<IActionResult> UpdateFcmToken([FromBody] FcmTokenDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        // ✨ ИСПРАВЛЕНИЕ: Удаляем этот токен у всех остальных пользователей, 
        // чтобы пуши не приходили чужим аккаунтам на этом же телефоне
        var usersWithSameToken = await _context.Users
            .Where(u => u.FcmToken == dto.Token && u.Id != myId)
            .ToListAsync();

        foreach (var u in usersWithSameToken)
        {
            u.FcmToken = null;
        }

        var me = await _context.Users.FindAsync(myId);
        if (me == null) return NotFound();

        me.FcmToken = dto.Token;
        await _context.SaveChangesAsync();
        return Ok();
    }
    // Класс для DTO (можно положить в конец файла)
    public class ImportStickerDto
    {
        public string Link { get; set; } = string.Empty;
    }
}

public class FcmTokenDto
{
    public string Token { get; set; } = string.Empty; }


public class UpdateMsgDto { public string Text { get; set; } = string.Empty; }
public class ReactDto { public string Emoji { get; set; } = string.Empty; }
public class ForwardDto { public int Message_Id { get; set; } public List<int> Conversation_Ids { get; set; } = new(); public bool Include_Author { get; set; } }
public class CreateGroupDto { public string? Name { get; set; } = string.Empty; public string? Description { get; set; } public IFormFile? Avatar { get; set; } }
public class VoteDto { public string OptionId { get; set; } = string.Empty; }
public class PollData
{
    public string Question { get; set; } = string.Empty;
    public bool IsMultipleChoice { get; set; } = false;
    public List<PollOption> Options { get; set; } = new();
}
public class PollOption
{
    public string Id { get; set; } = Guid.NewGuid().ToString("N");
    public string Text { get; set; } = string.Empty;
    public string? ImageUrl { get; set; }
    public string? ImageViewUrl { get; set; }
    public int? ImageIndex { get; set; } // ✨ Связь с загруженным файлом (его порядковый номер)
    public List<int> Voters { get; set; } = new();
}
