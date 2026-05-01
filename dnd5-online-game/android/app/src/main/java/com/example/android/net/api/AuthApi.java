package com.example.android.net.api;

import com.example.android.net.dto.AuthDtos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApi {

    @POST("auth/register")
    Call<AuthDtos.RegisterResponse> register(@Body AuthDtos.RegisterRequest body);

    @POST("auth/login")
    Call<AuthDtos.LoginResponse> login(@Body AuthDtos.LoginRequest body);

    @GET("auth/profile")
    Call<AuthDtos.ProfileResponse> profile();

    @PUT("auth/theme")
    Call<AuthDtos.ProfileResponse> updateTheme(@Body AuthDtos.UpdateThemeRequest body);

    @POST("auth/logout")
    Call<Void> logout();
}
