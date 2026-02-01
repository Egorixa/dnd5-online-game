# 1. Сборка (Build)
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Копируем файлы проектов (чтобы восстановить зависимости)
COPY ["DnD_backend/DnD_backend.csproj", "DnD_backend/"]
COPY ["Identity/Identity.csproj", "Identity/"]

# Восстанавливаем пакеты (NuGet restore)
RUN dotnet restore "DnD_backend/DnD_backend.csproj"

# Копируем весь остальной код
COPY . .

# Собираем проект
WORKDIR "/src/DnD_backend"
RUN dotnet build "DnD_backend.csproj" -c Release -o /app/build

# Публикуем (готовим файлы для запуска)
FROM build AS publish
RUN dotnet publish "DnD_backend.csproj" -c Release -o /app/publish /p:UseAppHost=false

# 2. Запуск (Runtime)
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app
EXPOSE 8080
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "DnD_backend.dll"]