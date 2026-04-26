using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json;

namespace SonzaiigiApi.Models;

public class ConversationMember
{
    [Key]
    public int Id { get; set; }

    public int ConversationId { get; set; }
    public Conversation Conversation { get; set; } = null!;

    public int UserId { get; set; }
    public User User { get; set; } = null!;

    public bool IsOwner { get; set; } = false;
    public bool IsMuted { get; set; } = false;
    // СЮДА ДОБАВИЛИ НИКНЕЙМ (он может быть null, если человек использует обычное имя)
    public string? Nickname { get; set; }
    public int LastReadMessageId { get; set; } = 0;
    // Гарантируем, что по умолчанию здесь всегда пустой JSON
    public string OverridesJson { get; set; } = "{}";

    [NotMapped]
    public Dictionary<string, bool> IndividualOverrides
    {
        get
        {
            if (string.IsNullOrWhiteSpace(OverridesJson)) return new Dictionary<string, bool>();
            try
            {
                var dict = JsonSerializer.Deserialize<Dictionary<string, bool>>(OverridesJson);
                return dict ?? new Dictionary<string, bool>();
            }
            catch
            {
                // БРОНЯ: Если в БД затесалось слово "Owner" или другой мусор, 
                // сервер не упадет с 500 ошибкой, а просто вернет пустые права
                return new Dictionary<string, bool>();
            }
        }
        set => OverridesJson = JsonSerializer.Serialize(value);
    }

    public DateTime JoinedAt { get; set; } = DateTime.UtcNow;

    public List<ConversationMemberRole> MemberRoles { get; set; } = new();
}
public class ConversationMemberRole
{
    public int MemberId { get; set; }
    public ConversationMember Member { get; set; } = null!;

    public int RoleId { get; set; }
    public GroupRole Role { get; set; } = null!;
}
public class GroupBan
{
    [Key]
    public int Id { get; set; }
    public int ConversationId { get; set; }
    public Conversation Conversation { get; set; } = null!;

    public int UserId { get; set; }
    public User User { get; set; } = null!;

    public int AdminId { get; set; } // Кто забанил
    public string? Reason { get; set; }
    public DateTime BannedAt { get; set; } = DateTime.UtcNow;
}

public class GroupAuditLog
{
    [Key]
    public int Id { get; set; }
    public int ConversationId { get; set; }
    public Conversation Conversation { get; set; } = null!;

    public int UserId { get; set; }
    public User User { get; set; } = null!; // Тот, кто совершил действие

    public string Action { get; set; } = string.Empty; // Например: "создал(а) роль 'Разработчик'"
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
