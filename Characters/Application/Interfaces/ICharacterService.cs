using Characters.Application.DTOs;

namespace Characters.Application.Interfaces
{
    public interface ICharacterService
    {
        // Active sheets attached to a session (room)
        Task<CharacterResponse> CreateAsync(Guid userId, Guid roomId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task<CharacterResponse> GetAsync(Guid userId, Guid roomId, Guid characterId, CancellationToken ct = default);
        Task<CharacterResponse> UpdateAsync(Guid userId, Guid roomId, Guid characterId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task DeleteAsync(Guid userId, Guid roomId, Guid characterId, CancellationToken ct = default);
        Task<List<CharacterResponse>> ListByRoomAsync(Guid userId, Guid roomId, CancellationToken ct = default);

        // Templates stored in the user's account, independent of any session
        Task<CharacterResponse> CreateTemplateAsync(Guid userId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task<List<CharacterResponse>> ListTemplatesAsync(Guid userId, CancellationToken ct = default);
        Task<CharacterResponse> GetTemplateAsync(Guid userId, Guid characterId, CancellationToken ct = default);
        Task<CharacterResponse> UpdateTemplateAsync(Guid userId, Guid characterId, CharacterUpsertRequest request, CancellationToken ct = default);
        Task DeleteTemplateAsync(Guid userId, Guid characterId, CancellationToken ct = default);
    }
}
