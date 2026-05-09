using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Characters.Migrations
{

    public partial class CharacterTemplates : Migration
    {

        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<Guid>(
                name: "room_id",
                schema: "characters",
                table: "characters",
                type: "uuid",
                nullable: true,
                oldClrType: typeof(Guid),
                oldType: "uuid");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<Guid>(
                name: "room_id",
                schema: "characters",
                table: "characters",
                type: "uuid",
                nullable: false,
                defaultValue: new Guid("00000000-0000-0000-0000-000000000000"),
                oldClrType: typeof(Guid),
                oldType: "uuid",
                oldNullable: true);
        }
    }
}
