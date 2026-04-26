using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class AddIsMutedInGroup : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "IsMuted",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "IsMuted",
                table: "ConversationMembers");
        }
    }
}
