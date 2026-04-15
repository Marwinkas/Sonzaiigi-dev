using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class AddVideoHls : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "VideoHls",
                table: "Messages",
                type: "text",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "VideoHls",
                table: "Messages");
        }
    }
}
