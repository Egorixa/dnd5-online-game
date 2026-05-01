package com.example.android.net.dto;

/** DTO для эндпоинтов /auth/* (Identity модуль бекенда). */
public class AuthDtos {

    private AuthDtos() {}

    public static class RegisterRequest {
        public String username;
        public String password;

        public RegisterRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class RegisterResponse {
        public String userId;
        public String username;
    }

    public static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class LoginResponse {
        public String accessToken;
        public String tokenType;
        public int expiresInSeconds;
        public UserStatsDto stats;
    }

    public static class UserStatsDto {
        public int wins;
        public int defeats;
        public int masterCount;
    }

    public static class ProfileResponse {
        public String userId;
        public String username;
        public String registrationDate;   // ISO-8601
        public String theme;              // "light" / "dark"
        public UserStatsDto stats;
    }

    public static class UpdateThemeRequest {
        public String theme;

        public UpdateThemeRequest(String theme) { this.theme = theme; }
    }
}
