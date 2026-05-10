using Characters.Application.DTOs;
using Characters.Application.Interfaces;
using Characters.Application.Mapping;
using Characters.Data;
using Characters.Entities;
using FluentValidation;
using Identity.Application.Interfaces;
using Microsoft.EntityFrameworkCore;
using Rooms.Application.Interfaces;
using Shared.Errors;
using Shared.Lookups;
using Shared.RealTime;
using ValidationException = Shared.Errors.ValidationException;

namespace Characters.Application.Services
{
    public class CharacterService : ICharacterService
    {
        private readonly CharactersDbContext _context;
        private readonly IRoomAccessChecker _roomAccess;
        private readonly IValidator<CharacterUpsertRequest> _validator;
        private readonly IRoomNotifier _notifier;
        private readonly IUserLookupService _userLookup;

        public CharacterService(
            CharactersDbContext context,
            IRoomAccessChecker roomAccess,
            IValidator<CharacterUpsertRequest> validator,
            IRoomNotifier notifier,
            IUserLookupService userLookup)
        {
            _context = context;
            _roomAccess = roomAccess;
            _validator = validator;
            _notifier = notifier;
            _userLookup = userLookup;
        }

        public async Task<CharacterResponse> CreateAsync(Guid userId, Guid roomId, CharacterUpsertRequest request, CancellationToken ct = default)
        {
            await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            await ValidateAsync(request, isCreate: true, ct);

            var character = new Character
            {
                CharacterId = Guid.NewGuid(),
                RoomId = roomId,
                OwnerUserId = userId,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            ApplyRequest(character, request);
            EnforceInvariants(character);

            _context.Characters.Add(character);
            await _context.SaveChangesAsync(ct);

            var response = CharacterMapper.ToResponse(character);
            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "created",
                characterName = response.Name,
                ownerUserName,
                character = response
            }, ct);

            return response;
        }

        public async Task<AttackDto> AddAttackAsync(Guid userId, Guid roomId, Guid characterId, AttackDto dto, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            var attack = new Attack
            {
                AttackId = Guid.NewGuid(),
                CharacterId = characterId,
                Name = dto.Name,
                AttackBonus = dto.AttackBonus,
                Damage = dto.Damage
            };

            character.Attacks.Add(attack);
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = character.Name,
                ownerUserName,
                character = CharacterMapper.ToResponse(character)
            }, ct);

            return new AttackDto { AttackId = attack.AttackId, Name = attack.Name, AttackBonus = attack.AttackBonus, Damage = attack.Damage };
        }

        public async Task<AttackDto> UpdateAttackAsync(Guid userId, Guid roomId, Guid characterId, Guid attackId, AttackDto dto, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            var attack = character.Attacks.FirstOrDefault(a => a.AttackId == attackId)
                ?? throw new NotFoundException("Attack not found");

            attack.Name = dto.Name;
            attack.AttackBonus = dto.AttackBonus;
            attack.Damage = dto.Damage;
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = character.Name,
                ownerUserName,
                character = CharacterMapper.ToResponse(character)
            }, ct);

            return new AttackDto { AttackId = attack.AttackId, Name = attack.Name, AttackBonus = attack.AttackBonus, Damage = attack.Damage };
        }

        public async Task DeleteAttackAsync(Guid userId, Guid roomId, Guid characterId, Guid attackId, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            var attack = character.Attacks.FirstOrDefault(a => a.AttackId == attackId)
                ?? throw new NotFoundException("Attack not found");

            character.Attacks.Remove(attack);
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = character.Name,
                ownerUserName,
                character = CharacterMapper.ToResponse(character)
            }, ct);
        }

        public async Task<SpellResponseDto> AddSpellAsync(Guid userId, Guid roomId, Guid characterId, SpellDto dto, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            var spell = new Spell
            {
                SpellId = Guid.NewGuid(),
                CharacterId = characterId,
                Name = dto.Name,
                Level = dto.Level,
                School = dto.School,
                CastingTime = dto.CastingTime,
                Range = dto.Range,
                Components = dto.Components,
                Duration = dto.Duration,
                Description = dto.Description,
                Prepared = dto.Prepared
            };

            character.Spells.Add(spell);
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = character.Name,
                ownerUserName,
                character = CharacterMapper.ToResponse(character)
            }, ct);

            return new SpellResponseDto { SpellId = spell.SpellId, Name = spell.Name, Level = spell.Level, School = spell.School, CastingTime = spell.CastingTime, Range = spell.Range, Components = spell.Components, Duration = spell.Duration, Description = spell.Description, Prepared = spell.Prepared };
        }

        public async Task<SpellResponseDto> UpdateSpellAsync(Guid userId, Guid roomId, Guid characterId, Guid spellId, SpellDto dto, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            var spell = character.Spells.FirstOrDefault(s => s.SpellId == spellId)
                ?? throw new NotFoundException("Spell not found");

            spell.Name = dto.Name;
            spell.Level = dto.Level;
            spell.School = dto.School;
            spell.CastingTime = dto.CastingTime;
            spell.Range = dto.Range;
            spell.Components = dto.Components;
            spell.Duration = dto.Duration;
            spell.Description = dto.Description;
            spell.Prepared = dto.Prepared;
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = character.Name,
                ownerUserName,
                character = CharacterMapper.ToResponse(character)
            }, ct);

            return new SpellResponseDto { SpellId = spell.SpellId, Name = spell.Name, Level = spell.Level, School = spell.School, CastingTime = spell.CastingTime, Range = spell.Range, Components = spell.Components, Duration = spell.Duration, Description = spell.Description, Prepared = spell.Prepared };
        }

        public async Task DeleteSpellAsync(Guid userId, Guid roomId, Guid characterId, Guid spellId, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            var spell = character.Spells.FirstOrDefault(s => s.SpellId == spellId)
                ?? throw new NotFoundException("Spell not found");

            character.Spells.Remove(spell);
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = character.Name,
                ownerUserName,
                character = CharacterMapper.ToResponse(character)
            }, ct);
        }

        public async Task<CharacterResponse> GetAsync(Guid userId, Guid roomId, Guid characterId, CancellationToken ct = default)
        {
            await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);
            return CharacterMapper.ToResponse(character);
        }

        public async Task<List<CharacterResponse>> ListByRoomAsync(Guid userId, Guid roomId, CancellationToken ct = default)
        {
            await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var characters = await _context.Characters
                .Include(c => c.SkillProficiencies)
                .Include(c => c.SaveProficiencies)
                .Include(c => c.Attacks)
                .Include(c => c.Spells)
                .Include(c => c.SpellSlots)
                .Where(c => c.RoomId == roomId && !c.IsArchived)
                .ToListAsync(ct);

            return characters.Select(CharacterMapper.ToResponse).ToList();
        }

        public async Task<CharacterResponse> UpdateAsync(Guid userId, Guid roomId, Guid characterId, CharacterUpsertRequest request, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may edit it");

            await ValidateAsync(request, isCreate: false, ct);

            if (request.RowVersion.HasValue && request.RowVersion.Value != character.RowVersion)
                throw new ConflictException("VERSION_CONFLICT", "Character was modified concurrently");

            ApplyRequest(character, request);
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            EnforceInvariants(character);

            await _context.SaveChangesAsync(ct);

            var response = CharacterMapper.ToResponse(character);
            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "updated",
                characterName = response.Name,
                ownerUserName,
                character = response
            }, ct);

            return response;
        }

        public async Task DeleteAsync(Guid userId, Guid roomId, Guid characterId, CancellationToken ct = default)
        {
            var access = await _roomAccess.RequireParticipantAsync(userId, roomId, ct);
            var character = await LoadAsync(roomId, characterId, ct);

            if (character.OwnerUserId != userId && !access.IsMaster)
                throw new ForbiddenException("Only the owner of the character or the room master may delete it");

            character.IsArchived = true;
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            await _context.SaveChangesAsync(ct);

            var ownerUserName = await _userLookup.GetUsernameAsync(character.OwnerUserId, ct);
            await _notifier.NotifyAsync(roomId, HubEvents.CharacterUpdated, new
            {
                action = "deleted",
                characterId = character.CharacterId,
                characterName = character.Name,
                ownerUserName
            }, ct);
        }

        private async Task<Character> LoadAsync(Guid roomId, Guid characterId, CancellationToken ct)
        {
            var character = await _context.Characters
                .Include(c => c.SkillProficiencies)
                .Include(c => c.SaveProficiencies)
                .Include(c => c.Attacks)
                .Include(c => c.Spells)
                .Include(c => c.SpellSlots)
                .FirstOrDefaultAsync(c => c.CharacterId == characterId && c.RoomId == roomId && !c.IsArchived, ct)
                ?? throw new NotFoundException("Character not found");
            return character;
        }

        public async Task<CharacterResponse> CreateTemplateAsync(Guid userId, CharacterUpsertRequest request, CancellationToken ct = default)
        {
            await ValidateAsync(request, isCreate: true, ct);

            var character = new Character
            {
                CharacterId = Guid.NewGuid(),
                RoomId = null,
                OwnerUserId = userId,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            ApplyRequest(character, request);
            EnforceInvariants(character);

            _context.Characters.Add(character);
            await _context.SaveChangesAsync(ct);

            return CharacterMapper.ToResponse(character);
        }

        public async Task<List<CharacterResponse>> ListTemplatesAsync(Guid userId, CancellationToken ct = default)
        {
            var characters = await _context.Characters
                .Include(c => c.SkillProficiencies)
                .Include(c => c.SaveProficiencies)
                .Include(c => c.Attacks)
                .Include(c => c.Spells)
                .Include(c => c.SpellSlots)
                .Where(c => c.OwnerUserId == userId && c.RoomId == null && !c.IsArchived)
                .ToListAsync(ct);

            return characters.Select(CharacterMapper.ToResponse).ToList();
        }

        public async Task<CharacterResponse> GetTemplateAsync(Guid userId, Guid characterId, CancellationToken ct = default)
        {
            var character = await LoadTemplateAsync(userId, characterId, ct);
            return CharacterMapper.ToResponse(character);
        }

        public async Task<CharacterResponse> UpdateTemplateAsync(Guid userId, Guid characterId, CharacterUpsertRequest request, CancellationToken ct = default)
        {
            var character = await LoadTemplateAsync(userId, characterId, ct);
            await ValidateAsync(request, isCreate: false, ct);

            if (request.RowVersion.HasValue && request.RowVersion.Value != character.RowVersion)
                throw new ConflictException("VERSION_CONFLICT", "Character was modified concurrently");

            ApplyRequest(character, request);
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;

            EnforceInvariants(character);

            await _context.SaveChangesAsync(ct);
            return CharacterMapper.ToResponse(character);
        }

        public async Task DeleteTemplateAsync(Guid userId, Guid characterId, CancellationToken ct = default)
        {
            var character = await LoadTemplateAsync(userId, characterId, ct);
            character.IsArchived = true;
            character.UpdatedAt = DateTime.UtcNow;
            character.RowVersion += 1;
            await _context.SaveChangesAsync(ct);
        }

        private async Task<Character> LoadTemplateAsync(Guid userId, Guid characterId, CancellationToken ct)
        {
            var character = await _context.Characters
                .Include(c => c.SkillProficiencies)
                .Include(c => c.SaveProficiencies)
                .Include(c => c.Attacks)
                .Include(c => c.Spells)
                .Include(c => c.SpellSlots)
                .FirstOrDefaultAsync(c => c.CharacterId == characterId && c.RoomId == null && !c.IsArchived, ct)
                ?? throw new NotFoundException("Character template not found");

            if (character.OwnerUserId != userId)
                throw new ForbiddenException("Template does not belong to current user");

            return character;
        }

        private async Task ValidateAsync(CharacterUpsertRequest request, bool isCreate, CancellationToken ct)
        {
            var result = await _validator.ValidateAsync(request, ct);
            if (!result.IsValid)
            {
                var details = result.Errors
                    .Select(e => new ApiErrorDetail { Field = e.PropertyName, Message = e.ErrorMessage })
                    .ToList();
                throw new ValidationException("Invalid character data", details);
            }
        }

        private static void ApplyRequest(Character c, CharacterUpsertRequest r)
        {
            if (r.Name != null) c.Name = r.Name;
            if (r.PlayerName != null) c.PlayerName = r.PlayerName;
            if (r.Race.HasValue) c.Race = r.Race;
            if (r.CharacterClass.HasValue) c.Class = r.CharacterClass;
            if (r.Level.HasValue) c.Level = r.Level.Value;
            if (r.Background.HasValue) c.Background = r.Background;
            if (r.Alignment.HasValue) c.Alignment = r.Alignment;
            if (r.ExperiencePoints.HasValue) c.ExperiencePoints = r.ExperiencePoints.Value;

            if (r.Strength.HasValue) c.Strength = r.Strength.Value;
            if (r.Dexterity.HasValue) c.Dexterity = r.Dexterity.Value;
            if (r.Constitution.HasValue) c.Constitution = r.Constitution.Value;
            if (r.Intelligence.HasValue) c.Intelligence = r.Intelligence.Value;
            if (r.Wisdom.HasValue) c.Wisdom = r.Wisdom.Value;
            if (r.Charisma.HasValue) c.Charisma = r.Charisma.Value;

            if (r.ArmorClass.HasValue) c.ArmorClass = r.ArmorClass.Value;
            if (r.InitiativeBonus.HasValue) c.InitiativeBonus = r.InitiativeBonus.Value;
            if (r.Speed.HasValue) c.Speed = r.Speed.Value;
            if (r.MaxHp.HasValue) c.MaxHp = r.MaxHp.Value;
            if (r.CurrentHp.HasValue) c.CurrentHp = r.CurrentHp.Value;
            if (r.TempHp.HasValue) c.TempHp = r.TempHp.Value;
            if (r.HitDieType.HasValue) c.HitDieType = r.HitDieType;
            if (r.HitDiceRemaining.HasValue) c.HitDiceRemaining = r.HitDiceRemaining.Value;
            if (r.DeathSaveSuccesses.HasValue) c.DeathSaveSuccesses = r.DeathSaveSuccesses.Value;
            if (r.DeathSaveFailures.HasValue) c.DeathSaveFailures = r.DeathSaveFailures.Value;
            if (r.Inspiration.HasValue) c.Inspiration = r.Inspiration.Value;

            if (r.CopperPieces.HasValue) c.CopperPieces = r.CopperPieces.Value;
            if (r.SilverPieces.HasValue) c.SilverPieces = r.SilverPieces.Value;
            if (r.ElectrumPieces.HasValue) c.ElectrumPieces = r.ElectrumPieces.Value;
            if (r.GoldPieces.HasValue) c.GoldPieces = r.GoldPieces.Value;
            if (r.PlatinumPieces.HasValue) c.PlatinumPieces = r.PlatinumPieces.Value;

            if (r.Equipment != null) c.Equipment = r.Equipment;
            if (r.OtherProficiencies != null) c.OtherProficiencies = r.OtherProficiencies;
            if (r.CharacterTraits != null) c.CharacterTraits = r.CharacterTraits;
            if (r.Ideals != null) c.Ideals = r.Ideals;
            if (r.Bonds != null) c.Bonds = r.Bonds;
            if (r.Flaws != null) c.Flaws = r.Flaws;
            if (r.FeaturesAndTraits != null) c.FeaturesAndTraits = r.FeaturesAndTraits;

            if (r.Eyes != null) c.Eyes = r.Eyes;
            if (r.Age.HasValue) c.Age = r.Age.Value;
            if (r.Height.HasValue) c.Height = r.Height.Value;
            if (r.Weight.HasValue) c.Weight = r.Weight.Value;
            if (r.Skin != null) c.Skin = r.Skin;
            if (r.Hair != null) c.Hair = r.Hair;
            if (r.AlliesAndOrganizations != null) c.AlliesAndOrganizations = r.AlliesAndOrganizations;
            if (r.Backstory != null) c.Backstory = r.Backstory;
            if (r.Treasure != null) c.Treasure = r.Treasure;
            if (r.AdditionalNotes != null) c.AdditionalNotes = r.AdditionalNotes;
            if (r.DistinguishingMarks != null) c.DistinguishingMarks = r.DistinguishingMarks;

            if (r.SpellcastingClass.HasValue) c.SpellcastingClass = r.SpellcastingClass;
            if (r.PreparedLimit.HasValue) c.PreparedLimit = r.PreparedLimit.Value;

            if (r.SpellSlots != null)
            {
                c.SpellSlots.Clear();
                foreach (var (level, slot) in r.SpellSlots.Where(kv => kv.Key >= 1 && kv.Key <= 9))
                    c.SpellSlots.Add(new SpellSlotLevel
                    {
                        CharacterId = c.CharacterId,
                        Level = level,
                        Total = slot.Total,
                        Used = slot.Used
                    });
            }

            if (r.SkillProficiencies != null)
            {
                c.SkillProficiencies.Clear();
                foreach (var (skill, level) in r.SkillProficiencies)
                    c.SkillProficiencies.Add(new SkillProficiency
                    {
                        CharacterId = c.CharacterId,
                        Skill = skill,
                        Level = level
                    });
            }

            if (r.SaveProficiencies != null)
            {
                c.SaveProficiencies.Clear();
                foreach (var (ability, level) in r.SaveProficiencies)
                    c.SaveProficiencies.Add(new SaveProficiency
                    {
                        CharacterId = c.CharacterId,
                        Ability = ability,
                        Level = level
                    });
            }

            if (r.Attacks != null)
            {
                c.Attacks.Clear();
                foreach (var a in r.Attacks)
                    c.Attacks.Add(new Attack
                    {
                        AttackId = a.AttackId ?? Guid.NewGuid(),
                        CharacterId = c.CharacterId,
                        Name = a.Name,
                        AttackBonus = a.AttackBonus,
                        Damage = a.Damage
                    });
            }

            if (r.Spells != null)
            {
                c.Spells.Clear();
                foreach (var s in r.Spells)
                    c.Spells.Add(new Spell
                    {
                        SpellId = s.SpellId ?? Guid.NewGuid(),
                        CharacterId = c.CharacterId,
                        Name = s.Name,
                        Level = s.Level,
                        School = s.School,
                        CastingTime = s.CastingTime,
                        Range = s.Range,
                        Components = s.Components,
                        Duration = s.Duration,
                        Description = s.Description,
                        Prepared = s.Prepared
                    });
            }
        }

        private static void EnforceInvariants(Character c)
        {
            if (c.MaxHp < 1)
                throw new ValidationException("maxHp", "Max HP must be at least 1");
            if (c.CurrentHp > c.MaxHp)
                throw new ValidationException("currentHp", "Current HP cannot exceed max HP");

            if (c.HitDiceRemaining > c.Level)
                throw new ValidationException("hitDiceRemaining", "Hit dice remaining cannot exceed level");

            if (c.Class.HasValue && c.HitDieType.HasValue)
            {
                var expected = ClassCatalog.Info[c.Class.Value].HitDie;
                if (expected != c.HitDieType.Value)
                    throw new ValidationException("hitDieType", $"Hit die for class {c.Class} must be {expected}");
            }

            if (c.SpellcastingClass.HasValue)
            {
                var info = ClassCatalog.Info[c.SpellcastingClass.Value];
                if (info.Spellcasting == SpellcastingType.None)
                    throw new ValidationException("spellcastingClass", $"{c.SpellcastingClass} cannot cast spells");
            }

            foreach (var slot in c.SpellSlots)
            {
                if (slot.Total < 0 || slot.Total > 99)
                    throw new ValidationException("spellSlots", $"Spell slot total must be 0–99 at level {slot.Level}");
                if (slot.Used < 0 || slot.Used > slot.Total)
                    throw new ValidationException("spellSlots", $"Used spell slots cannot exceed total at level {slot.Level}");
            }
        }
    }
}
