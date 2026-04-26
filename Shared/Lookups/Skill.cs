namespace Shared.Lookups
{
    public enum Skill
    {
        Acrobatics,
        AnimalHandling,
        Arcana,
        Athletics,
        Deception,
        History,
        Insight,
        Intimidation,
        Investigation,
        Medicine,
        Nature,
        Perception,
        Performance,
        Persuasion,
        Religion,
        SleightOfHand,
        Stealth,
        Survival
    }

    public enum ProficiencyLevel
    {
        None,
        Proficient
    }

    public static class SkillCatalog
    {
        public static readonly IReadOnlyDictionary<Skill, Ability> AbilityForSkill =
            new Dictionary<Skill, Ability>
            {
                [Skill.Acrobatics] = Ability.Dexterity,
                [Skill.AnimalHandling] = Ability.Wisdom,
                [Skill.Arcana] = Ability.Intelligence,
                [Skill.Athletics] = Ability.Strength,
                [Skill.Deception] = Ability.Charisma,
                [Skill.History] = Ability.Intelligence,
                [Skill.Insight] = Ability.Wisdom,
                [Skill.Intimidation] = Ability.Charisma,
                [Skill.Investigation] = Ability.Intelligence,
                [Skill.Medicine] = Ability.Wisdom,
                [Skill.Nature] = Ability.Intelligence,
                [Skill.Perception] = Ability.Wisdom,
                [Skill.Performance] = Ability.Charisma,
                [Skill.Persuasion] = Ability.Charisma,
                [Skill.Religion] = Ability.Intelligence,
                [Skill.SleightOfHand] = Ability.Dexterity,
                [Skill.Stealth] = Ability.Dexterity,
                [Skill.Survival] = Ability.Wisdom,
            };
    }
}
