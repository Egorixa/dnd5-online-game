using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Characters.Migrations
{
    public partial class SpellSlotsPerLevel : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "character_spell_slots",
                schema: "characters",
                columns: table => new
                {
                    character_id = table.Column<Guid>(type: "uuid", nullable: false),
                    level = table.Column<int>(type: "integer", nullable: false),
                    total = table.Column<int>(type: "integer", nullable: false),
                    used = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_character_spell_slots", x => new { x.character_id, x.level });
                    table.ForeignKey(
                        name: "FK_character_spell_slots_characters_character_id",
                        column: x => x.character_id,
                        principalSchema: "characters",
                        principalTable: "characters",
                        principalColumn: "character_id",
                        onDelete: ReferentialAction.Cascade);
                });
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "character_spell_slots",
                schema: "characters");
        }
    }
}
