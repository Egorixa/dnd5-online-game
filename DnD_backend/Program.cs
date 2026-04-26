using System.Text.Json.Serialization;
using Characters;
using Characters.Data;
using Identity;
using Identity.Data;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi.Models;
using RealTime;
using Rooms;
using Rooms.Data;
using Shared.Auth;
using Shared.Errors;

var builder = WebApplication.CreateBuilder(args);

// ---- modules ----
builder.Services.AddSharedAuth(builder.Configuration);
builder.Services.AddIdentityModule(builder.Configuration);
builder.Services.AddRoomsModule(builder.Configuration);
builder.Services.AddCharactersModule(builder.Configuration);
builder.Services.AddRealTimeModule();

// ---- web ----
builder.Services
    .AddControllers()
    .AddJsonOptions(o =>
    {
        o.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
    });

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "DnD5 List Backend",
        Version = "v1",
        Description = "Online character sheet for DnD5 — server part."
    });

    c.AddSecurityDefinition(JwtBearerDefaults.AuthenticationScheme, new OpenApiSecurityScheme
    {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header,
        Description = "Provide the JWT obtained from /auth/login"
    });

    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = JwtBearerDefaults.AuthenticationScheme
                }
            },
            Array.Empty<string>()
        }
    });
});

var app = builder.Build();

// ---- migrations ----
using (var scope = app.Services.CreateScope())
{
    var sp = scope.ServiceProvider;
    sp.GetRequiredService<IdentityDbContext>().Database.Migrate();
    sp.GetRequiredService<RoomsDbContext>().Database.Migrate();
    sp.GetRequiredService<CharactersDbContext>().Database.Migrate();
}

// ---- pipeline ----
app.UseMiddleware<GlobalExceptionMiddleware>();

app.UseSwagger();
app.UseSwaggerUI();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();
app.MapRealTimeHubs();

app.Run();
