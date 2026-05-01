package com.example.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Управление сессией пользователя и настройками через SharedPreferences.
 *
 * Хранит:
 *   - JWT-токен (access_token), полученный от бекенда (см. /auth/login),
 *   - идентификатор пользователя на сервере (serverUserId, GUID-строка),
 *   - локальный кэш-идентификатор (для Room),
 *   - имя пользователя, тему оформления,
 *   - последнее активное подключение к комнате (roomId, roomCode).
 */
public class SessionManager {

    private static final String PREF_NAME = "dnd5_session";
    private static final String KEY_USER_ID = "user_id";          // локальный int-id (Room PK)
    private static final String KEY_SERVER_USER_ID = "server_user_id"; // GUID на сервере
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_THEME = "theme";              // "light" | "dark"
    private static final String KEY_ACTIVE_ROOM_ID = "active_room_id";
    private static final String KEY_ACTIVE_ROOM_CODE = "active_room_code";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ── Сессия ────────────────────────────────────────────────────────────

    public void login(int localUserId, String serverUserId, String username, String token) {
        prefs.edit()
                .putInt(KEY_USER_ID, localUserId)
                .putString(KEY_SERVER_USER_ID, serverUserId == null ? "" : serverUserId)
                .putString(KEY_USERNAME, username == null ? "" : username)
                .putString(KEY_TOKEN, token == null ? "" : token)
                .apply();
    }

    /** Сохранить только локальный id (без сервера) — для офлайн-режима. */
    public void loginLocal(int localUserId, String username) {
        prefs.edit()
                .putInt(KEY_USER_ID, localUserId)
                .putString(KEY_USERNAME, username == null ? "" : username)
                .remove(KEY_SERVER_USER_ID)
                .remove(KEY_TOKEN)
                .apply();
    }

    public void updateToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token == null ? "" : token).apply();
    }

    public void updateServerUserId(String serverUserId) {
        prefs.edit().putString(KEY_SERVER_USER_ID, serverUserId == null ? "" : serverUserId).apply();
    }

    public void logout() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_SERVER_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_TOKEN)
                .remove(KEY_ACTIVE_ROOM_ID)
                .remove(KEY_ACTIVE_ROOM_CODE)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getInt(KEY_USER_ID, -1) != -1;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getServerUserId() {
        return prefs.getString(KEY_SERVER_USER_ID, "");
    }

    public boolean hasServerSession() {
        return !TextUtils.isEmpty(getToken()) && !TextUtils.isEmpty(getServerUserId());
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // ── Активная комната ──────────────────────────────────────────────────

    public void setActiveRoom(String roomId, String roomCode) {
        prefs.edit()
                .putString(KEY_ACTIVE_ROOM_ID, roomId == null ? "" : roomId)
                .putString(KEY_ACTIVE_ROOM_CODE, roomCode == null ? "" : roomCode)
                .apply();
    }

    public void clearActiveRoom() {
        prefs.edit()
                .remove(KEY_ACTIVE_ROOM_ID)
                .remove(KEY_ACTIVE_ROOM_CODE)
                .apply();
    }

    public String getActiveRoomId() { return prefs.getString(KEY_ACTIVE_ROOM_ID, ""); }
    public String getActiveRoomCode() { return prefs.getString(KEY_ACTIVE_ROOM_CODE, ""); }

    // ── Тема ──────────────────────────────────────────────────────────────

    /** "light" / "dark" — серверный формат. */
    public String getThemeRaw() {
        return prefs.getString(KEY_THEME, "light");
    }

    public void setThemeRaw(String theme) {
        String t = "dark".equalsIgnoreCase(theme) ? "dark" : "light";
        prefs.edit().putString(KEY_THEME, t).apply();
        applyTheme("dark".equals(t));
    }

    public boolean isDarkTheme() {
        return "dark".equals(getThemeRaw());
    }

    public void setDarkTheme(boolean dark) {
        setThemeRaw(dark ? "dark" : "light");
    }

    public void applySavedTheme() {
        applyTheme(isDarkTheme());
    }

    private void applyTheme(boolean dark) {
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
