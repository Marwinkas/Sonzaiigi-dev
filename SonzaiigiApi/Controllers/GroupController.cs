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

namespace SonzaiigiApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class GroupsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly AmazonS3Client _s3Client;
    private readonly IConnectionMultiplexer _redis;

    public GroupsController(AppDbContext context, IConnectionMultiplexer redis)
    {
        _context = context;
        _redis = redis;
        var s3Config = new AmazonS3Config { ServiceURL = "https://f7601d9aad1781510d16d7d8b23115c1.r2.cloudflarestorage.com", ForcePathStyle = true, AuthenticationRegion = "us-east-1" };
        _s3Client = new AmazonS3Client("5d5fbed17394421e895304f94c018e12", "f5d29455dd07539e769356f4596b89f804fd5a50ca250272bc1a3208fbada811", s3Config);
    }

    private string? GetFileUrl(string? path)
    {
        if (string.IsNullOrEmpty(path)) return null;
        if (path.StartsWith("http")) return path;
        return $"https://cdn.sonzaiigi.com/{path.TrimStart('/')}";
    }

    private async Task LogAction(int chatId, int userId, string action)
    {
        _context.GroupAuditLogs.Add(new GroupAuditLog { ConversationId = chatId, UserId = userId, Action = action });
        await _context.SaveChangesAsync();
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
    [HttpPost("{id}/toggle-mute")]
    public async Task<IActionResult> ToggleGroupMute(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var member = await _context.ConversationMembers
            .FirstOrDefaultAsync(m => m.ConversationId == id && m.UserId == myId);

        if (member == null) return NotFound();

        member.IsMuted = !member.IsMuted; // Нужно добавить колонку IsMuted в таблицу ConversationMember
        await _context.SaveChangesAsync();

        return Ok(new { is_muted = member.IsMuted });
    }
    [HttpGet("{id}")] // Полный путь будет api/groups/{id}
    public async Task<IActionResult> GetGroupInfo(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        var group = await _context.Conversations
            .Include(c => c.Members).ThenInclude(m => m.User)
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .Include(c => c.Roles)
            .Include(c => c.BannedUsers).ThenInclude(b => b.User)
            .Include(c => c.AuditLogs).ThenInclude(a => a.User)
            .FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);

        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || group.BannedUsers.Any(b => b.UserId == myId)) return Forbid();

        return Ok(new
        {
            id = group.Id,
            name = group.Name,
            description = group.Description,
            isPrivate = group.IsPrivate,
            avatar = GetFileUrl(group.Avatar),
            invite_token = group.IsPrivate ? null : group.InviteToken,
            sysMsgs = new { join = group.SysMsgJoin, leave = group.SysMsgLeave, edit = group.SysMsgEdit },
            slowMode = group.SlowMode,
            isMuted = myMember.IsMuted,
            roles = group.Roles.OrderBy(r => r.Hierarchy).Select(r => new {
                id = r.Id,
                name = r.Name,
                color = r.Color,
                isMuted = myMember.IsMuted,
                icon = GetFileUrl(r.Icon),
                mentionable = r.IsMentionable,
                hierarchy = r.Hierarchy,
                permissions = r.Permissions,
                grantablePermissions = r.GrantablePermissions,
                canEditRoleDesign = r.CanEditRoleDesign
            }),

            members = group.Members.Select(m => new {
                id = m.UserId,
                name = m.User.Name,
                nickname = m.Nickname, // Заодно передаем никнейм на фронт
                avatar = GetFileUrl(m.User.Avatar),
                roleIds = m.MemberRoles.Select(mr => mr.RoleId).ToList(),
                individualOverrides = m.IndividualOverrides,
                isOwner = m.IsOwner
            }),

            bannedUsers = group.BannedUsers.Select(b => new {
                id = b.UserId,
                name = b.User.Name,
                avatar = GetFileUrl(b.User.Avatar),
                reason = b.Reason,
                date = b.BannedAt.ToString("dd.MM.yyyy")
            }),

            auditLog = group.AuditLogs.OrderByDescending(a => a.CreatedAt).Take(50).Select(a => new {
                id = a.Id,
                user = a.User.Name,
                action = a.Action,
                time = a.CreatedAt.ToString("dd.MM.yyyy HH:mm")
            })
        });
    }
  
    [HttpPatch("{id}")]
    public async Task<IActionResult> UpdateGroup(int id, [FromForm] UpdateGroupDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);
        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);

        // Отдельно проверим права на изменение ссылки, если она есть в запросе
        if (!string.IsNullOrEmpty(dto.InviteToken))
        {
            if (myMember == null || !HasPermission(myMember, "manageLinks"))
                return StatusCode(403, new { message = "Нет прав изменять ссылку" });

            bool linkExists = await _context.Conversations.AnyAsync(c => c.InviteToken == dto.InviteToken && c.Id != id);
            if (linkExists) return BadRequest(new { message = "Такая ссылка уже занята, попробуй другую!" });

            group.InviteToken = dto.InviteToken;
        }
        if (dto.IsPrivate.HasValue) group.IsPrivate = dto.IsPrivate.Value;
        // Права на остальные настройки
        if (dto.Name != null || dto.Description != null || dto.Avatar != null || dto.SlowMode.HasValue || dto.SysMsgJoin.HasValue || dto.SysMsgLeave.HasValue || dto.SysMsgEdit.HasValue)
        {
            if (myMember == null || !HasPermission(myMember, "manageChat")) return Forbid();
        }

        if (dto.Name != null) group.Name = dto.Name;
        if (dto.Description != null) group.Description = dto.Description;
        if (dto.SlowMode.HasValue) group.SlowMode = dto.SlowMode.Value;
        if (dto.SysMsgJoin.HasValue) group.SysMsgJoin = dto.SysMsgJoin.Value;
        if (dto.SysMsgLeave.HasValue) group.SysMsgLeave = dto.SysMsgLeave.Value;
        if (dto.SysMsgEdit.HasValue) group.SysMsgEdit = dto.SysMsgEdit.Value;

        if (dto.Avatar != null)
        {
            using var img = await Image.LoadAsync(dto.Avatar.OpenReadStream());
            img.Mutate(x => x.Resize(new ResizeOptions { Size = new Size(512, 512), Mode = ResizeMode.Crop }));
            using var ms = new MemoryStream();
            await img.SaveAsWebpAsync(ms, new WebpEncoder { Quality = 80 });
            ms.Position = 0;
            var fileName = $"dev/groups/{Guid.NewGuid():N}.webp";
            await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = fileName, InputStream = ms, ContentType = "image/webp", DisablePayloadSigning = true });
            group.Avatar = fileName;
        }

        await _context.SaveChangesAsync();
        await LogAction(group.Id, myId, "обновил(а) настройки группы");

        var wsPayload = new { type = "group_updated", chat_id = group.Id, description = group.Description, name = group.Name, avatar = GetFileUrl(group.Avatar) };
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(wsPayload));

        return Ok(new { success = true });
    }
    [HttpDelete("{id}/roles/{roleId}")]
    public async Task<IActionResult> DeleteRole(int id, int roleId)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .Include(c => c.Roles)
            .FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);

        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || !HasPermission(myMember, "manageRoles")) return Forbid();

        var role = group.Roles.FirstOrDefault(r => r.Id == roleId);
        if (role == null) return NotFound();

        // Защита: нельзя удалить системную роль владельца (0)
        if (role.Hierarchy == 0)
            return BadRequest(new { message = "Нельзя удалить системную роль владельца" });

        // Защита: админ не может удалить роль, которая выше или равна его собственной
        if (!myMember.IsOwner)
        {
            var myHighestRole = group.Roles.Where(r => myMember.MemberRoles.Select(mr => mr.RoleId).Contains(r.Id)).Min(r => r.Hierarchy);
            if (myHighestRole >= role.Hierarchy)
                return StatusCode(403, new { message = "Вы не можете удалить эту роль, так как она выше или равна вашей" });
        }

        _context.GroupRoles.Remove(role);
        await _context.SaveChangesAsync();
        await LogAction(id, myId, $"удалил(а) роль '{role.Name}'");

        // Уведомляем участников чата, чтобы у них обновился список ролей
        var wsPayload = new { type = "chat_roles_updated", chat_id = id };
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(wsPayload));

        return Ok(new { success = true });
    }
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteGroup(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.Members).FirstOrDefaultAsync(c => c.Id == id);

        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || !myMember.IsOwner) return Forbid("Только владелец может удалить группу");

        _context.Conversations.Remove(group);
        await _context.SaveChangesAsync();

        // Уведомляем всех через Redis/WebSocket, что чат удален
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_removed", chat_id = id }));

        return Ok();
    }
    [HttpPost("{id}/reset-link")]
    public async Task<IActionResult> ResetLink(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);
        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || !HasPermission(myMember, "manageLinks")) return Forbid();

        group.InviteToken = Guid.NewGuid().ToString("N").Substring(0, 15);
        await _context.SaveChangesAsync();
        await LogAction(group.Id, myId, "сбросил(а) ссылку-приглашение");

        return Ok(new { new_token = group.InviteToken });
    }

    // --- НОВЫЙ МЕТОД ДЛЯ СМЕНЫ НИКНЕЙМА ---
    [HttpPatch("{id}/members/{userId}/nickname")]
    public async Task<IActionResult> UpdateNickname(int id, int userId, [FromBody] UpdateNicknameDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);

        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        var targetMember = group.Members.FirstOrDefault(m => m.UserId == userId);

        if (myMember == null || targetMember == null) return NotFound();

        if (myId == userId)
        {
            if (!HasPermission(myMember, "changeNickname")) return StatusCode(403, new { message = "У тебя нет прав менять свой никнейм" });
        }
        else
        {
            if (!HasPermission(myMember, "manageNicknames")) return StatusCode(403, new { message = "У тебя нет прав менять чужие никнеймы" });
            if (targetMember.IsOwner && !myMember.IsOwner) return StatusCode(403, new { message = "Нельзя изменить никнейм владельцу" });
        }

        // Если прислали пустое значение, никнейм просто удалится и будет показываться обычное имя
        targetMember.Nickname = string.IsNullOrWhiteSpace(dto.Nickname) ? null : dto.Nickname;
        await _context.SaveChangesAsync();

        await LogAction(id, myId, $"изменил(а) никнейм участнику ID {userId}");
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "member_updated", chat_id = id, target_user_id = userId, nickname = targetMember.Nickname }));

        return Ok(new { success = true });
    }

    [HttpPost("{id}/members/{userId}/{actionType}")]
    public async Task<IActionResult> ManageMember(int id, int userId, string actionType)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);

        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        var targetMember = group.Members.FirstOrDefault(m => m.UserId == userId);

        if (myMember == null || targetMember == null) return NotFound();
        if (targetMember.IsOwner) return Forbid("Нельзя применить действие к владельцу");

        var db = _redis.GetDatabase();

        switch (actionType)
        {
            case "kick":
                if (!HasPermission(myMember, "kickMembers")) return Forbid();
                _context.ConversationMembers.Remove(targetMember);
                await LogAction(group.Id, myId, $"выгнал(а) пользователя ID {userId}");
                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_removed", chat_id = group.Id, target_user_id = userId }));
                break;

            case "ban":
                if (!HasPermission(myMember, "banMembers")) return Forbid();
                _context.ConversationMembers.Remove(targetMember);
                _context.GroupBans.Add(new GroupBan { ConversationId = id, UserId = userId, AdminId = myId, Reason = "Нарушение правил" });
                await LogAction(group.Id, myId, $"забанил(а) пользователя ID {userId}");
                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_removed", chat_id = group.Id, target_user_id = userId }));
                break;

            default: return BadRequest();
        }

        await _context.SaveChangesAsync();
        await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "member_joined", chat_id = group.Id }));
        return Ok(new { success = true });
    }

    [HttpPost("{id}/leave")]
    public async Task<IActionResult> LeaveGroup(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.Members).FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);
        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember != null)
        {
            if (myMember.IsOwner) return BadRequest("Владелец не может покинуть группу. Передайте права или удалите её.");

            _context.ConversationMembers.Remove(myMember);
            await LogAction(group.Id, myId, "покинул(а) группу");
            await _context.SaveChangesAsync();

            var db = _redis.GetDatabase();
            await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_removed", chat_id = id, target_user_id = myId }));
            await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "member_joined", chat_id = id }));

            if (group.SysMsgLeave)
            {
                var me = await _context.Users.FindAsync(myId);
                var msg = new Message { ConversationId = group.Id, UserId = myId, Body = "🚪 Покинул(а) чат", CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow };
                _context.Messages.Add(msg);
                await _context.SaveChangesAsync();

                var wsPayload = new
                {
                    type = "new_message",
                    chat_id = group.Id,
                    message = new
                    {
                        id = msg.Id,
                        text = msg.Body,
                        senderId = myId,
                        senderName = me?.Name ?? "Unknown",
                        senderAvatar = GetFileUrl(me?.Avatar),
                        time = msg.CreatedAt.ToString("HH:mm"),
                        is_read = false
                    }
                };
                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(wsPayload));
            }
        }
        return Ok();
    }

    [HttpPost("{id}/roles")]
    [HttpPatch("{id}/roles/{roleId}")]
    public async Task<IActionResult> SaveRole(int id, int? roleId, [FromForm] RoleDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations
            .Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .Include(c => c.Roles)
            .FirstOrDefaultAsync(c => c.Id == id && c.IsGroup);

        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || !HasPermission(myMember, "manageRoles")) return Forbid();

        GroupRole role;
        if (roleId.HasValue)
        {
            role = group.Roles.FirstOrDefault(r => r.Id == roleId.Value);
            if (role == null) return NotFound();
            if (!myMember.IsOwner && group.Roles.Where(r => myMember.MemberRoles.Select(mr => mr.RoleId).Contains(r.Id)).Min(r => r.Hierarchy) >= role.Hierarchy)
                return Forbid("Вы не можете редактировать роль выше вашей");
        }
        else
        {
            role = new GroupRole { ConversationId = id, Hierarchy = group.Roles.Count };
            _context.GroupRoles.Add(role);
        }

        role.Name = dto.Name;
        role.Color = dto.Color;
        role.IsMentionable = dto.IsMentionable;
        role.CanEditRoleDesign = dto.CanEditRoleDesign;
        role.PermissionsJson = dto.PermissionsJson;
        role.GrantablePermissionsJson = dto.GrantablePermissionsJson;

        if (dto.Icon != null)
        {
            using var img = await Image.LoadAsync(dto.Icon.OpenReadStream());
            img.Mutate(x => x.Resize(new ResizeOptions { Size = new Size(128, 128), Mode = ResizeMode.Crop }));
            using var ms = new MemoryStream();
            await img.SaveAsWebpAsync(ms, new WebpEncoder { Quality = 80 });
            ms.Position = 0;
            var fileName = $"dev/roles/{Guid.NewGuid():N}.webp";
            await _s3Client.PutObjectAsync(new PutObjectRequest { BucketName = "artworks", Key = fileName, InputStream = ms, ContentType = "image/webp", DisablePayloadSigning = true });
            role.Icon = fileName;
        }

        await _context.SaveChangesAsync();
        await LogAction(id, myId, roleId.HasValue ? $"изменил(а) роль '{role.Name}'" : $"создал(а) роль '{role.Name}'");

        // ✨ ВОТ ЭТИ СТРОКИ: Отправляем уведомление всем в чате, чтобы они обновили свои права
        var wsPayload = new { type = "chat_roles_updated", chat_id = id };
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(wsPayload));

        return Ok(new { success = true });
    }

    [HttpPatch("{id}/roles/reorder")]
    public async Task<IActionResult> ReorderRoles(int id, [FromBody] ReorderRolesDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.Roles).Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == id);
        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || !HasPermission(myMember, "manageRoles")) return Forbid();

        for (int i = 0; i < dto.RoleIds.Count; i++)
        {
            var role = group.Roles.FirstOrDefault(r => r.Id == dto.RoleIds[i]);
            if (role != null && role.Hierarchy != 0)
            {
                role.Hierarchy = i;
            }
        }
        await _context.SaveChangesAsync();
        return Ok(new { success = true });
    }

    [HttpPatch("{id}/members/{userId}")]
    public async Task<IActionResult> UpdateMemberRoles(int id, int userId, [FromBody] UpdateMemberDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.Members).ThenInclude(m => m.MemberRoles).FirstOrDefaultAsync(c => c.Id == id);
        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        var targetMember = group.Members.FirstOrDefault(m => m.UserId == userId);

        if (myMember == null || targetMember == null) return NotFound();
        if (!HasPermission(myMember, "manageRoles")) return Forbid();
        if (targetMember.IsOwner) return Forbid();

        targetMember.MemberRoles.Clear();
        foreach (var roleId in dto.RoleIds)
        {
            if (roleId != 1) targetMember.MemberRoles.Add(new ConversationMemberRole { RoleId = roleId });
        }

        targetMember.OverridesJson = dto.OverridesJson;
        await _context.SaveChangesAsync();
        await LogAction(id, myId, $"изменил(а) права участнику ID {userId}");

        var reloadedMember = await _context.ConversationMembers
            .Include(m => m.MemberRoles).ThenInclude(mr => mr.Role)
            .FirstAsync(m => m.ConversationId == id && m.UserId == userId);

        var updatedPerms = new
        {
            sendMessages = HasPermission(reloadedMember, "sendMessages"),
            attachFiles = HasPermission(reloadedMember, "attachFiles"),
            addReactions = HasPermission(reloadedMember, "addReactions"),
            canForward = HasPermission(reloadedMember, "canForward"),
            pinMessages = HasPermission(reloadedMember, "pinMessages"),
            deleteOthersMessages = HasPermission(reloadedMember, "deleteOthersMessages")
        };

        var wsPayload = new
        {
            type = "permissions_updated",
            chat_id = id,
            target_user_id = userId,
            permissions = updatedPerms
        };
        await _redis.GetDatabase().PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(wsPayload));

        return Ok(new { success = true });
    }

    [HttpPost("{id}/bans/{userId}/unban")]
    public async Task<IActionResult> UnbanUser(int id, int userId)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var group = await _context.Conversations.Include(c => c.BannedUsers).Include(c => c.Members).ThenInclude(m => m.MemberRoles).ThenInclude(mr => mr.Role).FirstOrDefaultAsync(c => c.Id == id);
        if (group == null) return NotFound();

        var myMember = group.Members.FirstOrDefault(m => m.UserId == myId);
        if (myMember == null || !HasPermission(myMember, "banMembers")) return Forbid();

        var banRecord = group.BannedUsers.FirstOrDefault(b => b.UserId == userId);
        if (banRecord != null)
        {
            _context.GroupBans.Remove(banRecord);
            await LogAction(id, myId, $"разбанил(а) пользователя ID {userId}");
            await _context.SaveChangesAsync();
        }

        return Ok(new { success = true });
    }
}

public class UpdateGroupDto
{
    public string? Name { get; set; }
    public string? Description { get; set; }
    public IFormFile? Avatar { get; set; }
    public string? InviteToken { get; set; }
    public bool? IsPrivate { get; set; }
    public int? SlowMode { get; set; }
    public bool? SysMsgJoin { get; set; }
    public bool? SysMsgLeave { get; set; }
    public bool? SysMsgEdit { get; set; }
}

public class RoleDto
{
    public string Name { get; set; } = string.Empty;
    public string Color { get; set; } = string.Empty;
    public bool IsMentionable { get; set; }
    public bool CanEditRoleDesign { get; set; }
    public string PermissionsJson { get; set; } = "{}";
    public string GrantablePermissionsJson { get; set; } = "{}";
    public IFormFile? Icon { get; set; }
}

public class UpdateMemberDto
{
    public List<int> RoleIds { get; set; } = new();
    public string OverridesJson { get; set; } = "{}";
}

public class ReorderRolesDto
{
    public List<int> RoleIds { get; set; } = new();
}

public class UpdateNicknameDto
{
    public string? Nickname { get; set; }
}
