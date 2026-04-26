using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using StackExchange.Redis;
using Amazon.S3;
using Amazon.S3.Model;
using System.Security.Claims;
namespace SonzaiigiApi.Controllers;

[ApiController]
[Route("api/app")]
public class AppUpdateController : ControllerBase
{
    private readonly IConnectionMultiplexer _redis;
    private readonly AmazonS3Client _s3Client;

    public AppUpdateController(IConnectionMultiplexer redis)
    {
        _redis = redis;
        var s3Config = new AmazonS3Config { ServiceURL = "https://f7601d9aad1781510d16d7d8b23115c1.r2.cloudflarestorage.com", ForcePathStyle = true, AuthenticationRegion = "us-east-1" };
        _s3Client = new AmazonS3Client("5d5fbed17394421e895304f94c018e12", "f5d29455dd07539e769356f4596b89f804fd5a50ca250272bc1a3208fbada811", s3Config);
    }

    [HttpGet("check")]
    public async Task<IActionResult> CheckUpdate()
    {
        var db = _redis.GetDatabase();
        var version = await db.StringGetAsync("app_latest_version");
        var url = await db.StringGetAsync("app_apk_url");

        return Ok(new
        {
            version = version.HasValue ? version.ToString() : "1.0.0",
            url = url.HasValue ? url.ToString() : ""
        });
    }

    [HttpPost("upload")]
    [Authorize] // Загружать могут только авторизованные
    [DisableRequestSizeLimit]
    public async Task<IActionResult> UploadApk([FromForm] string version, [FromForm] IFormFile apk)
    {
        if (apk == null || apk.Length == 0) return BadRequest("Файл не выбран");
        // ✨ ЗАЩИТА: Загружать АПК может только админ (замените 1 на свой ID в базе)
        var myId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "0");
        if (myId != 3) return Forbid();

        var fileName = $"dev/updates/sonzaiigi_v{version.Replace(".", "_")}_{Guid.NewGuid():N}.apk";

        using var stream = apk.OpenReadStream();
        await _s3Client.PutObjectAsync(new PutObjectRequest
        {
            BucketName = "artworks",
            Key = fileName,
            InputStream = stream,
            ContentType = "application/vnd.android.package-archive",
            DisablePayloadSigning = true
        });

        var fullUrl = $"https://cdn.sonzaiigi.com/{fileName}";
        var cleanVersion = version.Trim();
        var db = _redis.GetDatabase();
        await db.StringSetAsync("app_latest_version", cleanVersion); // Сохраняем чистую строку
        await db.StringSetAsync("app_apk_url", fullUrl.Trim());


        return Ok(new { success = true, version = cleanVersion, url = fullUrl });
    }
}