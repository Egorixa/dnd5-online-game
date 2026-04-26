using Microsoft.EntityFrameworkCore;
using Rooms.Entities;

namespace Rooms.Data
{
    public class RoomsDbContext : DbContext
    {
        public RoomsDbContext(DbContextOptions<RoomsDbContext> options) : base(options) { }

        public DbSet<Room> Rooms => Set<Room>();
        public DbSet<RoomParticipant> Participants => Set<RoomParticipant>();
        public DbSet<RoomEvent> Events => Set<RoomEvent>();

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.HasDefaultSchema("rooms");

            modelBuilder.Entity<Room>(e =>
            {
                e.Property(x => x.AccessMode).HasConversion<string>();
                e.Property(x => x.Status).HasConversion<string>();
                e.HasIndex(x => x.RoomCode).IsUnique();
                e.HasMany(x => x.Participants)
                 .WithOne(p => p.Room!)
                 .HasForeignKey(p => p.RoomId)
                 .OnDelete(DeleteBehavior.Cascade);
            });

            modelBuilder.Entity<RoomParticipant>(e =>
            {
                e.Property(x => x.Role).HasConversion<string>();
                e.HasIndex(x => new { x.RoomId, x.UserId, x.LeftAt });
            });

            modelBuilder.Entity<RoomEvent>(e =>
            {
                e.Property(x => x.Type).HasConversion<string>();
                e.HasIndex(x => new { x.RoomId, x.CreatedAt });
            });
        }
    }
}
