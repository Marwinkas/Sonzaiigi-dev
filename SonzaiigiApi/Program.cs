using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SonzaiigiApi.Data;
using System.Text;
using StackExchange.Redis;
var builder = WebApplication.CreateBuilder(args);


builder.WebHost.UseUrls("http://127.0.0.1:5000");

// Подключение к PostgreSQL
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql("Host=127.0.0.1;Port=5432;Database=sonzaiigi;Username=sonzai_admin;Password=X@zLRY+RvC:gF>i~ff#=r6-N/T\\p%0&#R!pdMxKa"));
// Подключаем Redis как Singleton (одно подключение на всё приложение)
// Подключаем Redis с паролем
// Подключаем Redis с паролем
builder.Services.AddSingleton<IConnectionMultiplexer>(sp =>
    ConnectionMultiplexer.Connect("127.0.0.1:6379,password=X@zLRY+RvC:gF>i~ff#=r6-N/T\\p%0&#R!pdMxKa"));
// Настройка JWT
var jwtKey = "super_secret_key_sonzaiigi_must_be_long_enough_1234567890!"; // В будущем вынесем в appsettings.json
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey)),
            ValidateIssuer = false,
            ValidateAudience = false
        };
    });

// Добавляем поддержку контроллеров
builder.Services.AddControllers();

// Добавляем разрешение для твоего React
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend", policy =>
    {
        // Указываем оба возможных варианта запуска Vite
        policy.WithOrigins("http://localhost:5173", "http://127.0.0.1:5173")
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });
});





var app = builder.Build();
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    try
    {
        var context = services.GetRequiredService<AppDbContext>();
        // Эта строчка сама применит все миграции и создаст базу, если её нет
        await context.Database.MigrateAsync();
        Console.WriteLine("База данных успешно обновлена и готова к работе!");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Ой, что-то пошло не так при создании базы: {ex.Message}");
    }
}
// Включаем наше разрешение
app.UseCors("AllowFrontend");
app.UseAuthentication();
app.UseAuthorization();

// Включаем маршрутизацию к контроллерам
app.MapControllers();

app.Run();
