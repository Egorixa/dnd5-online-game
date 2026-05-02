using Characters.Application.DTOs;
using FluentValidation;

namespace Characters.Application.Validators
{
    public class CharacterUpsertValidator : AbstractValidator<CharacterUpsertRequest>
    {
        public CharacterUpsertValidator()
        {
            When(x => x.Name != null, () =>
                RuleFor(x => x.Name!).Length(1, 50).WithMessage("Name must be 1-50 characters"));

            When(x => x.PlayerName != null, () =>
                RuleFor(x => x.PlayerName!).Length(1, 50));

            When(x => x.Level.HasValue, () =>
                RuleFor(x => x.Level!.Value).InclusiveBetween(1, 20));

            When(x => x.ExperiencePoints.HasValue, () =>
                RuleFor(x => x.ExperiencePoints!.Value).InclusiveBetween(0, 999_999));

            When(x => x.Strength.HasValue, () => RuleFor(x => x.Strength!.Value).InclusiveBetween(1, 30));
            When(x => x.Dexterity.HasValue, () => RuleFor(x => x.Dexterity!.Value).InclusiveBetween(1, 30));
            When(x => x.Constitution.HasValue, () => RuleFor(x => x.Constitution!.Value).InclusiveBetween(1, 30));
            When(x => x.Intelligence.HasValue, () => RuleFor(x => x.Intelligence!.Value).InclusiveBetween(1, 30));
            When(x => x.Wisdom.HasValue, () => RuleFor(x => x.Wisdom!.Value).InclusiveBetween(1, 30));
            When(x => x.Charisma.HasValue, () => RuleFor(x => x.Charisma!.Value).InclusiveBetween(1, 30));

            When(x => x.ArmorClass.HasValue, () => RuleFor(x => x.ArmorClass!.Value).InclusiveBetween(1, 50));
            When(x => x.InitiativeBonus.HasValue, () => RuleFor(x => x.InitiativeBonus!.Value).InclusiveBetween(-20, 20));
            When(x => x.Speed.HasValue, () => RuleFor(x => x.Speed!.Value).InclusiveBetween(0, 200));
            When(x => x.MaxHp.HasValue, () => RuleFor(x => x.MaxHp!.Value).InclusiveBetween(1, 999));
            When(x => x.CurrentHp.HasValue, () => RuleFor(x => x.CurrentHp!.Value).InclusiveBetween(0, 999));
            When(x => x.TempHp.HasValue, () => RuleFor(x => x.TempHp!.Value).InclusiveBetween(0, 999));
            When(x => x.HitDiceRemaining.HasValue, () => RuleFor(x => x.HitDiceRemaining!.Value).GreaterThanOrEqualTo(0));
            When(x => x.DeathSaveSuccesses.HasValue, () => RuleFor(x => x.DeathSaveSuccesses!.Value).InclusiveBetween(0, 3));
            When(x => x.DeathSaveFailures.HasValue, () => RuleFor(x => x.DeathSaveFailures!.Value).InclusiveBetween(0, 3));

            When(x => x.CopperPieces.HasValue, () => RuleFor(x => x.CopperPieces!.Value).InclusiveBetween(0, 999_999));
            When(x => x.SilverPieces.HasValue, () => RuleFor(x => x.SilverPieces!.Value).InclusiveBetween(0, 999_999));
            When(x => x.ElectrumPieces.HasValue, () => RuleFor(x => x.ElectrumPieces!.Value).InclusiveBetween(0, 999_999));
            When(x => x.GoldPieces.HasValue, () => RuleFor(x => x.GoldPieces!.Value).InclusiveBetween(0, 999_999));
            When(x => x.PlatinumPieces.HasValue, () => RuleFor(x => x.PlatinumPieces!.Value).InclusiveBetween(0, 999_999));

            When(x => x.Equipment != null, () => RuleFor(x => x.Equipment!).MaximumLength(2000));
            When(x => x.OtherProficiencies != null, () => RuleFor(x => x.OtherProficiencies!).MaximumLength(1000));
            When(x => x.CharacterTraits != null, () => RuleFor(x => x.CharacterTraits!).MaximumLength(1500));
            When(x => x.Ideals != null, () => RuleFor(x => x.Ideals!).MaximumLength(1500));
            When(x => x.Bonds != null, () => RuleFor(x => x.Bonds!).MaximumLength(1500));
            When(x => x.Flaws != null, () => RuleFor(x => x.Flaws!).MaximumLength(1500));
            When(x => x.FeaturesAndTraits != null, () => RuleFor(x => x.FeaturesAndTraits!).MaximumLength(3000));

            When(x => x.Eyes != null, () => RuleFor(x => x.Eyes!).MaximumLength(30));
            When(x => x.Skin != null, () => RuleFor(x => x.Skin!).MaximumLength(30));
            When(x => x.Hair != null, () => RuleFor(x => x.Hair!).MaximumLength(30));
            When(x => x.Age.HasValue, () => RuleFor(x => x.Age!.Value).InclusiveBetween(0, 999));
            When(x => x.Height.HasValue, () => RuleFor(x => x.Height!.Value).InclusiveBetween(0, 999));
            When(x => x.Weight.HasValue, () => RuleFor(x => x.Weight!.Value).InclusiveBetween(0, 999));

            When(x => x.AlliesAndOrganizations != null, () => RuleFor(x => x.AlliesAndOrganizations!).MaximumLength(1500));
            When(x => x.Backstory != null, () => RuleFor(x => x.Backstory!).MaximumLength(3000));
            When(x => x.Treasure != null, () => RuleFor(x => x.Treasure!).MaximumLength(1500));
            When(x => x.AdditionalNotes != null, () => RuleFor(x => x.AdditionalNotes!).MaximumLength(1500));
            When(x => x.DistinguishingMarks != null, () => RuleFor(x => x.DistinguishingMarks!).MaximumLength(1500));

            When(x => x.Attacks != null, () =>
            {
                RuleFor(x => x.Attacks!.Count).LessThanOrEqualTo(20).WithMessage("Up to 20 attacks");
                RuleForEach(x => x.Attacks!).ChildRules(a =>
                {
                    a.RuleFor(at => at.Name).NotEmpty().Length(1, 50);
                    a.RuleFor(at => at.AttackBonus).InclusiveBetween(-20, 20);
                    a.RuleFor(at => at.Damage).NotEmpty().Length(1, 50);
                });
            });

            When(x => x.Spells != null, () =>
            {
                RuleFor(x => x.Spells!.Count).LessThanOrEqualTo(100).WithMessage("Up to 100 spells");
                RuleForEach(x => x.Spells!).ChildRules(s =>
                {
                    s.RuleFor(sp => sp.Name).NotEmpty().Length(1, 60);
                    s.RuleFor(sp => sp.Level).InclusiveBetween(0, 9);
                });
            });

            When(x => x.SpellSlotsTotal.HasValue, () => RuleFor(x => x.SpellSlotsTotal!.Value).InclusiveBetween(0, 99));
            When(x => x.SpellSlotsUsed.HasValue, () => RuleFor(x => x.SpellSlotsUsed!.Value).InclusiveBetween(0, 99));
            When(x => x.PreparedLimit.HasValue, () => RuleFor(x => x.PreparedLimit!.Value).InclusiveBetween(0, 99));
        }
    }
}
