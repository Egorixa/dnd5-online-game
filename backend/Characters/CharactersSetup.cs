using Characters.Application.DTOs;
using Characters.Application.Interfaces;
using Characters.Application.Services;
using Characters.Application.Validators;
using Characters.Data;
using FluentValidation;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace Characters
{
    public static class CharactersSetup
    {
        public static IServiceCollection AddCharactersModule(this IServiceCollection services, IConfiguration configuration)
        {
            var connectionString = configuration.GetConnectionString("DefaultConnection")
                ?? throw new InvalidOperationException("ConnectionStrings:DefaultConnection is not configured");

            services.AddDbContext<CharactersDbContext>(options =>
                options.UseNpgsql(connectionString, npgsql =>
                    npgsql.MigrationsHistoryTable("__EFMigrationsHistory", "characters")));

            services.AddScoped<ICharacterService, CharacterService>();
            services.AddScoped<IValidator<CharacterUpsertRequest>, CharacterUpsertValidator>();

            return services;
        }
    }
}
