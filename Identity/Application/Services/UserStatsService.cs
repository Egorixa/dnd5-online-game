using Identity.Application.Interfaces;
using Identity.Data;
using Microsoft.EntityFrameworkCore;

namespace Identity.Application.Services
{
    public class UserStatsService : IUserStatsService
    {
        private readonly IdentityDbContext _context;

        public UserStatsService(IdentityDbContext context)
        {
            _context = context;
        }

        public async Task IncrementWinsAsync(IEnumerable<Guid> userIds, CancellationToken ct = default)
        {
            var ids = userIds.Distinct().ToList();
            if (ids.Count == 0) return;
            await _context.Users
                .Where(u => ids.Contains(u.UserId))
                .ExecuteUpdateAsync(s => s.SetProperty(u => u.Wins, u => u.Wins + 1), ct);
        }

        public async Task IncrementDefeatsAsync(IEnumerable<Guid> userIds, CancellationToken ct = default)
        {
            var ids = userIds.Distinct().ToList();
            if (ids.Count == 0) return;
            await _context.Users
                .Where(u => ids.Contains(u.UserId))
                .ExecuteUpdateAsync(s => s.SetProperty(u => u.Defeats, u => u.Defeats + 1), ct);
        }

        public async Task IncrementMasterCountAsync(Guid userId, CancellationToken ct = default)
        {
            await _context.Users
                .Where(u => u.UserId == userId)
                .ExecuteUpdateAsync(s => s.SetProperty(u => u.CountMasterTime, u => u.CountMasterTime + 1), ct);
        }
    }
}
