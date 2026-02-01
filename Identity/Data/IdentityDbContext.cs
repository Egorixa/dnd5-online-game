using Identity.Entities;
using Microsoft.EntityFrameworkCore;

namespace Identity.Data
{
    public class IdentityDbContext : DbContext
    {
        public IdentityDbContext(DbContextOptions<IdentityDbContext> options)
            : base(options)
        {
        }
        public DbSet<User> Users { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.HasDefaultSchema("identity");

            modelBuilder.Entity<User>()
                .Property(u => u.ThemeDesign)
                .HasConversion<string>();
        }
    }
}