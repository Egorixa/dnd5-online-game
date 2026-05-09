using System.Text.Json;
using System.Text.Json.Serialization;
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
            services.AddSignalR()
                .AddJsonProtocol(o =>
                {
                    o.PayloadSerializerOptions.Converters.Add(new JsonStringEnumConverter());
                    o.PayloadSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
                });
            services.AddSingleton<IRoomNotifier, SignalRRoomNotifier>();
            return services;
        }

        public static IEndpointRouteBuilder MapRealTimeHubs(this IEndpointRouteBuilder endpoints)
        {
            endpoints.MapHub<RoomHub>("/hubs/session");
            return endpoints;
        }
    }
}
