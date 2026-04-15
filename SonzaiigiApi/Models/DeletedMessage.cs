using System.ComponentModel.DataAnnotations;

namespace SonzaiigiApi.Models;

public class DeletedMessage
{
    [Key]
    public int Id { get; set; }

    public int MessageId { get; set; }
    public Message Message { get; set; } = null!;

    public int UserId { get; set; }
    public User User { get; set; } = null!;

    public DateTime DeletedAt { get; set; } = DateTime.UtcNow;
}
