namespace SonzaiigiApi.Models;

public class Follow
{
    public int FollowerId { get; set; }
    public int FollowedId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
public class Block
{
    public int BlockerId { get; set; }
    public int BlockedId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
public class Mute
{
    public int UserId { get; set; }
    public int MutedUserId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
