using FluentValidation;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Rooms.Application.DTOs;
using Rooms.Application.Interfaces;
using Rooms.Application.Services;
using Rooms.Application.Validators;
using Rooms.Data;

namespace Rooms
{
    public static class RoomsSetup
    {
        public static IServiceCollection AddRoomsModule(this IServiceCollection services, IConfiguration configuration)
        {
            var connectionString = configuration.GetConnectionString("DefaultConnection")
                ?? throw new InvalidOperationException("ConnectionStrings:DefaultConnection is not configured");

            services.AddDbContext<RoomsDbContext>(options =>
                options.UseNpgsql(connectionString, npgsql =>
                    npgsql.MigrationsHistoryTable("__EFMigrationsHistory", "rooms")));

            services.AddScoped<IRoomAccessChecker, RoomAccessChecker>();
            services.AddScoped<IRoomService, RoomService>();
            services.AddScoped<IDiceService, DiceService>();

            services.AddScoped<IValidator<CreateRoomRequest>, CreateRoomRequestValidator>();

            return services;
        }
    }
}
