using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SonzaiigiApi.Data;
using SonzaiigiApi.DTOs;
using SonzaiigiApi.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace SonzaiigiApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _context;
    // ВАЖНО: этот ключ должен в точности совпадать с тем, что в Program.cs!
    private readonly string _jwtKey = "super_secret_key_sonzaiigi_must_be_long_enough_1234567890!";

    public AuthController(AppDbContext context)
    {
        _context = context;
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterDto dto)
    {
        // 1. Проверяем, нет ли уже такого email
        if (await _context.Users.AnyAsync(u => u.Email == dto.Email))
            return BadRequest(new { message = "Email уже используется" });

        // 2. Проверяем, свободен ли Username (уникальность)
        if (await _context.Users.AnyAsync(u => u.Username == dto.Username))
            return BadRequest(new { message = "Такой @username уже занят" });

        // 3. Создаем пользователя с данными от фронтенда
        var user = new User
        {
            Name = dto.Name,
            Username = dto.Username,
            Email = dto.Email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        // 4. Сохраняем в БД
        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        // 5. Генерируем токен и отдаем фронтенду
        var token = GenerateJwtToken(user);
        return Ok(new { token, user = new { user.Id, user.Email, user.Name, user.Username, user.Avatar } });
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginDto dto)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email);

        // Проверяем пароль. Обрати внимание, мы добавили проверку на null, 
        // так как у Google-пользователей пароля в базе не будет.
        if (user == null || user.PasswordHash == null || !BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash))
            return Unauthorized(new { message = "Неверный email или пароль" });

        var token = GenerateJwtToken(user);
        return Ok(new { token, user = new { user.Id, user.Email, user.Name, user.Username, user.Avatar } });
    }

    // Вспомогательный метод создания "билетика"
    private string GenerateJwtToken(User user)
    {
        var tokenHandler = new JwtSecurityTokenHandler();
        var key = Encoding.UTF8.GetBytes(_jwtKey);

        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Email, user.Email)
            }),
            Expires = DateTime.UtcNow.AddDays(7), // Токен действителен 7 дней
            SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
        };

        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
}
