using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Rooms.Migrations
{
    /// <inheritdoc />
    public partial class InitialRooms : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.EnsureSchema(
                name: "rooms");

            migrationBuilder.CreateTable(
                name: "room_events",
                schema: "rooms",
                columns: table => new
                {
                    event_id = table.Column<Guid>(type: "uuid", nullable: false),
                    room_id = table.Column<Guid>(type: "uuid", nullable: false),
                    type = table.Column<string>(type: "text", nullable: false),
                    actor_user_id = table.Column<Guid>(type: "uuid", nullable: true),
                    payload = table.Column<string>(type: "jsonb", nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_room_events", x => x.event_id);
                });

            migrationBuilder.CreateTable(
                name: "rooms",
                schema: "rooms",
                columns: table => new
                {
                    room_id = table.Column<Guid>(type: "uuid", nullable: false),
                    room_code = table.Column<string>(type: "character varying(8)", maxLength: 8, nullable: false),
                    master_id = table.Column<Guid>(type: "uuid", nullable: false),
                    access_mode = table.Column<string>(type: "text", nullable: false),
                    status = table.Column<string>(type: "text", nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    finished_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_rooms", x => x.room_id);
                });

            migrationBuilder.CreateTable(
                name: "room_participants",
                schema: "rooms",
                columns: table => new
                {
                    participant_id = table.Column<Guid>(type: "uuid", nullable: false),
                    room_id = table.Column<Guid>(type: "uuid", nullable: false),
                    user_id = table.Column<Guid>(type: "uuid", nullable: false),
                    role = table.Column<string>(type: "text", nullable: false),
                    joined_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    left_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_room_participants", x => x.participant_id);
                    table.ForeignKey(
                        name: "FK_room_participants_rooms_room_id",
                        column: x => x.room_id,
                        principalSchema: "rooms",
                        principalTable: "rooms",
                        principalColumn: "room_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_room_events_room_id_created_at",
                schema: "rooms",
                table: "room_events",
                columns: new[] { "room_id", "created_at" });

            migrationBuilder.CreateIndex(
                name: "IX_room_participants_room_id_user_id_left_at",
                schema: "rooms",
                table: "room_participants",
                columns: new[] { "room_id", "user_id", "left_at" });

            migrationBuilder.CreateIndex(
                name: "IX_rooms_room_code",
                schema: "rooms",
                table: "rooms",
                column: "room_code",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "room_events",
                schema: "rooms");

            migrationBuilder.DropTable(
                name: "room_participants",
                schema: "rooms");

            migrationBuilder.DropTable(
                name: "rooms",
                schema: "rooms");
        }
    }
}
