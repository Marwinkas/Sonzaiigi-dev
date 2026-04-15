namespace SonzaiigiApi.Models;

public class Message
{
    public int Id { get; set; }
    public int ConversationId { get; set; }
    public Conversation Conversation { get; set; } = null!;

    public int UserId { get; set; }
    public User User { get; set; } = null!;

    public string Body { get; set; } = string.Empty;
    public string? Image { get; set; }
    public string? Audio { get; set; }
    public string? GifUrl { get; set; }
    public string? AudioTitle { get; set; }
    public string? AudioArtist { get; set; }
    public string? AudioDuration { get; set; } // Храним как строку "05:09"
    public string? AudioSize { get; set; }     // Храним как строку "4.7 MB"
    public string? AudioCover { get; set; }    // Путь к файлу обложки в R2
    public string? ForwardedFrom { get; set; }
    public string? VideoHls { get; set; }
    // Добавляем поля для обычных файлов (Документов)
    public string? DocumentUrl { get; set; }
    public string? DocumentName { get; set; }
    public string? DocumentSize { get; set; }

    // Добавляем поле для опросов (будем хранить JSON)
    public string? PollJson { get; set; }

    // Для ответов на сообщения (nullOnDelete)
    public int? ParentId { get; set; }
    public Message? Parent { get; set; }
    public List<Message> Replies { get; set; } = new();

    public DateTime? PinnedAt { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public List<Reaction> Reactions { get; set; } = new();

    public bool IsRead { get; set; } = false;
    public ICollection<DeletedMessage> DeletedMessages { get; set; } = new List<DeletedMessage>();
}

public class Reaction
{
    public int Id { get; set; }
    public int MessageId { get; set; }
    public Message Message { get; set; } = null!;
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public string Emoji { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}

public class FavoriteGif
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public string GifUrl { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}
