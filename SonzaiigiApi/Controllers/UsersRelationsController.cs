using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SonzaiigiApi.Data;
using SonzaiigiApi.Models;
using System.Security.Claims;
using StackExchange.Redis;
using System.Text.Json;

namespace SonzaiigiApi.Controllers;

[ApiController]
[Route("api")]
[Authorize]
public class UsersRelationsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IConnectionMultiplexer _redis;

    public UsersRelationsController(AppDbContext context, IConnectionMultiplexer redis)
    {
        _context = context;
        _redis = redis;
    }

    [HttpPost("users/{id}/block")]
    public async Task<IActionResult> ToggleBlock(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        if (myId == id) return BadRequest("Нельзя заблокировать самого себя");

        var me = await _context.Users
            .Include(u => u.BlockedUsers)
            .Include(u => u.Following)
            .Include(u => u.Followers)
            .FirstAsync(u => u.Id == myId);

        var target = await _context.Users
            .Include(u => u.Followers)
            .FirstOrDefaultAsync(u => u.Id == id);

        if (target == null) return NotFound();

        bool isNowBlocked;
        var db = _redis.GetDatabase();

        if (me.BlockedUsers.Any(u => u.Id == id))
        {
            me.BlockedUsers.Remove(target);
            isNowBlocked = false;
        }
        else
        {
            me.BlockedUsers.Add(target);
            isNowBlocked = true;

            me.Following.Remove(target);
            target.Followers.Remove(me);

            var followLink = await _context.Users.Include(u => u.Following).Where(u => u.Id == id).FirstOrDefaultAsync();
            if (followLink != null)
            {
                var meInHisFollowing = followLink.Following.FirstOrDefault(f => f.Id == myId);
                if (meInHisFollowing != null) followLink.Following.Remove(meInHisFollowing);
            }
        }

        await _context.SaveChangesAsync();

        var conversation = await _context.Conversations
            .Include(c => c.Messages)
            .Where(c => !c.IsGroup && c.Members.Any(u => u.UserId == myId) && c.Members.Any(u => u.UserId == id))
            .FirstOrDefaultAsync();

        if (conversation != null)
        {
            if (isNowBlocked)
            {
                if (!conversation.Messages.Any())
                {
                    _context.Conversations.Remove(conversation);
                    await _context.SaveChangesAsync();

                    // ИСПРАВЛЕНО: RedisChannel.Literal
                    await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new { type = "chat_removed", chat_id = conversation.Id }));
                }
                else
                {
                    // ИСПРАВЛЕНО: RedisChannel.Literal
                    await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new
                    {
                        type = "chat_relation_update",
                        chat_id = conversation.Id,
                        can_reply = false
                    }));
                }
            }
            else
            {
                // ИСПРАВЛЕНО: RedisChannel.Literal
                await db.PublishAsync(RedisChannel.Literal("chat_events"), JsonSerializer.Serialize(new
                {
                    type = "chat_relation_update",
                    chat_id = conversation.Id,
                    can_reply = false
                }));
            }
        }

        return Ok(new { is_blocked = isNowBlocked });
    }

    [HttpPost("users/{id}/mute")]
    public async Task<IActionResult> ToggleMute(int id)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
        if (myId == id) return BadRequest();

        var me = await _context.Users.Include(u => u.MutedUsers).FirstAsync(u => u.Id == myId);
        var target = await _context.Users.FindAsync(id);
        if (target == null) return NotFound();

        bool isMuted;
        if (me.MutedUsers.Any(u => u.Id == id))
        {
            me.MutedUsers.Remove(target);
            isMuted = false;
        }
        else
        {
            me.MutedUsers.Add(target);
            isMuted = true;
        }

        await _context.SaveChangesAsync();
        return Ok(new { is_muted = isMuted });
    }

    [HttpPost("messages/toggle-favorite-gif")]
    public async Task<IActionResult> ToggleFavoriteGif([FromBody] ToggleGifDto dto)
    {
        var myId = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        var existingGif = await _context.FavoriteGifs.FirstOrDefaultAsync(g => g.UserId == myId && g.GifUrl == dto.Gif_Url);

        if (existingGif != null)
        {
            _context.FavoriteGifs.Remove(existingGif);
        }
        else
        {
            _context.FavoriteGifs.Add(new FavoriteGif { UserId = myId, GifUrl = dto.Gif_Url, UpdatedAt = DateTime.UtcNow });
        }

        await _context.SaveChangesAsync();

        var gifs = await _context.FavoriteGifs.Where(g => g.UserId == myId).OrderByDescending(g => g.UpdatedAt).Select(g => g.GifUrl).ToListAsync();

        return Ok(new { favorite_gifs = gifs });
    }
}

public class ToggleGifDto
{
    [System.Text.Json.Serialization.JsonPropertyName("gif_url")]
    public string Gif_Url { get; set; } = string.Empty;
}
