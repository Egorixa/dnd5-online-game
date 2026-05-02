using Identity.Entities;

namespace Identity.Application.Interfaces
{
    public interface IAuthRepository
    {
        Task<User?> GetByLoginAsync(string login, CancellationToken ct = default);
        Task<User?> GetByIdAsync(Guid userId, CancellationToken ct = default);
        Task<bool> IsLoginTakenAsync(string login, CancellationToken ct = default);
        Task AddUserAsync(User user, CancellationToken ct = default);
        Task SaveChangesAsync(CancellationToken ct = default);
    }
}
