package com.example.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Управление сессией пользователя и настройками (тема) через SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "dnd5_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_THEME_DARK = "theme_dark";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void login(int userId, String username) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public void logout() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getInt(KEY_USER_ID, -1) != -1;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean(KEY_THEME_DARK, false);
    }

    public void setDarkTheme(boolean dark) {
        prefs.edit().putBoolean(KEY_THEME_DARK, dark).apply();
        applyTheme(dark);
    }

    public void applySavedTheme() {
        applyTheme(isDarkTheme());
    }

    private void applyTheme(boolean dark) {
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
