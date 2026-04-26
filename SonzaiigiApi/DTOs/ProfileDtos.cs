namespace SonzaiigiApi.DTOs;

// Обрати внимание: IFormFile - это аналог файла в C#
public class UpdateProfileDto
{
    public string Name { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public string? Bio { get; set; } // ✨ НОВОЕ ПОЛЕ
    public IFormFile? Avatar { get; set; }
}

public class UpdatePasswordDto
{
    public string Current_Password { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}
