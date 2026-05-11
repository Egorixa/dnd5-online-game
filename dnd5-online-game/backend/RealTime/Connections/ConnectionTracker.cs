using System.Collections.Concurrent;

namespace RealTime.Connections
{
    public interface IConnectionTracker
    {
        void Track(Guid userId, string connectionId);
        void Untrack(Guid userId, string connectionId);
        IReadOnlyList<string> GetConnections(Guid userId);
    }

    public class ConnectionTracker : IConnectionTracker
    {
        private readonly ConcurrentDictionary<Guid, ConcurrentDictionary<string, byte>> _map = new();

        public void Track(Guid userId, string connectionId)
        {
            _map.GetOrAdd(userId, _ => new ConcurrentDictionary<string, byte>())
                .TryAdd(connectionId, 0);
        }

        public void Untrack(Guid userId, string connectionId)
        {
            if (_map.TryGetValue(userId, out var set))
            {
                set.TryRemove(connectionId, out _);
                if (set.IsEmpty)
                    _map.TryRemove(userId, out _);
            }
        }

        public IReadOnlyList<string> GetConnections(Guid userId)
        {
            if (_map.TryGetValue(userId, out var set))
                return set.Keys.ToList();
            return Array.Empty<string>();
        }
    }
}
