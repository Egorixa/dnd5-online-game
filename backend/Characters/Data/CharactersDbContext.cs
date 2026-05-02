using Characters.Entities;
using Microsoft.EntityFrameworkCore;

namespace Characters.Data
{
    public class CharactersDbContext : DbContext
    {
        public CharactersDbContext(DbContextOptions<CharactersDbContext> options) : base(options) { }

        public DbSet<Character> Characters => Set<Character>();
        public DbSet<SkillProficiency> SkillProficiencies => Set<SkillProficiency>();
        public DbSet<SaveProficiency> SaveProficiencies => Set<SaveProficiency>();
        public DbSet<Attack> Attacks => Set<Attack>();
        public DbSet<Spell> Spells => Set<Spell>();

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.HasDefaultSchema("characters");

            modelBuilder.Entity<Character>(e =>
            {
                e.Property(x => x.Race).HasConversion<string>();
                e.Property(x => x.Class).HasConversion<string>();
                e.Property(x => x.Background).HasConversion<string>();
                e.Property(x => x.Alignment).HasConversion<string>();
                e.Property(x => x.HitDieType).HasConversion<string>();
                e.Property(x => x.SpellcastingClass).HasConversion<string>();
                e.Property(x => x.RowVersion).IsConcurrencyToken();
                e.HasIndex(x => x.RoomId);
                e.HasMany(x => x.SkillProficiencies)
                    .WithOne()
                    .HasForeignKey(x => x.CharacterId)
                    .OnDelete(DeleteBehavior.Cascade);
                e.HasMany(x => x.SaveProficiencies)
                    .WithOne()
                    .HasForeignKey(x => x.CharacterId)
                    .OnDelete(DeleteBehavior.Cascade);
                e.HasMany(x => x.Attacks)
                    .WithOne()
                    .HasForeignKey(x => x.CharacterId)
                    .OnDelete(DeleteBehavior.Cascade);
                e.HasMany(x => x.Spells)
                    .WithOne()
                    .HasForeignKey(x => x.CharacterId)
                    .OnDelete(DeleteBehavior.Cascade);
            });

            modelBuilder.Entity<SkillProficiency>(e =>
            {
                e.HasKey(x => new { x.CharacterId, x.Skill });
                e.Property(x => x.Skill).HasConversion<string>();
                e.Property(x => x.Level).HasConversion<string>();
            });

            modelBuilder.Entity<SaveProficiency>(e =>
            {
                e.HasKey(x => new { x.CharacterId, x.Ability });
                e.Property(x => x.Ability).HasConversion<string>();
                e.Property(x => x.Level).HasConversion<string>();
            });
        }
    }
}
