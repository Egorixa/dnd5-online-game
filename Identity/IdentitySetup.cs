using FluentValidation;
using Identity.Application.DTOs;
using Identity.Application.Interfaces;
using Identity.Application.Services;
using Identity.Application.Validators;
using Identity.Data;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace Identity
{
    public static class IdentitySetup
    {
        public static IServiceCollection AddIdentityModule(this IServiceCollection services, IConfiguration configuration)
        {
            var connectionString = configuration.GetConnectionString("DefaultConnection")
                ?? throw new InvalidOperationException("ConnectionStrings:DefaultConnection is not configured");

            services.AddDbContext<IdentityDbContext>(options =>
                options.UseNpgsql(connectionString, npgsql =>
                    npgsql.MigrationsHistoryTable("__EFMigrationsHistory", "identity")));

            services.AddScoped<IAuthRepository, AuthRepository>();
            services.AddScoped<IAuthService, AuthService>();
            services.AddScoped<IUserStatsService, UserStatsService>();

            services.AddScoped<IValidator<RegisterRequest>, RegisterRequestValidator>();
            services.AddScoped<IValidator<LoginRequest>, LoginRequestValidator>();

            return services;
        }
    }
}
