using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class AddGroupRoles2 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Conversations_Users_UserId",
                table: "Conversations");

            migrationBuilder.DropIndex(
                name: "IX_Conversations_UserId",
                table: "Conversations");

            migrationBuilder.DropIndex(
                name: "IX_ConversationMembers_ConversationId",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "UserId",
                table: "Conversations");

            migrationBuilder.AddColumn<int>(
                name: "UserId1",
                table: "ConversationMembers",
                type: "integer",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_ConversationMembers_ConversationId_UserId",
                table: "ConversationMembers",
                columns: new[] { "ConversationId", "UserId" },
                unique: true);

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

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_ConversationMembers_Users_UserId1",
                table: "ConversationMembers");

            migrationBuilder.DropIndex(
                name: "IX_ConversationMembers_ConversationId_UserId",
                table: "ConversationMembers");

            migrationBuilder.DropIndex(
                name: "IX_ConversationMembers_UserId1",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "UserId1",
                table: "ConversationMembers");

            migrationBuilder.AddColumn<int>(
                name: "UserId",
                table: "Conversations",
                type: "integer",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Conversations_UserId",
                table: "Conversations",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "IX_ConversationMembers_ConversationId",
                table: "ConversationMembers",
                column: "ConversationId");

            migrationBuilder.AddForeignKey(
                name: "FK_Conversations_Users_UserId",
                table: "Conversations",
                column: "UserId",
                principalTable: "Users",
                principalColumn: "Id");
        }
    }
}
