using Identity.Application.Interfaces;
using Identity.Data;
using Microsoft.EntityFrameworkCore;

namespace Identity.Application.Services
{
    public class UserLookupService : IUserLookupService
    {
        private readonly IdentityDbContext _context;

        public UserLookupService(IdentityDbContext context)
        {
            _context = context;
        }

        public Task<string?> GetUsernameAsync(Guid userId, CancellationToken ct = default)
            => _context.Users
                .Where(u => u.UserId == userId)
                .Select(u => u.Login)
                .FirstOrDefaultAsync(ct);

        public async Task<Dictionary<Guid, string>> GetUsernamesAsync(IEnumerable<Guid> userIds, CancellationToken ct = default)
        {
            var ids = userIds.Distinct().ToList();
            if (ids.Count == 0) return new Dictionary<Guid, string>();

            return await _context.Users
                .Where(u => ids.Contains(u.UserId))
                .ToDictionaryAsync(u => u.UserId, u => u.Login, ct);
        }
    }
}
