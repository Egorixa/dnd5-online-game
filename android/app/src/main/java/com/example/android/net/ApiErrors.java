package com.example.android.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Response;

/**
 * Утилиты для извлечения сообщения об ошибке из Retrofit Response.
 * Backend (см. Shared/Errors/ApiError.cs и GlobalExceptionMiddleware) возвращает JSON
 * вида { "code": "...", "message": "..." }.
 */
public final class ApiErrors {

    private static final Gson GSON = new Gson();

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
                    return obj.get("message").getAsString();
                }
            } catch (Exception ignored) {
                // not JSON — return raw body
            }
            return body;
        } catch (IOException e) {
            return fallback;
        }
    }

    public static String fromThrowable(Throwable t, String fallback) {
        if (t == null) return fallback;
        String msg = t.getMessage();
        return msg != null && !msg.isEmpty() ? msg : fallback;
    }
}
