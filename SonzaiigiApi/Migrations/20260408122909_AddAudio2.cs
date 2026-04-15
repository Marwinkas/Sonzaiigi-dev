using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class AddAudio2 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "AudioArtist",
                table: "Messages",
                type: "text",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "AudioCover",
                table: "Messages",
                type: "text",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "AudioDuration",
                table: "Messages",
                type: "text",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "AudioSize",
                table: "Messages",
                type: "text",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "AudioTitle",
                table: "Messages",
                type: "text",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "AudioArtist",
                table: "Messages");

            migrationBuilder.DropColumn(
                name: "AudioCover",
                table: "Messages");

            migrationBuilder.DropColumn(
                name: "AudioDuration",
                table: "Messages");

            migrationBuilder.DropColumn(
                name: "AudioSize",
                table: "Messages");

            migrationBuilder.DropColumn(
                name: "AudioTitle",
                table: "Messages");
        }
    }
}
