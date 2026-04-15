using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json;

namespace SonzaiigiApi.Models;

public class GroupRole
{
    [Key]
    public int Id { get; set; }
    public int ConversationId { get; set; }
    public Conversation Conversation { get; set; } = null!;

    public string Name { get; set; } = "Новая роль";
    public string Color { get; set; } = "#94a3b8";
    public string? Icon { get; set; }
    public bool IsMentionable { get; set; } = false;

    public int Hierarchy { get; set; } = 999;
    public bool CanEditRoleDesign { get; set; } = false;

    public string PermissionsJson { get; set; } = "{}";
    public string GrantablePermissionsJson { get; set; } = "{}";

    [NotMapped]
    public Dictionary<string, bool> Permissions
    {
        get
        {
            if (string.IsNullOrWhiteSpace(PermissionsJson)) return new Dictionary<string, bool>();
            try { return JsonSerializer.Deserialize<Dictionary<string, bool>>(PermissionsJson) ?? new Dictionary<string, bool>(); }
            catch { return new Dictionary<string, bool>(); }
        }
        set => PermissionsJson = JsonSerializer.Serialize(value);
    }

    [NotMapped]
    public Dictionary<string, bool> GrantablePermissions
    {
        get
        {
            if (string.IsNullOrWhiteSpace(GrantablePermissionsJson)) return new Dictionary<string, bool>();
            try { return JsonSerializer.Deserialize<Dictionary<string, bool>>(GrantablePermissionsJson) ?? new Dictionary<string, bool>(); }
            catch { return new Dictionary<string, bool>(); }
        }
        set => GrantablePermissionsJson = JsonSerializer.Serialize(value);
    }

    public List<ConversationMemberRole> MemberRoles { get; set; } = new();
}
