using Characters.Application.DTOs;

namespace Characters.Application.Interfaces
{
    public interface ICharacterService
    {

        Task<CharacterResponse> CreateAsync(Guid userId, Guid roomId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task<CharacterResponse> GetAsync(Guid userId, Guid roomId, Guid characterId, CancellationToken ct = default);
        Task<CharacterResponse> UpdateAsync(Guid userId, Guid roomId, Guid characterId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task DeleteAsync(Guid userId, Guid roomId, Guid characterId, CancellationToken ct = default);
        Task<List<CharacterResponse>> ListByRoomAsync(Guid userId, Guid roomId, CancellationToken ct = default);

        Task<AttackDto> AddAttackAsync(Guid userId, Guid roomId, Guid characterId, AttackDto dto, CancellationToken ct = default);
        Task<AttackDto> UpdateAttackAsync(Guid userId, Guid roomId, Guid characterId, Guid attackId, AttackDto dto, CancellationToken ct = default);
        Task DeleteAttackAsync(Guid userId, Guid roomId, Guid characterId, Guid attackId, CancellationToken ct = default);

        Task<SpellResponseDto> AddSpellAsync(Guid userId, Guid roomId, Guid characterId, SpellDto dto, CancellationToken ct = default);
        Task<SpellResponseDto> UpdateSpellAsync(Guid userId, Guid roomId, Guid characterId, Guid spellId, SpellDto dto, CancellationToken ct = default);
        Task DeleteSpellAsync(Guid userId, Guid roomId, Guid characterId, Guid spellId, CancellationToken ct = default);

        Task<CharacterResponse> CreateTemplateAsync(Guid userId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task<List<CharacterResponse>> ListTemplatesAsync(Guid userId, CancellationToken ct = default);
        Task<CharacterResponse> GetTemplateAsync(Guid userId, Guid characterId, CancellationToken ct = default);
        Task<CharacterResponse> UpdateTemplateAsync(Guid userId, Guid characterId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task DeleteTemplateAsync(Guid userId, Guid characterId, CancellationToken ct = default);
    }
}
