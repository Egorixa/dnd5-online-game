package com.example.android.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public final class ApiErrors {

    private static final Gson GSON = new Gson();


    private static final Map<String, String> RU = new HashMap<>();
    static {
        RU.put("Cannot roll dice in a finished room", "Нельзя бросать кубики в завершённой комнате");
        RU.put("Cannot join a finished room", "Нельзя войти в завершённую комнату");
        RU.put("Room is finished", "Сессия завершена");
        RU.put("Forbidden", "Доступ запрещён");
        RU.put("Unauthorized", "Не авторизованы");
        RU.put("User is already in the room", "Вы уже в этой комнате");
        RU.put("Master cannot join own room as player", "Мастер не может войти игроком в свою комнату");
        RU.put("Room not found", "Комната не найдена");
        RU.put("Character not found", "Персонаж не найден");
        RU.put("Invalid credentials", "Неверный логин или пароль");
        RU.put("Username already exists", "Такой логин уже занят");
        RU.put("Validation failed", "Ошибка проверки данных");
        RU.put("One or more validation errors occurred.", "Ошибка проверки данных");
        RU.put("Invalid value", "Неверное значение");
    }

    private ApiErrors() {}

    public static String extract(Response<?> response) {
        return extract(response, "ошибка");
    }

    public static String fromThrowable(Throwable t) {
        return fromThrowable(t, "ошибка сети");
    }

    public static String extract(Response<?> response, String fallback) {
        if (response == null) return fallback;
        if (response.errorBody() == null) return "HTTP " + response.code() + ": " + fallback;
        try {
            String body = response.errorBody().string();
            if (body == null || body.isEmpty()) {
                return "HTTP " + response.code() + ": " + fallback;
            }
            try {
                JsonObject obj = GSON.fromJson(body, JsonObject.class);
                if (obj != null && obj.has("message")) {
                    return localize(obj.get("message").getAsString());
                }
            } catch (Exception ignored) {

            }
            return localize(body);
        } catch (IOException e) {
            return fallback;
        }
    }

    public static String fromThrowable(Throwable t, String fallback) {
        if (t == null) return fallback;
        String msg = t.getMessage();
        return msg != null && !msg.isEmpty() ? localize(msg) : fallback;
    }

    private static String localize(String msg) {
        if (msg == null) return null;
        String trimmed = msg.trim();
        String hit = RU.get(trimmed);
        if (hit != null) return hit;

        for (Map.Entry<String, String> e : RU.entrySet()) {
            if (trimmed.contains(e.getKey())) return e.getValue();
        }
        return msg;
    }
}
