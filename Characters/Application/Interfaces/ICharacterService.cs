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
    }
}
