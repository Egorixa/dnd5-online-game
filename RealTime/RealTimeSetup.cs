using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.DependencyInjection;
using RealTime.Hubs;
using RealTime.Notifications;
using Shared.RealTime;

namespace RealTime
{
    public static class RealTimeSetup
    {
        public static IServiceCollection AddRealTimeModule(this IServiceCollection services)
        {
            services.AddSignalR();
            services.AddSingleton<IRoomNotifier, SignalRRoomNotifier>();
            return services;
        }

        public static IEndpointRouteBuilder MapRealTimeHubs(this IEndpointRouteBuilder endpoints)
        {
            endpoints.MapHub<RoomHub>("/hubs/room");
            return endpoints;
        }
    }
}
