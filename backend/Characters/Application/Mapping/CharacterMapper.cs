using Characters.Application.DTOs;
using Characters.Application.Services;
using Characters.Entities;
using Shared.Lookups;

namespace Characters.Application.Mapping
{
    public static class CharacterMapper
    {
        public static CharacterResponse ToResponse(Character c)
        {
            var response = new CharacterResponse
            {
                CharacterId = c.CharacterId,
                RoomId = c.RoomId,
                OwnerUserId = c.OwnerUserId,
                RowVersion = c.RowVersion,

                Name = c.Name,
                PlayerName = c.PlayerName,
                Race = c.Race,
                Class = c.Class,
                Level = c.Level,
                Background = c.Background,
                Alignment = c.Alignment,
                ExperiencePoints = c.ExperiencePoints,

                Strength = c.Strength,
                Dexterity = c.Dexterity,
                Constitution = c.Constitution,
                Intelligence = c.Intelligence,
                Wisdom = c.Wisdom,
                Charisma = c.Charisma,

                Modifiers = new AbilityModifiers
                {
                    Strength = CharacterCalculator.Modifier(c.Strength),
                    Dexterity = CharacterCalculator.Modifier(c.Dexterity),
                    Constitution = CharacterCalculator.Modifier(c.Constitution),
                    Intelligence = CharacterCalculator.Modifier(c.Intelligence),
                    Wisdom = CharacterCalculator.Modifier(c.Wisdom),
                    Charisma = CharacterCalculator.Modifier(c.Charisma),
                },

                ProficiencyBonus = CharacterCalculator.ProficiencyBonus(c.Level),
                PassivePerception = CharacterCalculator.PassivePerception(c),
                SpellSaveDc = CharacterCalculator.SpellSaveDc(c),
                SpellAttackBonus = CharacterCalculator.SpellAttackBonus(c),
                HitDiceTotal = CharacterCalculator.HitDiceTotal(c),

                ArmorClass = c.ArmorClass,
                InitiativeBonus = c.InitiativeBonus,
                Speed = c.Speed,
                MaxHp = c.MaxHp,
                CurrentHp = c.CurrentHp,
                TempHp = c.TempHp,
                HitDieType = c.HitDieType,
                HitDiceRemaining = c.HitDiceRemaining,
                DeathSaveSuccesses = c.DeathSaveSuccesses,
                DeathSaveFailures = c.DeathSaveFailures,
                Inspiration = c.Inspiration,

                CopperPieces = c.CopperPieces,
                SilverPieces = c.SilverPieces,
                ElectrumPieces = c.ElectrumPieces,
                GoldPieces = c.GoldPieces,
                PlatinumPieces = c.PlatinumPieces,

                Equipment = c.Equipment,
                OtherProficiencies = c.OtherProficiencies,
                CharacterTraits = c.CharacterTraits,
                Ideals = c.Ideals,
                Bonds = c.Bonds,
                Flaws = c.Flaws,
                FeaturesAndTraits = c.FeaturesAndTraits,

                Eyes = c.Eyes,
                Age = c.Age,
                Height = c.Height,
                Weight = c.Weight,
                Skin = c.Skin,
                Hair = c.Hair,
                AlliesAndOrganizations = c.AlliesAndOrganizations,
                Backstory = c.Backstory,
                Treasure = c.Treasure,
                AdditionalNotes = c.AdditionalNotes,
                DistinguishingMarks = c.DistinguishingMarks,

                SpellcastingClass = c.SpellcastingClass,
                SpellSlotsTotal = c.SpellSlotsTotal,
                SpellSlotsUsed = c.SpellSlotsUsed,
                PreparedLimit = c.PreparedLimit,

                Attacks = c.Attacks
                    .Select(a => new AttackDto
                    {
                        AttackId = a.AttackId,
                        Name = a.Name,
                        AttackBonus = a.AttackBonus,
                        Damage = a.Damage
                    })
                    .ToList(),

                Spells = c.Spells
                    .Select(s => new SpellResponseDto
                    {
                        SpellId = s.SpellId,
                        Name = s.Name,
                        Level = s.Level,
                        School = s.School,
                        CastingTime = s.CastingTime,
                        Range = s.Range,
                        Components = s.Components,
                        Duration = s.Duration,
                        Description = s.Description,
                        Prepared = s.Prepared
                    })
                    .ToList()
            };

            foreach (Skill skill in Enum.GetValues<Skill>())
            {
                var level = c.SkillProficiencies.FirstOrDefault(p => p.Skill == skill)?.Level ?? ProficiencyLevel.None;
                response.Skills[skill] = new SkillView
                {
                    Level = level,
                    Bonus = CharacterCalculator.SkillBonus(c, skill)
                };
            }

            foreach (Ability ability in Enum.GetValues<Ability>())
            {
                var level = c.SaveProficiencies.FirstOrDefault(p => p.Ability == ability)?.Level ?? ProficiencyLevel.None;
                response.Saves[ability] = new SaveView
                {
                    Level = level,
                    Bonus = CharacterCalculator.SaveBonus(c, ability)
                };
            }

            return response;
        }
    }
}
