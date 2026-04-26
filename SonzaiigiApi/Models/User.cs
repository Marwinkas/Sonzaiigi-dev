namespace SonzaiigiApi.Models;

public class User
{
    public int Id { get; set; }
    public string? Name { get; set; }
    public string? Username { get; set; }
    public string Email { get; set; } = string.Empty;
    public DateTime? EmailVerifiedAt { get; set; }
    public string? PasswordHash { get; set; } // В БД будет PasswordHash, как и положено
    public string? GoogleId { get; set; }
    public string? VerificationCode { get; set; }
    public string? Avatar { get; set; }
    public string? Bio { get; set; } // ✨ НОВОЕ ПОЛЕ
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    public string? FcmToken { get; set; }
    // --- Связи (Аналог Eloquent Relations) ---
    public List<Post> Posts { get; set; } = new();
    public List<Post> LikedPosts { get; set; } = new();
    public List<ConversationMember> ConversationMembers { get; set; } = new();
    public List<Message> Messages { get; set; } = new();
    public List<FavoriteGif> FavoriteGifs { get; set; } = new();
    public List<Reaction> Reactions { get; set; } = new();

    // Самоссылающиеся списки друзей и ЧС (Магия EF Core)
    public List<User> Following { get; set; } = new();
    public List<User> Followers { get; set; } = new();
    public List<User> BlockedUsers { get; set; } = new();
    public List<User> BlockedBy { get; set; } = new();
    public List<User> MutedUsers { get; set; } = new();
    public List<User> MutedBy { get; set; } = new();
}

public class Post
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public string Title { get; set; } = string.Empty;
    public string? Description { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public List<PostImage> Images { get; set; } = new();
    public List<User> LikedByUsers { get; set; } = new();
}

public class PostImage
{
    public int Id { get; set; }
    public int PostId { get; set; }
    public Post Post { get; set; } = null!;
    public string Path { get; set; } = string.Empty;
    public int SortOrder { get; set; } = 0;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}

public class Conversation
{
    public int Id { get; set; }
    public bool IsGroup { get; set; } = false;
    public string? Name { get; set; }
    public string? Description { get; set; }
    public string? Avatar { get; set; }
    public string? InviteToken { get; set; }
    public bool IsPrivate { get; set; } = false;

    // --- НАСТРОЙКИ ГРУППЫ ---
    public int SlowMode { get; set; } = 0; // в секундах
    public bool SysMsgJoin { get; set; } = true;
    public bool SysMsgLeave { get; set; } = false;
    public bool SysMsgEdit { get; set; } = true;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    // --- СВЯЗИ ---
    public List<ConversationMember> Members { get; set; } = new();
    public List<Message> Messages { get; set; } = new();
    public List<GroupRole> Roles { get; set; } = new(); // Кастомные роли этой группы
    public List<GroupBan> BannedUsers { get; set; } = new();
    public List<GroupAuditLog> AuditLogs { get; set; } = new();
}
