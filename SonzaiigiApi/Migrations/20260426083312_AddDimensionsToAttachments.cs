using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class AddDimensionsToAttachments : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "Height",
                table: "MessageAttachment",
                type: "integer",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "Width",
                table: "MessageAttachment",
                type: "integer",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Height",
                table: "MessageAttachment");

            migrationBuilder.DropColumn(
                name: "Width",
                table: "MessageAttachment");
        }
    }
}
