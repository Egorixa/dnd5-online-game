package com.example.android.net;

import android.content.Context;

import com.example.android.data.SessionManager;
import com.example.android.net.api.AuthApi;
import com.example.android.net.api.CharactersApi;
import com.example.android.net.api.DiceApi;
import com.example.android.net.api.RoomsApi;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton-фабрика Retrofit-клиента и API-интерфейсов.
 * Создаётся при первом обращении и переиспользует один OkHttpClient/Retrofit.
 */
public final class ApiClient {

    private static volatile ApiClient INSTANCE;

    private final Retrofit retrofit;

    private final AuthApi authApi;
    private final RoomsApi roomsApi;
    private final CharactersApi charactersApi;
    private final DiceApi diceApi;

    private ApiClient(Context appContext) {
        SessionManager session = new SessionManager(appContext);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(session))
                .addInterceptor(logging)
                .build();

        // Backend выдаёт JSON в camelCase (default для System.Text.Json),
        // потому используем стандартное сопоставление.
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .serializeNulls()
                .create();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.authApi = retrofit.create(AuthApi.class);
        this.roomsApi = retrofit.create(RoomsApi.class);
        this.charactersApi = retrofit.create(CharactersApi.class);
        this.diceApi = retrofit.create(DiceApi.class);
    }

    public static ApiClient get(Context context) {
        ApiClient local = INSTANCE;
        if (local == null) {
            synchronized (ApiClient.class) {
                local = INSTANCE;
                if (local == null) {
                    local = new ApiClient(context.getApplicationContext());
                    INSTANCE = local;
                }
            }
        }
        return local;
    }

    public AuthApi auth() { return authApi; }
    public RoomsApi rooms() { return roomsApi; }
    public CharactersApi characters() { return charactersApi; }
    public DiceApi dice() { return diceApi; }
    public Retrofit retrofit() { return retrofit; }
}
