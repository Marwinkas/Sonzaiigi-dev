using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SonzaiigiApi.Data;
using SonzaiigiApi.DTOs;
using SonzaiigiApi.Models;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp.Formats.Jpeg;
using Amazon.S3;
using Amazon.S3.Model;
using System.Security.Claims;
using StackExchange.Redis;
using System.Text.Json;

namespace SonzaiigiApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ProfileController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly string _bucketName = "sonzaiigi-files";
    private readonly AmazonS3Client _s3Client;
    private readonly IConnectionMultiplexer _redis;

    public ProfileController(AppDbContext context, IConnectionMultiplexer redis)
    {
        _context = context;
        _redis = redis;
        string accessKey = "5d5fbed17394421e895304f94c018e12";
        string secretKey = "f5d29455dd07539e769356f4596b89f804fd5a50ca250272bc1a3208fbada811";
        string endpoint = "https://f7601d9aad1781510d16d7d8b23115c1.r2.cloudflarestorage.com";

        var s3Config = new AmazonS3Config
        {
            ServiceURL = endpoint,
            ForcePathStyle = true,
            AuthenticationRegion = "us-east-1"
        };
        _s3Client = new AmazonS3Client(accessKey, secretKey, s3Config);
    }

    private string? GetFileUrl(string? path)
    {
        if (string.IsNullOrEmpty(path)) return null;
        if (path.StartsWith("http")) return path;
        return $"https://cdn.sonzaiigi.com/{path.TrimStart('/')}";
    }

    [HttpGet("~/api/users/{id}/relations")]
    public async Task<IActionResult> GetRelations(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        // Подгружаем списки ЧС и Мутов
        var me = await _context.Users
            .Include(u => u.Following)
            .Include(u => u.BlockedUsers)
            .Include(u => u.MutedUsers)
            .FirstOrDefaultAsync(u => u.Id == myId);

        var target = await _context.Users.Include(u => u.Following).FirstOrDefaultAsync(u => u.Id == id);

        if (me == null || target == null) return NotFound();

        bool isFollowing = me.Following.Any(u => u.Id == id);
        bool isFollowedByThem = target.Following.Any(u => u.Id == myId);
        bool isBlocking = me.BlockedUsers.Any(u => u.Id == id);
        bool isMuted = me.MutedUsers.Any(u => u.Id == id);

        return Ok(new
        {
            is_following = isFollowing,
            is_mutual = isFollowing && isFollowedByThem,
            is_blocking = isBlocking,
            is_muted = isMuted
        });
    }

    [HttpPost("~/api/users/{id}/follow")]
    public async Task<IActionResult> ToggleFollow(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        var me = await _context.Users.Include(u => u.Following).Include(u => u.Followers).FirstOrDefaultAsync(u => u.Id == myId);
        var targetUser = await _context.Users.Include(u => u.Followers).Include(u => u.Following).FirstOrDefaultAsync(u => u.Id == id);

        if (me == null || targetUser == null) return NotFound();

        bool isCurrentlyFollowing = me.Following.Any(u => u.Id == id);

        if (isCurrentlyFollowing) me.Following.Remove(targetUser);
        else me.Following.Add(targetUser);

        await _context.SaveChangesAsync();

        bool isMutual = me.Following.Any(u => u.Id == id) && targetUser.Following.Any(u => u.Id == myId);
        var db = _redis.GetDatabase();

        var conversation = await _context.Conversations
            .Include(c => c.Messages)
            .Where(c => !c.IsGroup && c.Members.Any(u => u.UserId == myId) && c.Members.Any(u => u.UserId == id))
            .FirstOrDefaultAsync();

        if (conversation != null)
        {
            var relationPayload = new { type = "chat_relation_update", chat_id = conversation.Id, can_reply = isMutual };
            await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(relationPayload));
        }

        if (isMutual)
        {
            if (conversation == null)
            {
                conversation = new Conversation { IsGroup = false, CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };

                // ИСПРАВЛЕНО: Убрали Role = "Member"
                conversation.Members.Add(new ConversationMember { UserId = me.Id });
                conversation.Members.Add(new ConversationMember { UserId = targetUser.Id });

                _context.Conversations.Add(conversation);
                await _context.SaveChangesAsync();

                var newChatForMe = new { id = conversation.Id, is_group = false, name = targetUser.Name, avatar = GetFileUrl(targetUser.Avatar), lastMessage = "Нет сообщений", time = "", can_reply = true, user = new { Id = targetUser.Id, Name = targetUser.Name, Username = targetUser.Username } };
                var newChatForThem = new { id = conversation.Id, is_group = false, name = me.Name, avatar = GetFileUrl(me.Avatar), lastMessage = "Нет сообщений", time = "", can_reply = true, user = new { Id = me.Id, Name = me.Name, Username = me.Username } };

                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_created", target_user_id = myId, chat = newChatForMe }));
                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_created", target_user_id = targetUser.Id, chat = newChatForThem }));
            }
        }
        else
        {
            if (conversation != null && !conversation.Messages.Any())
            {
                _context.Conversations.Remove(conversation);
                await _context.SaveChangesAsync();
                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_removed", chat_id = conversation.Id }));
            }
        }

        return Ok(new { is_following = !isCurrentlyFollowing, is_mutual = isMutual });
    }

    [HttpPost]
    public async Task<IActionResult> UpdateProfile([FromForm] UpdateProfileDto dto)
    {
        var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

        var user = await _context.Users.FindAsync(userId);
        if (user == null) return NotFound();

        if (await _context.Users.AnyAsync(u => u.Email == dto.Email && u.Id != userId))
            return BadRequest(new { errors = new { email = "Этот email уже занят" } });

        if (await _context.Users.AnyAsync(u => u.Username == dto.Username && u.Id != userId))
            return BadRequest(new { errors = new { username = "Это имя пользователя уже занято" } });

        user.Name = dto.Name; user.Username = dto.Username; user.Email = dto.Email; user.UpdatedAt = DateTime.UtcNow;

        if (dto.Avatar != null)
        {
            await EnsureBucketExistsAsync();
            using var image = await Image.LoadAsync(dto.Avatar.OpenReadStream());
            image.Mutate(x => x.Resize(new ResizeOptions { Size = new Size(256, 256), Mode = ResizeMode.Crop }));
            using var ms = new MemoryStream();
            await image.SaveAsJpegAsync(ms, new JpegEncoder { Quality = 80 });
            ms.Position = 0;
            var folder = "dev/avatars";
            var fileName = $"{folder}/thumb_{Guid.NewGuid():N}.jpg";
            var putRequest = new PutObjectRequest { BucketName = "artworks", Key = fileName, InputStream = ms, ContentType = "image/jpeg", DisablePayloadSigning = true };
            await _s3Client.PutObjectAsync(putRequest);
            user.Avatar = fileName;
        }

        await _context.SaveChangesAsync();

        var wsPayload = new { type = "user_updated", user_id = user.Id, name = user.Name, avatar = GetFileUrl(user.Avatar), username = user.Username };
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(wsPayload));

        return Ok(new { user = new { user.Id, user.Email, user.Name, user.Username, user.Avatar } });
    }

    [HttpPut("password")]
    public async Task<IActionResult> UpdatePassword([FromBody] UpdatePasswordDto dto)
    {
        var userId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var user = await _context.Users.FindAsync(userId);

        if (user == null || user.PasswordHash == null || !BCrypt.Net.BCrypt.Verify(dto.Current_Password, user.PasswordHash))
            return BadRequest(new { errors = new { current_password = "Неверный текущий пароль" } });

        user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password);
        user.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return Ok();
    }

    [HttpGet("~/api/users/profile/{username}")]
    public async Task<IActionResult> GetProfile(string username)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var user = await _context.Users.Include(u => u.Followers).Include(u => u.Following).FirstOrDefaultAsync(u => u.Username == username);

        if (user == null) return NotFound();

        bool isFollowing = user.Followers.Any(f => f.Id == myId);
        bool isFollowedByThem = user.Following.Any(f => f.Id == myId);
        bool isBlocking = await _context.Users.Where(u => u.Id == myId).SelectMany(u => u.BlockedUsers).AnyAsync(b => b.Id == user.Id);

        return Ok(new
        {
            user = new { user.Id, user.Name, user.Username, avatar = GetFileUrl(user.Avatar) },
            interactions = new { isFollowing, isFollowedByThem, isBlocking }
        });
    }

    private async Task EnsureBucketExistsAsync()
    {
        try
        {
            var response = await _s3Client.ListBucketsAsync();
            if (!response.Buckets.Any(b => b.BucketName == _bucketName))
            {
                await _s3Client.PutBucketAsync(_bucketName);
                Console.WriteLine($"---> Баскет {_bucketName} успешно создан!");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"---> ОШИБКА MinIO: {ex.Message}");
            throw;
        }
    }
}
