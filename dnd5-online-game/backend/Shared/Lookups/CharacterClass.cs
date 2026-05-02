namespace Shared.Lookups
{
    public enum CharacterClass
    {
        Barbarian,
        Bard,
        Cleric,
        Druid,
        Fighter,
        Monk,
        Paladin,
        Ranger,
        Rogue,
        Sorcerer,
        Warlock,
        Wizard
    }

    public enum HitDie
    {
        d6 = 6,
        d8 = 8,
        d10 = 10,
        d12 = 12
    }

    public enum SpellcastingType
    {
        None,
        Full,
        Half,
        Pact
    }

    public enum Ability
    {
        Strength,
        Dexterity,
        Constitution,
        Intelligence,
        Wisdom,
        Charisma
    }

    public record ClassInfo(
        CharacterClass Class,
        HitDie HitDie,
        SpellcastingType Spellcasting,
        Ability? SpellcastingAbility);

    public static class ClassCatalog
    {
        public static readonly IReadOnlyDictionary<CharacterClass, ClassInfo> Info =
            new Dictionary<CharacterClass, ClassInfo>
            {
                [CharacterClass.Barbarian] = new(CharacterClass.Barbarian, HitDie.d12, SpellcastingType.None, null),
                [CharacterClass.Bard] = new(CharacterClass.Bard, HitDie.d8, SpellcastingType.Full, Ability.Charisma),
                [CharacterClass.Cleric] = new(CharacterClass.Cleric, HitDie.d8, SpellcastingType.Full, Ability.Wisdom),
                [CharacterClass.Druid] = new(CharacterClass.Druid, HitDie.d8, SpellcastingType.Full, Ability.Wisdom),
                [CharacterClass.Fighter] = new(CharacterClass.Fighter, HitDie.d10, SpellcastingType.None, null),
                [CharacterClass.Monk] = new(CharacterClass.Monk, HitDie.d8, SpellcastingType.None, null),
                [CharacterClass.Paladin] = new(CharacterClass.Paladin, HitDie.d10, SpellcastingType.Half, Ability.Charisma),
                [CharacterClass.Ranger] = new(CharacterClass.Ranger, HitDie.d10, SpellcastingType.Half, Ability.Wisdom),
                [CharacterClass.Rogue] = new(CharacterClass.Rogue, HitDie.d8, SpellcastingType.None, null),
                [CharacterClass.Sorcerer] = new(CharacterClass.Sorcerer, HitDie.d6, SpellcastingType.Full, Ability.Charisma),
                [CharacterClass.Warlock] = new(CharacterClass.Warlock, HitDie.d8, SpellcastingType.Pact, Ability.Charisma),
                [CharacterClass.Wizard] = new(CharacterClass.Wizard, HitDie.d6, SpellcastingType.Full, Ability.Intelligence),
            };
    }
}
