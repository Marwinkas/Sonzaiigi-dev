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
        // 1. Владельцу чата можно абсолютно всё
        if (member.IsOwner) return true;

        // 2. Индивидуальные настройки участника (крестик или галочка в профиле) всегда в приоритете
        if (member.IndividualOverrides.TryGetValue(perm, out bool over)) return over;

        // 3. Наши стандартные разрешения
        bool isDefaultAllowed = perm == "sendMessages" || perm == "canForward" || perm == "addReactions" || perm == "attachFiles";

        // Если у пользователя ВООБЩЕ НЕТ ролей — он пользуется стандартными разрешениями
        if (!member.MemberRoles.Any()) return isDefaultAllowed;

        // 4. ЕСЛИ РОЛИ ЕСТЬ: стандартные разрешения перестают действовать автоматически.
        // Теперь мы разрешаем действие ТОЛЬКО если оно явно включено (стоит галочка) хотя бы в одной из ролей.
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
            .OrderByDescending(m => m.CreatedAt).Skip(offset).Take(limit + 1).Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).ToListAsync();

        bool hasMore = messages.Count > limit;
        if (hasMore) messages.RemoveAt(messages.Count - 1);
        messages.Reverse();

        var myMember = group.Members.First(m => m.UserId == myId);
        return Ok(new { messages = messages.Select(m => FormatMessage(m, myId, me?.Name)), has_more = hasMore, can_reply = !group.IsGroup || HasPermission(myMember, "sendMessages") });
    }
    [HttpPost("messages/{conversationId}/upload-chunk")]
    public async Task<IActionResult> UploadChunk(int conversationId, [FromForm] IFormFile chunk, [FromForm] int chunkIndex, [FromForm] string uploadId)
    {
        // Путь, куда будем по кусочкам дописывать файл
        var tempPath = Path.Combine(Path.GetTempPath(), $"upload_{uploadId}");

        // Если это первый кусок — создаем файл (перезаписываем старый), иначе — дописываем в конец
        using (var stream = new FileStream(tempPath, chunkIndex == 0 ? FileMode.Create : FileMode.Append))
        {
            await chunk.CopyToAsync(stream);
        }

        return Ok();
    }
    [HttpPost("messages/{conversationId}")]
    public async Task<IActionResult> SendMessage(int conversationId, [FromForm] string? text, [FromForm] int? parent_id, [FromForm] string? gif_url, [FromForm] string? poll_json, [FromForm] List<IFormFile>? poll_images,
        [FromForm] string? uploaded_file_id, [FromForm] string? original_file_name, [FromForm] string? content_type)
    {
        var userId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(userId);
        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == conversationId);
        if (group == null) return NotFound();
        var myMember = group.Members.FirstOrDefault(m => m.UserId == userId);
        if (myMember == null) return Forbid();
        bool hasFile = !string.IsNullOrEmpty(uploaded_file_id);

        // Проверки базовых прав
        if (group.IsGroup && !HasPermission(myMember, "sendMessages")) return StatusCode(403, new { message = "Вам запрещено отправлять сообщения" });
        if (group.IsGroup && (hasFile != null || gif_url != null) && !HasPermission(myMember, "attachFiles")) return StatusCode(403, new { message = "Вам запрещено прикреплять медиафайлы" });

        // --- МЕДЛЕННЫЙ РЕЖИМ ---
        if (group.IsGroup && group.SlowMode > 0 && !HasPermission(myMember, "bypassSlowMode"))
        {
            var lastMessage = await _context.Messages
                .Where(m => m.ConversationId == conversationId && m.UserId == userId)
                .OrderByDescending(m => m.CreatedAt)
                .FirstOrDefaultAsync();

            if (lastMessage != null)
            {
                var secondsPassed = (DateTime.UtcNow - lastMessage.CreatedAt).TotalSeconds;
                if (secondsPassed < group.SlowMode)
                {
                    var waitTime = Math.Ceiling(group.SlowMode - secondsPassed);
                    return StatusCode(429, new { message = $"Работает медленный режим. Пожалуйста, подожди еще {waitTime} сек." });
                }
            }
        }

        var message = new Message { ConversationId = conversationId, UserId = userId, Body = text ?? "", ParentId = parent_id, GifUrl = gif_url, CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };

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

                                // ✨ Оставили только 1200х1200 для опросов
                                using var view = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(1200, 1200), Mode = ResizeMode.Max }));
                                var msView = new MemoryStream();
                                await view.SaveAsWebpAsync(msView, new WebpEncoder { Quality = 82 });
                                msView.Position = 0;

                                var viewFileName = $"dev/chat_{conversationId}/{baseGuid}_poll_view.webp";

                                // Обе ссылки теперь ведут на версию 1200x1200
                                opt.ImageUrl = viewFileName;
                                opt.ImageViewUrl = viewFileName;

                                uploadTasks.Add(_s3Client.PutObjectAsync(new PutObjectRequest
                                {
                                    BucketName = "artworks",
                                    Key = viewFileName,
                                    InputStream = msView,
                                    ContentType = "image/webp",
                                    DisablePayloadSigning = true
                                }).ContinueWith(_ => msView.Dispose()));
                            }
                        }
                    }
                    if (uploadTasks.Any()) await Task.WhenAll(uploadTasks);
                }
                message.PollJson = JsonSerializer.Serialize(poll);
            }
        }

        if (hasFile)
        {
            var rawTempPath = Path.Combine(Path.GetTempPath(), $"upload_{uploaded_file_id}");
            if (!System.IO.File.Exists(rawTempPath)) return BadRequest("Файл не найден.");

            var extension = Path.GetExtension(original_file_name).ToLower();
            var tempOriginalPath = rawTempPath + extension;

            if (System.IO.File.Exists(tempOriginalPath)) System.IO.File.Delete(tempOriginalPath);
            System.IO.File.Move(rawTempPath, tempOriginalPath);

            var baseFileName = $"dev/chat_{conversationId}/{Guid.NewGuid():N}";

            var isGif = extension == ".gif" || content_type == "image/gif";
            var isVideo = !isGif && content_type?.StartsWith("video/") == true;
            var isImage = !isGif && content_type?.StartsWith("image/") == true;
            var isAudio = content_type?.StartsWith("audio/") == true;

            try
            {
                // === ОБРАБОТКА GIF (КОНВЕРТАЦИЯ В WEBM) ===
                // === ОБРАБОТКА GIF ===
                if (isGif)
                {
                    // ✨ Отменяем WebM! Сохраняем как есть, чтобы Android мог проиграть анимацию
                    using var stream = new FileStream(tempOriginalPath, FileMode.Open);
                    await _s3Client.PutObjectAsync(new PutObjectRequest
                    {
                        BucketName = "artworks",
                        Key = $"{baseFileName}_master.gif",
                        InputStream = stream,
                        ContentType = "image/gif",
                        DisablePayloadSigning = true
                    });

                    message.Image = $"{baseFileName}|.gif";
                }
                // === ОБРАБОТКА ОБЫЧНЫХ ИЗОБРАЖЕНИЙ ===
                else if (isImage)
                {
                    using var img = await Image.LoadAsync(tempOriginalPath);

                    // ✨ Убрали thumb, оставили только view 1200x1200 и master
                    using var view = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(1200, 1200), Mode = ResizeMode.Max }));
                    using var msView = new MemoryStream();
                    await view.SaveAsWebpAsync(msView, new WebpEncoder { Quality = 82 });
                    msView.Position = 0;
                    await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = $"{baseFileName}_view.webp", InputStream = msView, ContentType = "image/webp", DisablePayloadSigning = true });

                    using var master = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(3840, 3840), Mode = ResizeMode.Max }));
                    using var msMaster = new MemoryStream();
                    string masterExt = original_file_name.EndsWith(".png", StringComparison.OrdinalIgnoreCase) ? ".png" : ".jpg";
                    if (masterExt == ".png") await master.SaveAsPngAsync(msMaster);
                    else await master.SaveAsJpegAsync(msMaster, new SixLabors.ImageSharp.Formats.Jpeg.JpegEncoder { Quality = 90 });
                    msMaster.Position = 0;
                    await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = $"{baseFileName}_master{masterExt}", InputStream = msMaster, ContentType = masterExt == ".png" ? "image/png" : "image/jpeg", DisablePayloadSigning = true });

                    message.Image = $"{baseFileName}|{masterExt}";
                }
                else if (isVideo)
                {
                    // 1. Формируем имя файла (оригинальное расширение)
                    var originalExt = Path.GetExtension(original_file_name).ToLower();
                    var fileName = $"{baseFileName}_video{originalExt}";

                    // 2. Грузим оригинал в Cloudflare R2
                    using var stream = new FileStream(tempOriginalPath, FileMode.Open);
                    await _s3Client.PutObjectAsync(new PutObjectRequest
                    {
                        BucketName = "artworks",
                        Key = fileName,
                        InputStream = stream,
                        ContentType = content_type,
                        DisablePayloadSigning = true,
                        UseChunkEncoding = false
                    });

                    // 3. Сохраняем прямой путь в базу
                    // (Используем уже готовую колонку VideoHls, чтобы не делать миграции)
                    message.VideoHls = fileName;

                    // Запишем размер файла (как у документов), чтобы фронтенд мог его показать, если нужно
                    double sizeMb = new FileInfo(tempOriginalPath).Length / 1048576.0;
                    message.DocumentSize = sizeMb < 0.1 ? $"{new FileInfo(tempOriginalPath).Length / 1024.0:F1} KB" : $"{sizeMb:F1} MB";
                }
                // === ОБРАБОТКА АУДИО (AAC 256kbps) ===
                else if (isAudio)
                {
                    var originalExt = Path.GetExtension(original_file_name).ToLower();
                    var tempAacPath = Path.Combine(Path.GetTempPath(), $"{Guid.NewGuid()}.m4a");

                    string trackTitle = Path.GetFileNameWithoutExtension(original_file_name);
                    string trackArtist = me?.Name ?? "Пользователь";
                    int durationSecs = 0;
                    string? coverFileName = null;
                    try
                    {
                        try
                        {
                            using (var tfile = TagLib.File.Create(tempOriginalPath))
                            {
                                if (!string.IsNullOrEmpty(tfile.Tag.Title))
                                    trackTitle = tfile.Tag.Title;

                                if (!string.IsNullOrEmpty(tfile.Tag.FirstPerformer))
                                    trackArtist = tfile.Tag.FirstPerformer;

                                durationSecs = (int)tfile.Properties.Duration.TotalSeconds;

                                if (tfile.Tag.Pictures.Length > 0)
                                {
                                    var pic = tfile.Tag.Pictures[0];
                                    using var img = Image.Load(pic.Data.Data);

                                    // Обложку аудио пока оставляю маленькой, чтобы не грузить плеер, 
                                    // но если хочешь 1200х1200 и тут - скажи!
                                    using var thumb = img.Clone(x => x.Resize(new ResizeOptions { Size = new Size(150, 150), Mode = ResizeMode.Crop }));
                                    using var msThumb = new MemoryStream();
                                    await thumb.SaveAsWebpAsync(msThumb, new WebpEncoder { Quality = 80 });
                                    msThumb.Position = 0;
                                    coverFileName = $"dev/chat_{conversationId}/{Guid.NewGuid():N}_cover.webp";
                                    await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = coverFileName, InputStream = msThumb, ContentType = "image/webp", DisablePayloadSigning = true });
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine($"TagLib не смог прочитать теги: {ex.Message}");
                        }

                        string durationStr = $"{durationSecs / 60:D2}:{durationSecs % 60:D2}";
                        double sizeMb = new FileInfo(tempOriginalPath).Length / 1048576.0;
                        string sizeStr = $"{sizeMb:F1} MB";

                        var ffmpegArgs = $"-i \"{tempOriginalPath}\" -vn -c:a aac -b:a 256k -y \"{tempAacPath}\"";
                        using (var process = new System.Diagnostics.Process
                        {
                            StartInfo = new System.Diagnostics.ProcessStartInfo
                            {
                                FileName = "ffmpeg",
                                Arguments = ffmpegArgs,
                                UseShellExecute = false,
                                CreateNoWindow = true,
                                RedirectStandardOutput = false,
                                RedirectStandardError = false
                            }
                        })
                        {
                            process.Start();
                            var completedTask = process.WaitForExitAsync();
                            var timeoutTask = Task.Delay(TimeSpan.FromMinutes(2));

                            if (await Task.WhenAny(completedTask, timeoutTask) == timeoutTask)
                            {
                                process.Kill();
                                return StatusCode(500, "Обработка аудио заняла слишком много времени.");
                            }
                        }

                        var uploadTasks = new List<Task>();

                        var aacUploadStream = new FileStream(tempAacPath, FileMode.Open, FileAccess.Read, FileShare.Read, 4096, true);
                        uploadTasks.Add(_s3Client.PutObjectAsync(new PutObjectRequest
                        {
                            BucketName = "artworks",
                            Key = $"{baseFileName}_stream.m4a",
                            InputStream = aacUploadStream,
                            ContentType = "audio/mp4",
                            DisablePayloadSigning = true
                        }).ContinueWith(_ => aacUploadStream.Dispose()));

                        var masterUploadStream = new FileStream(tempOriginalPath, FileMode.Open, FileAccess.Read, FileShare.Read, 4096, true);
                        string masterContentType = originalExt == ".wav" ? "audio/wav" : originalExt == ".flac" ? "audio/flac" : "audio/mpeg";
                        uploadTasks.Add(_s3Client.PutObjectAsync(new PutObjectRequest
                        {
                            BucketName = "artworks",
                            Key = $"{baseFileName}_master{originalExt}",
                            InputStream = masterUploadStream,
                            ContentType = masterContentType,
                            DisablePayloadSigning = true
                        }).ContinueWith(_ => masterUploadStream.Dispose()));

                        await Task.WhenAll(uploadTasks);

                        message.AudioTitle = trackTitle;
                        message.AudioArtist = trackArtist;
                        message.AudioDuration = durationStr;
                        message.AudioSize = sizeStr;
                        message.AudioCover = coverFileName;
                        message.Audio = $"{baseFileName}|{originalExt}";
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine($"Ошибка обработки аудио: {ex.Message}");
                        return StatusCode(500, "Ошибка при обработке аудиофайла.");
                    }
                    finally
                    {
                        if (System.IO.File.Exists(tempOriginalPath)) try { System.IO.File.Delete(tempOriginalPath); } catch { }
                        if (System.IO.File.Exists(tempAacPath)) try { System.IO.File.Delete(tempAacPath); } catch { }
                    }
                }
                else
                {
                    // === ОБРАБОТКА ЛЮБЫХ ДРУГИХ ФАЙЛОВ ===
                    var originalExt = Path.GetExtension(original_file_name).ToLower();
                    var fileName = $"{baseFileName}_doc{originalExt}";

                    using var stream = new FileStream(tempOriginalPath, FileMode.Open);
                    await _s3Client.PutObjectAsync(new PutObjectRequest
                    {
                        BucketName = "artworks",
                        Key = fileName,
                        InputStream = stream,
                        ContentType = content_type,
                        DisablePayloadSigning = true
                    });

                    message.DocumentUrl = fileName;
                    message.DocumentName = original_file_name;
                    double sizeMb = new FileInfo(tempOriginalPath).Length / 1048576.0;
                    message.DocumentSize = sizeMb < 0.1 ? $"{new FileInfo(tempOriginalPath).Length / 1024.0:F1} KB" : $"{sizeMb:F1} MB";
                }
            }
            finally
            {
                if (System.IO.File.Exists(tempOriginalPath)) System.IO.File.Delete(tempOriginalPath);
            }
        }

        _context.Messages.Add(message);
        await _context.SaveChangesAsync();

        message = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).FirstAsync(m => m.Id == message.Id);
        var formattedMessage = FormatMessage(message, userId, me?.Name);

        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "new_message", chat_id = conversationId, message = formattedMessage }));

        return Ok(formattedMessage);
    }
    [HttpGet("chats")]
    public async Task<IActionResult> GetChats()
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.Include(u => u.Following).Include(u => u.Followers).Include(u => u.BlockedUsers).Include(u => u.BlockedBy).FirstAsync(u => u.Id == myId);

        var conversations = await _context.Conversations.Include(c => c.BannedUsers).Include(c => c.Members).ThenInclude(m => m.User)
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .Include(c => c.Messages.OrderByDescending(m => m.CreatedAt).Take(1))
            .OrderByDescending(c => c.Messages.Any()
        ? c.Messages.Max(m => m.CreatedAt)
        : c.CreatedAt)
            .Where(c => c.Members.Any(m => m.UserId == myId) && !c.BannedUsers.Any(b => b.UserId == myId)).ToListAsync();

        var result = new List<object>();

        foreach (var conv in conversations)
        {
            var lastMsg = conv.Messages.FirstOrDefault();
            int unreadCount = await _context.Messages.CountAsync(m => m.ConversationId == conv.Id && m.UserId != myId && !m.IsRead);
            int pingsCount = await _context.Messages.CountAsync(m => m.ConversationId == conv.Id && m.UserId != myId && !m.IsRead && m.Body.Contains("@" + me.Username));
            var myMember = conv.Members.First(m => m.UserId == myId);

            if (conv.IsGroup)
            {
                var perms = new
                {
                    sendMessages = HasPermission(myMember, "sendMessages"),
                    attachFiles = HasPermission(myMember, "attachFiles"),
                    addReactions = HasPermission(myMember, "addReactions"),
                    canForward = HasPermission(myMember, "canForward"),
                    pinMessages = HasPermission(myMember, "pinMessages"),
                    deleteOthersMessages = HasPermission(myMember, "deleteOthersMessages")
                };

                result.Add(new
                {
                    id = conv.Id,
                    is_group = true,
                    name = conv.Name,
                    avatar = GetFileUrl(conv.Avatar),
                    description = conv.Description,
                    invite_token = conv.InviteToken,
                    lastMessage = lastMsg?.Body ?? "Нет сообщений",
                    time = lastMsg?.CreatedAt,
                    can_reply = perms.sendMessages,
                    user = (object?)null,
                    unread_count = unreadCount,
                    pings_count = pingsCount,
                    permissions = perms
                });
                continue;
            }

            var otherUser = conv.Members.FirstOrDefault(m => m.UserId != myId)?.User;
            if (otherUser == null) continue;
            bool canReply = me.Following.Any(u => u.Id == otherUser.Id) && me.Followers.Any(u => u.Id == otherUser.Id) && !me.BlockedUsers.Any(u => u.Id == otherUser.Id) && !me.BlockedBy.Any(u => u.Id == otherUser.Id);
            if (!canReply && lastMsg == null) continue;

            var personalPerms = new { sendMessages = true, attachFiles = true, addReactions = true, canForward = true, pinMessages = true, deleteOthersMessages = false };

            result.Add(new
            {
                id = conv.Id,
                is_group = false,
                name = otherUser.Name,
                username = otherUser.Username,
                user = new { otherUser.Id, otherUser.Name, otherUser.Username },
                avatar = GetFileUrl(otherUser.Avatar),
                lastMessage = lastMsg?.Body ?? "Нет сообщений",
                time = lastMsg?.CreatedAt,
                can_reply = canReply,
                permissions = personalPerms
            });
        }
        return Ok(new { chats = result });
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
        if (existing != null)
        {
            if (existing.Emoji == dto.Emoji) _context.Reactions.Remove(existing);
            else existing.Emoji = dto.Emoji;
        }
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
        var original = await _context.Messages.Include(m => m.User).FirstOrDefaultAsync(m => m.Id == dto.Message_Id);
        if (original == null) return NotFound();

        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == original.ConversationId);
        if (group != null && group.IsGroup)
        {
            var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
            if (myMember != null && !HasPermission(myMember, "canForward")) return StatusCode(403, new { message = "Запрещено пересылать сообщения из этого чата" });
        }

        foreach (var convId in dto.Conversation_Ids)
        {
            var newMessage = new Message { ConversationId = convId, UserId = myId, Body = original.Body, Image = original.Image, GifUrl = original.GifUrl, ForwardedFrom = dto.Include_Author ? original.User?.Name : null, CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };
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
        var msg = await _context.Messages.FindAsync(id);
        if (msg == null) return NotFound();

        if (forEveryone)
        {
            var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == msg.ConversationId);
            var myMember = group?.Members.FirstOrDefault(m => m.UserId == myId);

            if (msg.UserId != myId && (myMember == null || !HasPermission(myMember, "deleteOthersMessages")))
                return StatusCode(403, new { message = "Нет прав на удаление чужих сообщений" });

            if (!string.IsNullOrEmpty(msg.Image)) try { await _s3Client.DeleteObjectAsync("artworks", msg.Image); } catch { }
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

    // ✨ ВОТ ЗДЕСЬ МЫ ДОБАВИЛИ {maxMessageId} ✨
    [HttpPost("conversations/{id}/read/{maxMessageId}")]
    public async Task<IActionResult> MarkAsRead(int id, int maxMessageId)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        // Берем только те сообщения, ID которых МЕНЬШЕ ИЛИ РАВЕН тому, что мы видим
        var unreadMessages = await _context.Messages
            .Where(m => m.ConversationId == id && m.UserId != myId && !m.IsRead && m.Id <= maxMessageId)
            .ToListAsync();

        if (unreadMessages.Any())
        {
            foreach (var m in unreadMessages) m.IsRead = true;
            await _context.SaveChangesAsync();
            await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "messages_read", chat_id = id, reader_id = myId }));
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

        var before = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions)
            .Where(m => m.ConversationId == conversationId && m.Id < id && !m.DeletedMessages.Any(dm => dm.UserId == myId))
            .OrderByDescending(m => m.Id).Take(20).ToListAsync();

        var after = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions)
            .Where(m => m.ConversationId == conversationId && m.Id > id && !m.DeletedMessages.Any(dm => dm.UserId == myId))
            .OrderBy(m => m.Id).Take(20).ToListAsync();

        var combined = new List<Message>();
        before.Reverse();
        combined.AddRange(before);

        var fullMsg = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions).FirstOrDefaultAsync(m => m.Id == id);
        if (fullMsg != null) combined.Add(fullMsg);
        combined.AddRange(after);

        return Ok(new { messages = combined.Select(m => FormatMessage(m, myId, me?.Name)), has_more_up = before.Count >= 20, has_more_down = after.Count >= 20 });
    }

    [HttpGet("messages/{conversationId}/more-down")]
    public async Task<IActionResult> LoadMoreDown(int conversationId, [FromQuery] int after_id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var me = await _context.Users.FindAsync(myId);

        var messages = await _context.Messages.Include(m => m.User).Include(m => m.Parent).ThenInclude(p => p.User).Include(m => m.Reactions)
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

        var msgsWithImages = await _context.Messages.Where(m => m.ConversationId == id && m.Image != null).ToListAsync();
        foreach (var msg in msgsWithImages) { try { await _s3Client.DeleteObjectAsync("artworks", msg.Image); } catch { } }

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

        // 1. Десериализуем опрос
        var poll = JsonSerializer.Deserialize<PollData>(msg.PollJson);
        if (poll == null) return BadRequest();

        // 2. Убираем старый голос пользователя со всех вариантов (если это не мульти-выбор)
        if (!poll.IsMultipleChoice)
        {
            foreach (var opt in poll.Options) opt.Voters.Remove(myId);
        }

        // 3. Добавляем (или убираем) голос на выбранный вариант
        var targetOption = poll.Options.FirstOrDefault(o => o.Id == dto.OptionId);
        if (targetOption != null)
        {
            if (targetOption.Voters.Contains(myId)) targetOption.Voters.Remove(myId); // Снятие голоса
            else targetOption.Voters.Add(myId); // Голосование
        }

        // 4. Сохраняем обратно в БД
        msg.PollJson = JsonSerializer.Serialize(poll);
        await _context.SaveChangesAsync();

        // 5. Рассылаем обновление по сокетам
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new
        {
            type = "poll_update",
            chat_id = msg.ConversationId,
            message_id = messageId,
            poll = poll
        }));

        return Ok(poll);
    }
    private object FormatMessage(Message m, int myId, string? myName)
    {
        bool isReplyToMe = m.Parent != null && m.Parent.UserId == myId;
        bool isMention = !string.IsNullOrEmpty(m.Body) && !string.IsNullOrEmpty(myName) && m.Body.Contains($"@{myName}");

        string? imageThumb = null;
        string? imageView = null;
        string? imageMaster = null;
        string? audioStream = null;
        string? audioMaster = null;
        string? videoMaster = null;

        if (!string.IsNullOrEmpty(m.VideoHls))
        {
            // Отдаем прямую ссылку на наш загруженный оригинал
            videoMaster = GetFileUrl(m.VideoHls);
        }
        if (!string.IsNullOrEmpty(m.Audio))
        {
            var parts = m.Audio.Split('|');
            var basePath = parts[0];
            var masterExt = parts.Length > 1 ? parts[1] : ".wav";

            audioStream = GetFileUrl($"{basePath}_stream.m4a"); // Наш легкий AAC
            audioMaster = GetFileUrl($"{basePath}_master{masterExt}"); // Исходник
        }
        // ✨ Разбираем базовый путь и расширение мастер-файла
        if (!string.IsNullOrEmpty(m.Image))
        {
            var parts = m.Image.Split('|');
            var basePath = parts[0];
            var masterExt = parts.Length > 1 ? parts[1] : ".jpg";

            // ✨ Если это гифка, отдаем везде оригинал, чтобы она сразу играла в клиенте!
            if (masterExt == ".gif")
            {
                imageThumb = GetFileUrl($"{basePath}_master.gif");
                imageView = GetFileUrl($"{basePath}_master.gif");
                imageMaster = GetFileUrl($"{basePath}_master.gif");
            }
            else
            {
                imageThumb = GetFileUrl($"{basePath}_view.webp");
                imageView = GetFileUrl($"{basePath}_view.webp");
                imageMaster = GetFileUrl($"{basePath}_master{masterExt}");
            }
        }
        return new
        {
            id = m.Id,
            text = m.Body,
            image = GetFileUrl(m.Image),
            gif_url = m.GifUrl,
            senderId = m.UserId,
            senderUsername = m.User?.Username ?? "Unknown",
            senderName = m.User?.Name ?? "Unknown",
            senderAvatar = GetFileUrl(m.User?.Avatar),
            image_thumb = imageThumb,   // ✨ Новое поле
            image_view = imageView,     // ✨ Новое поле
            image_master = imageMaster, // ✨ Новое поле
            time = m.CreatedAt,
            document_url = GetFileUrl(m.DocumentUrl),
            document_name = m.DocumentName,
            document_size = m.DocumentSize,
            is_pinned = m.PinnedAt != null,
            is_mention = isMention || isReplyToMe,
            audio_stream = audioStream,
            audio_master = audioMaster,
            audio_title = m.AudioTitle,
            audio_artist = m.AudioArtist,
            audio_duration = m.AudioDuration,
            video_master = videoMaster,
            audio_size = m.AudioSize,
            audio_cover = GetFileUrl(m.AudioCover),
            forwarded_from = m.ForwardedFrom,
            is_read = m.IsRead,
            poll = string.IsNullOrEmpty(m.PollJson) ? null : JsonSerializer.Deserialize<object>(m.PollJson),
            reply_to = m.Parent != null ? new { id = m.Parent.Id, name = m.Parent.User?.Name, text = m.Parent.Body ?? "Вложение" } : null,
            reactions = m.Reactions?.GroupBy(r => r.Emoji).Select(g => new { emoji = g.Key, count = g.Count(), reacted_by_me = g.Any(r => r.UserId == myId), userIds = g.Select(x => x.UserId).ToList() })
        };
    }
    [HttpPost("stickers/import")]
    public async Task<IActionResult> ImportStickers([FromBody] ImportStickerDto dto)
    {
        // 1. Вытаскиваем имя пака из ссылки
        // Например: https://t.me/addstickers/BlueArchive82 -> BlueArchive82
        var packName = dto.Link.Split('/').LastOrDefault();
        if (string.IsNullOrEmpty(packName))
            return BadRequest(new { message = "Неверная ссылка" });

        // ⚠️ ВСТАВЬ СЮДА ТОКЕН СВОЕГО БОТА (получи у @BotFather)
        string telegramBotToken = "8299297981:AAHRY81xYYGsfWYbWy2sSGIKTBeA_kePgD4";

        using var httpClient = new HttpClient();

        try
        {
            // 2. Спрашиваем Телеграм про этот пак
            var response = await httpClient.GetAsync($"https://api.telegram.org/bot{telegramBotToken}/getStickerSet?name={packName}");
            var jsonStr = await response.Content.ReadAsStringAsync();

            using var jsonDoc = JsonDocument.Parse(jsonStr);
            var root = jsonDoc.RootElement;

            // Если Телеграм ответил "false", значит пак не найден или токен неверный
            if (!root.GetProperty("ok").GetBoolean())
            {
                return BadRequest(new { message = $"Ошибка Telegram: {root.GetProperty("description").GetString()}" });
            }

            var stickersCount = root.GetProperty("result").GetProperty("stickers").GetArrayLength();

            // Пока просто возвращаем успех и количество стикеров для теста
            return Ok(new
            {
                message = "Успешно!",
                packName = packName,
                totalStickers = stickersCount
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Ошибка связи с Telegram: " + ex.Message });
        }
    }

    // Класс для DTO (можно положить в конец файла)
    public class ImportStickerDto
    {
        public string Link { get; set; } = string.Empty;
    }
}


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
