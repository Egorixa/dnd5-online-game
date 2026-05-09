using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Rooms.Migrations
{

    public partial class AddRoomNameAndUppercaseEnums : Migration
    {

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

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "name",
                schema: "rooms",
                table: "rooms");
        }
    }
}
