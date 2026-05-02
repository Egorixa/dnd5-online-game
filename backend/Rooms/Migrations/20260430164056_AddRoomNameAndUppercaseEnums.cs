using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Rooms.Migrations
{
    /// <inheritdoc />
    public partial class AddRoomNameAndUppercaseEnums : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "name",
                schema: "rooms",
                table: "rooms",
                type: "character varying(100)",
                maxLength: 100,
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "name",
                schema: "rooms",
                table: "rooms");
        }
    }
}
