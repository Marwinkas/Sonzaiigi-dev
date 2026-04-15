using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace SonzaiigiApi.Migrations
{
    /// <inheritdoc />
    public partial class ComplexRolesUpdate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "MembersCanForward",
                table: "Conversations");

            migrationBuilder.DropColumn(
                name: "CanBanUsers",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "CanChangeInfo",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "CanChangeLink",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "CanDeleteMessages",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "CanPinMessages",
                table: "ConversationMembers");

            migrationBuilder.DropColumn(
                name: "CanPromoteAdmins",
                table: "ConversationMembers");

            migrationBuilder.RenameColumn(
                name: "MembersCanSendMessages",
                table: "Conversations",
                newName: "SysMsgLeave");

            migrationBuilder.RenameColumn(
                name: "MembersCanSendMedia",
                table: "Conversations",
                newName: "SysMsgJoin");

            migrationBuilder.RenameColumn(
                name: "MembersCanReact",
                table: "Conversations",
                newName: "SysMsgEdit");

            migrationBuilder.RenameColumn(
                name: "Role",
                table: "ConversationMembers",
                newName: "OverridesJson");

            migrationBuilder.RenameColumn(
                name: "IsBanned",
                table: "ConversationMembers",
                newName: "IsOwner");

            migrationBuilder.AddColumn<int>(
                name: "SlowMode",
                table: "Conversations",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.CreateTable(
                name: "GroupAuditLogs",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    ConversationId = table.Column<int>(type: "integer", nullable: false),
                    UserId = table.Column<int>(type: "integer", nullable: false),
                    Action = table.Column<string>(type: "text", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_GroupAuditLogs", x => x.Id);
                    table.ForeignKey(
                        name: "FK_GroupAuditLogs_Conversations_ConversationId",
                        column: x => x.ConversationId,
                        principalTable: "Conversations",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_GroupAuditLogs_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "GroupBans",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    ConversationId = table.Column<int>(type: "integer", nullable: false),
                    UserId = table.Column<int>(type: "integer", nullable: false),
                    AdminId = table.Column<int>(type: "integer", nullable: false),
                    Reason = table.Column<string>(type: "text", nullable: true),
                    BannedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_GroupBans", x => x.Id);
                    table.ForeignKey(
                        name: "FK_GroupBans_Conversations_ConversationId",
                        column: x => x.ConversationId,
                        principalTable: "Conversations",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_GroupBans_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "GroupRoles",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    ConversationId = table.Column<int>(type: "integer", nullable: false),
                    Name = table.Column<string>(type: "text", nullable: false),
                    Color = table.Column<string>(type: "text", nullable: false),
                    Icon = table.Column<string>(type: "text", nullable: true),
                    IsMentionable = table.Column<bool>(type: "boolean", nullable: false),
                    Hierarchy = table.Column<int>(type: "integer", nullable: false),
                    CanEditRoleDesign = table.Column<bool>(type: "boolean", nullable: false),
                    PermissionsJson = table.Column<string>(type: "text", nullable: false),
                    GrantablePermissionsJson = table.Column<string>(type: "text", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_GroupRoles", x => x.Id);
                    table.ForeignKey(
                        name: "FK_GroupRoles_Conversations_ConversationId",
                        column: x => x.ConversationId,
                        principalTable: "Conversations",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "ConversationMemberRoles",
                columns: table => new
                {
                    MemberId = table.Column<int>(type: "integer", nullable: false),
                    RoleId = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ConversationMemberRoles", x => new { x.MemberId, x.RoleId });
                    table.ForeignKey(
                        name: "FK_ConversationMemberRoles_ConversationMembers_MemberId",
                        column: x => x.MemberId,
                        principalTable: "ConversationMembers",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_ConversationMemberRoles_GroupRoles_RoleId",
                        column: x => x.RoleId,
                        principalTable: "GroupRoles",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_ConversationMemberRoles_RoleId",
                table: "ConversationMemberRoles",
                column: "RoleId");

            migrationBuilder.CreateIndex(
                name: "IX_GroupAuditLogs_ConversationId",
                table: "GroupAuditLogs",
                column: "ConversationId");

            migrationBuilder.CreateIndex(
                name: "IX_GroupAuditLogs_UserId",
                table: "GroupAuditLogs",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "IX_GroupBans_ConversationId",
                table: "GroupBans",
                column: "ConversationId");

            migrationBuilder.CreateIndex(
                name: "IX_GroupBans_UserId",
                table: "GroupBans",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "IX_GroupRoles_ConversationId",
                table: "GroupRoles",
                column: "ConversationId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ConversationMemberRoles");

            migrationBuilder.DropTable(
                name: "GroupAuditLogs");

            migrationBuilder.DropTable(
                name: "GroupBans");

            migrationBuilder.DropTable(
                name: "GroupRoles");

            migrationBuilder.DropColumn(
                name: "SlowMode",
                table: "Conversations");

            migrationBuilder.RenameColumn(
                name: "SysMsgLeave",
                table: "Conversations",
                newName: "MembersCanSendMessages");

            migrationBuilder.RenameColumn(
                name: "SysMsgJoin",
                table: "Conversations",
                newName: "MembersCanSendMedia");

            migrationBuilder.RenameColumn(
                name: "SysMsgEdit",
                table: "Conversations",
                newName: "MembersCanReact");

            migrationBuilder.RenameColumn(
                name: "OverridesJson",
                table: "ConversationMembers",
                newName: "Role");

            migrationBuilder.RenameColumn(
                name: "IsOwner",
                table: "ConversationMembers",
                newName: "IsBanned");

            migrationBuilder.AddColumn<bool>(
                name: "MembersCanForward",
                table: "Conversations",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "CanBanUsers",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "CanChangeInfo",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "CanChangeLink",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "CanDeleteMessages",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "CanPinMessages",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "CanPromoteAdmins",
                table: "ConversationMembers",
                type: "boolean",
                nullable: false,
                defaultValue: false);
        }
    }
}
