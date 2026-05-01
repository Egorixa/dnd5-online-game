package com.example.android.net;

/**
 * Конфигурация подключения мобильного клиента к серверной части DnD5.
 *
 * BASE_URL должен указывать на корень API. Для эмулятора Android по умолчанию
 * используется адрес 10.0.2.2 — это localhost хост-машины, на которой запущен
 * dotnet-сервер (порт 5283 — см. backend/DnD_backend/Properties/launchSettings.json).
 *
 * При запуске на физическом устройстве замените BASE_URL на реальный адрес сервера.
 */
public final class ApiConfig {

    private ApiConfig() {}

    /** Базовый URL сервера. Должен заканчиваться на «/». */
    public static final String BASE_URL = "http://10.0.2.2:5283/";

    /** URL SignalR-хаба (см. RealTimeSetup.MapRealTimeHubs). */
    public static final String HUB_URL = BASE_URL + "hubs/session";
}
