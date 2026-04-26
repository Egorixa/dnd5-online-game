using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Characters.Migrations
{
    /// <inheritdoc />
    public partial class InitialCharacters : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.EnsureSchema(
                name: "characters");

            migrationBuilder.CreateTable(
                name: "characters",
                schema: "characters",
                columns: table => new
                {
                    character_id = table.Column<Guid>(type: "uuid", nullable: false),
                    room_id = table.Column<Guid>(type: "uuid", nullable: false),
                    owner_user_id = table.Column<Guid>(type: "uuid", nullable: false),
                    is_archived = table.Column<bool>(type: "boolean", nullable: false),
                    row_version = table.Column<long>(type: "bigint", nullable: false),
                    name = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    player_name = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    race = table.Column<string>(type: "text", nullable: true),
                    @class = table.Column<string>(name: "class", type: "text", nullable: true),
                    level = table.Column<int>(type: "integer", nullable: false),
                    background = table.Column<string>(type: "text", nullable: true),
                    alignment = table.Column<string>(type: "text", nullable: true),
                    experience_points = table.Column<int>(type: "integer", nullable: false),
                    strength = table.Column<int>(type: "integer", nullable: false),
                    dexterity = table.Column<int>(type: "integer", nullable: false),
                    constitution = table.Column<int>(type: "integer", nullable: false),
                    intelligence = table.Column<int>(type: "integer", nullable: false),
                    wisdom = table.Column<int>(type: "integer", nullable: false),
                    charisma = table.Column<int>(type: "integer", nullable: false),
                    armor_class = table.Column<int>(type: "integer", nullable: false),
                    initiative_bonus = table.Column<int>(type: "integer", nullable: false),
                    speed = table.Column<int>(type: "integer", nullable: false),
                    max_hp = table.Column<int>(type: "integer", nullable: false),
                    current_hp = table.Column<int>(type: "integer", nullable: false),
                    temp_hp = table.Column<int>(type: "integer", nullable: false),
                    hit_die_type = table.Column<string>(type: "text", nullable: true),
                    hit_dice_remaining = table.Column<int>(type: "integer", nullable: false),
                    death_save_successes = table.Column<int>(type: "integer", nullable: false),
                    death_save_failures = table.Column<int>(type: "integer", nullable: false),
                    inspiration = table.Column<bool>(type: "boolean", nullable: false),
                    copper_pieces = table.Column<int>(type: "integer", nullable: false),
                    silver_pieces = table.Column<int>(type: "integer", nullable: false),
                    electrum_pieces = table.Column<int>(type: "integer", nullable: false),
                    gold_pieces = table.Column<int>(type: "integer", nullable: false),
                    platinum_pieces = table.Column<int>(type: "integer", nullable: false),
                    equipment = table.Column<string>(type: "character varying(2000)", maxLength: 2000, nullable: false),
                    other_proficiencies = table.Column<string>(type: "character varying(1000)", maxLength: 1000, nullable: false),
                    character_traits = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    ideals = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    bonds = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    flaws = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    features_and_traits = table.Column<string>(type: "character varying(3000)", maxLength: 3000, nullable: false),
                    eyes = table.Column<string>(type: "character varying(30)", maxLength: 30, nullable: false),
                    age = table.Column<int>(type: "integer", nullable: false),
                    height = table.Column<int>(type: "integer", nullable: false),
                    weight = table.Column<int>(type: "integer", nullable: false),
                    skin = table.Column<string>(type: "character varying(30)", maxLength: 30, nullable: false),
                    hair = table.Column<string>(type: "character varying(30)", maxLength: 30, nullable: false),
                    allies_and_organizations = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    backstory = table.Column<string>(type: "character varying(3000)", maxLength: 3000, nullable: false),
                    treasure = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    additional_notes = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    distinguishing_marks = table.Column<string>(type: "character varying(1500)", maxLength: 1500, nullable: false),
                    spellcasting_class = table.Column<string>(type: "text", nullable: true),
                    spell_slots_total = table.Column<int>(type: "integer", nullable: false),
                    spell_slots_used = table.Column<int>(type: "integer", nullable: false),
                    prepared_limit = table.Column<int>(type: "integer", nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    updated_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_characters", x => x.character_id);
                });

            migrationBuilder.CreateTable(
                name: "character_attacks",
                schema: "characters",
                columns: table => new
                {
                    attack_id = table.Column<Guid>(type: "uuid", nullable: false),
                    character_id = table.Column<Guid>(type: "uuid", nullable: false),
                    name = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    attack_bonus = table.Column<int>(type: "integer", nullable: false),
                    damage = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_character_attacks", x => x.attack_id);
                    table.ForeignKey(
                        name: "FK_character_attacks_characters_character_id",
                        column: x => x.character_id,
                        principalSchema: "characters",
                        principalTable: "characters",
                        principalColumn: "character_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "character_save_proficiencies",
                schema: "characters",
                columns: table => new
                {
                    character_id = table.Column<Guid>(type: "uuid", nullable: false),
                    ability = table.Column<string>(type: "text", nullable: false),
                    level = table.Column<string>(type: "text", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_character_save_proficiencies", x => new { x.character_id, x.ability });
                    table.ForeignKey(
                        name: "FK_character_save_proficiencies_characters_character_id",
                        column: x => x.character_id,
                        principalSchema: "characters",
                        principalTable: "characters",
                        principalColumn: "character_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "character_skill_proficiencies",
                schema: "characters",
                columns: table => new
                {
                    character_id = table.Column<Guid>(type: "uuid", nullable: false),
                    skill = table.Column<string>(type: "text", nullable: false),
                    level = table.Column<string>(type: "text", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_character_skill_proficiencies", x => new { x.character_id, x.skill });
                    table.ForeignKey(
                        name: "FK_character_skill_proficiencies_characters_character_id",
                        column: x => x.character_id,
                        principalSchema: "characters",
                        principalTable: "characters",
                        principalColumn: "character_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "character_spells",
                schema: "characters",
                columns: table => new
                {
                    spell_id = table.Column<Guid>(type: "uuid", nullable: false),
                    character_id = table.Column<Guid>(type: "uuid", nullable: false),
                    name = table.Column<string>(type: "character varying(60)", maxLength: 60, nullable: false),
                    level = table.Column<int>(type: "integer", nullable: false),
                    school = table.Column<string>(type: "character varying(40)", maxLength: 40, nullable: false),
                    casting_time = table.Column<string>(type: "character varying(60)", maxLength: 60, nullable: false),
                    range = table.Column<string>(type: "character varying(60)", maxLength: 60, nullable: false),
                    components = table.Column<string>(type: "character varying(60)", maxLength: 60, nullable: false),
                    duration = table.Column<string>(type: "character varying(60)", maxLength: 60, nullable: false),
                    description = table.Column<string>(type: "character varying(2000)", maxLength: 2000, nullable: false),
                    prepared = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_character_spells", x => x.spell_id);
                    table.ForeignKey(
                        name: "FK_character_spells_characters_character_id",
                        column: x => x.character_id,
                        principalSchema: "characters",
                        principalTable: "characters",
                        principalColumn: "character_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_character_attacks_character_id",
                schema: "characters",
                table: "character_attacks",
                column: "character_id");

            migrationBuilder.CreateIndex(
                name: "IX_character_spells_character_id",
                schema: "characters",
                table: "character_spells",
                column: "character_id");

            migrationBuilder.CreateIndex(
                name: "IX_characters_room_id",
                schema: "characters",
                table: "characters",
                column: "room_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "character_attacks",
                schema: "characters");

            migrationBuilder.DropTable(
                name: "character_save_proficiencies",
                schema: "characters");

            migrationBuilder.DropTable(
                name: "character_skill_proficiencies",
                schema: "characters");

            migrationBuilder.DropTable(
                name: "character_spells",
                schema: "characters");

            migrationBuilder.DropTable(
                name: "characters",
                schema: "characters");
        }
    }
}
