using Identity.Application.Interfaces;
using Identity.Application.Services;
using Identity.Data;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);


var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");

Console.WriteLine($"--> [1/3] Настройка сервисов. Строка подключения: {connectionString}");

builder.Services.AddDbContext<IdentityDbContext>(options =>
    options.UseNpgsql(connectionString));

builder.Services.AddScoped<IAuthRepository, AuthRepository>();
builder.Services.AddScoped<IAuthService, AuthService>();

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

Console.WriteLine("--> [2/3] Применение миграций...");
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    try
    {
        var context = services.GetRequiredService<IdentityDbContext>();
        context.Database.Migrate();
        Console.WriteLine("--> УСПЕХ: Миграции применены!");
    }
    catch (Exception ex)
    {
        var logger = services.GetRequiredService<ILogger<Program>>();
        logger.LogError(ex, "--> ОШИБКА: Миграция не удалась (Docker перезапустится).");
        throw;
    }
}


app.UseSwagger();
app.UseSwaggerUI();


app.MapControllers();

Console.WriteLine("--> [3/3] ЗАПУСК ПРИЛОЖЕНИЯ. Слушаем порт 8080...");

app.Run();