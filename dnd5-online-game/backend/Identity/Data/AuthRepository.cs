using Identity.Application.Interfaces;
using Identity.Entities;
using Microsoft.EntityFrameworkCore;

namespace Identity.Data
{
    public class AuthRepository : IAuthRepository
    {
        private readonly IdentityDbContext _context;

        public AuthRepository(IdentityDbContext context)
        {
            _context = context;
        }

        public Task<User?> GetByLoginAsync(string login, CancellationToken ct = default)
            => _context.Users.FirstOrDefaultAsync(u => u.Login == login, ct);

        public Task<User?> GetByIdAsync(Guid userId, CancellationToken ct = default)
            => _context.Users.FirstOrDefaultAsync(u => u.UserId == userId, ct);

        public Task<bool> IsLoginTakenAsync(string login, CancellationToken ct = default)
            => _context.Users.AnyAsync(u => u.Login == login, ct);

        public async Task AddUserAsync(User user, CancellationToken ct = default)
        {
            await _context.Users.AddAsync(user, ct);
        }

        public Task SaveChangesAsync(CancellationToken ct = default)
            => _context.SaveChangesAsync(ct);
    }
}
