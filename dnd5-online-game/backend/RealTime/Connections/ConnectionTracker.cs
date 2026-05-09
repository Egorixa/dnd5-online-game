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
        private readonly ConcurrentDictionary<Guid, ConcurrentBag<string>> _map = new();

        public void Track(Guid userId, string connectionId)
        {
            _map.GetOrAdd(userId, _ => new ConcurrentBag<string>()).Add(connectionId);
        }

        public void Untrack(Guid userId, string connectionId)
        {
            if (_map.TryGetValue(userId, out var bag))
            {
                var updated = new ConcurrentBag<string>(bag.Where(c => c != connectionId));
                _map.TryUpdate(userId, updated, bag);
            }
        }

        public IReadOnlyList<string> GetConnections(Guid userId)
        {
            if (_map.TryGetValue(userId, out var bag))
                return bag.ToList();
            return Array.Empty<string>();
        }
    }
}
