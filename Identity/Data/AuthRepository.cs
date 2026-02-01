using Identity.Application.Interfaces;
using Identity.Entities;
using Microsoft.EntityFrameworkCore;

namespace Identity.Data // Или Identity.Infrastructure.Repositories
{
    public class AuthRepository : IAuthRepository
    {
        private readonly IdentityDbContext _context;

        public AuthRepository(IdentityDbContext context)
        {
            _context = context;
        }

        public async Task<User?> GetByLoginAsync(string login)
        {
            return await _context.Users
                .FirstOrDefaultAsync(u => u.Login == login);
        }

        public async Task<bool> IsLoginTakenAsync(string login)
        {
            return await _context.Users
                .AnyAsync(u => u.Login == login);
        }

        public async Task AddUserAsync(User user)
        {
            await _context.Users.AddAsync(user);
        }

        public async Task SaveChangesAsync()
        {
            await _context.SaveChangesAsync();
        }
    }
}