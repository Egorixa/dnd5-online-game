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

            modelBuilder.Entity<User>(entity =>
            {
                entity.Property(u => u.ThemeDesign).HasConversion<string>();
                entity.HasIndex(u => u.Login)
                    .IsUnique()
                    .HasDatabaseName("ix_users_login_unique");
            });
        }
    }
}