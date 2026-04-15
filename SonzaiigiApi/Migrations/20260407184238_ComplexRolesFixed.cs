using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class ComplexRolesFixed : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_ConversationMembers_Users_UserId1",
                table: "ConversationMembers");

            migrationBuilder.DropIndex(
                name: "IX_ConversationMembers_UserId1",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "UserId1",
                table: "ConversationMembers");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "UserId1",
                table: "ConversationMembers",
                type: "integer",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_ConversationMembers_UserId1",
                table: "ConversationMembers",
                column: "UserId1");

            migrationBuilder.AddForeignKey(
                name: "FK_ConversationMembers_Users_UserId1",
                table: "ConversationMembers",
                column: "UserId1",
                principalTable: "Users",
                principalColumn: "Id");
        }
    }
}
