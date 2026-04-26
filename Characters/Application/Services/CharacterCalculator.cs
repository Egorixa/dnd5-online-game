using Characters.Entities;
using Shared.Lookups;

namespace Characters.Application.Services
{
    public static class CharacterCalculator
    {
        public static int Modifier(int abilityScore)
        {
            // D&D 5e: floor((score - 10) / 2)
            int diff = abilityScore - 10;
            return diff >= 0 ? diff / 2 : (diff - 1) / 2;
        }

        public static int ProficiencyBonus(int level)
        {
            level = Math.Clamp(level, 1, 20);
            return 2 + (level - 1) / 4;
        }

        public static int GetAbilityScore(Character c, Ability ability) => ability switch
        {
            Ability.Strength => c.Strength,
            Ability.Dexterity => c.Dexterity,
            Ability.Constitution => c.Constitution,
            Ability.Intelligence => c.Intelligence,
            Ability.Wisdom => c.Wisdom,
            Ability.Charisma => c.Charisma,
            _ => 10
        };

        public static int GetAbilityModifier(Character c, Ability ability)
            => Modifier(GetAbilityScore(c, ability));

        public static int PassivePerception(Character c)
        {
            int wisMod = GetAbilityModifier(c, Ability.Wisdom);
            bool proficient = c.SkillProficiencies
                .Any(sp => sp.Skill == Skill.Perception && sp.Level == ProficiencyLevel.Proficient);
            return 10 + wisMod + (proficient ? ProficiencyBonus(c.Level) : 0);
        }

        public static int? SpellSaveDc(Character c)
        {
            if (c.SpellcastingClass is null) return null;
            var info = ClassCatalog.Info[c.SpellcastingClass.Value];
            if (info.SpellcastingAbility is null) return null;
            return 8 + ProficiencyBonus(c.Level) + GetAbilityModifier(c, info.SpellcastingAbility.Value);
        }

        public static int? SpellAttackBonus(Character c)
        {
            if (c.SpellcastingClass is null) return null;
            var info = ClassCatalog.Info[c.SpellcastingClass.Value];
            if (info.SpellcastingAbility is null) return null;
            return ProficiencyBonus(c.Level) + GetAbilityModifier(c, info.SpellcastingAbility.Value);
        }

        public static int SkillBonus(Character c, Skill skill)
        {
            var ability = SkillCatalog.AbilityForSkill[skill];
            int mod = GetAbilityModifier(c, ability);
            bool proficient = c.SkillProficiencies
                .Any(sp => sp.Skill == skill && sp.Level == ProficiencyLevel.Proficient);
            return mod + (proficient ? ProficiencyBonus(c.Level) : 0);
        }

        public static int SaveBonus(Character c, Ability ability)
        {
            int mod = GetAbilityModifier(c, ability);
            bool proficient = c.SaveProficiencies
                .Any(sp => sp.Ability == ability && sp.Level == ProficiencyLevel.Proficient);
            return mod + (proficient ? ProficiencyBonus(c.Level) : 0);
        }

        public static int HitDiceTotal(Character c) => c.Level;

        public static HitDie? ExpectedHitDie(CharacterClass? cls)
            => cls.HasValue ? ClassCatalog.Info[cls.Value].HitDie : null;
    }
}
