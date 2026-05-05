package com.example.android.net;

/**
 * Конфигурация подключения мобильного клиента к серверной части DnD5.
 *
 * BASE_URL должен указывать на корень API. Для эмулятора Android по умолчанию
 * используется адрес 10.0.2.2 — это localhost хост-машины. Backend поднят
 * через Docker и пробрасывает контейнерный 8080 на порт 5000 хоста
 * (см. backend/docker-compose.yml).
 *
 * При запуске на физическом устройстве замените BASE_URL на реальный адрес сервера.
 */
public final class ApiConfig {

    private ApiConfig() {}

    /** Базовый URL сервера. Должен заканчиваться на «/». */
    public static final String BASE_URL = "http://10.0.2.2:5000/";

    /** URL SignalR-хаба (см. RealTimeSetup.MapRealTimeHubs). */
    public static final String HUB_URL = BASE_URL + "hubs/session";
}
