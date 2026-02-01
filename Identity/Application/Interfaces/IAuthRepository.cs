using Identity.Entities;

namespace Identity.Application.Interfaces
{
    public interface IAuthRepository
    {
        Task<User?> GetByLoginAsync(string login);

        Task<bool> IsLoginTakenAsync(string login);

        Task AddUserAsync(User user);

        Task SaveChangesAsync();
    }
}