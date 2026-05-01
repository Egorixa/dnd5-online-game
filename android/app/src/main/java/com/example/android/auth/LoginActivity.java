package com.example.android.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.MainActivity;
import com.example.android.R;
import com.example.android.data.AppDatabase;
import com.example.android.data.SessionManager;
import com.example.android.data.dao.UserDao;
import com.example.android.data.model.User;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.dto.AuthDtos;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SessionManager(this).applySavedTheme();
        setContentView(R.layout.activity_login);

        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_to_register);

        btnLogin.setOnClickListener(v -> tryLogin());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void tryLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        final String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        final String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Введите имя пользователя");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Введите пароль");
            return;
        }

        btnLogin.setEnabled(false);
        AuthDtos.LoginRequest req = new AuthDtos.LoginRequest(username, password);
        ApiClient.get(this).auth().login(req).enqueue(new Callback<AuthDtos.LoginResponse>() {
            @Override
            public void onResponse(Call<AuthDtos.LoginResponse> call, Response<AuthDtos.LoginResponse> response) {
                btnLogin.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    String msg = ApiErrors.extract(response, "Неверный логин или пароль");
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthDtos.LoginResponse body = response.body();
                // Подтянуть профиль, чтобы узнать userId/тему
                fetchProfileAndFinish(username, password, body.accessToken);
            }

            @Override
            public void onFailure(Call<AuthDtos.LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Не удалось подключиться: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchProfileAndFinish(String username, String password, String token) {
        // Сохраняем токен заранее, чтобы AuthInterceptor подставил его в /auth/profile.
        SessionManager session = new SessionManager(this);
        session.updateToken(token);

        ApiClient.get(this).auth().profile().enqueue(new Callback<AuthDtos.ProfileResponse>() {
            @Override
            public void onResponse(Call<AuthDtos.ProfileResponse> call, Response<AuthDtos.ProfileResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this,
                            ApiErrors.extract(response, "Не удалось получить профиль"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthDtos.ProfileResponse profile = response.body();

                // Локальный кэш User (для совместимости с CharacterDao по userId).
                final int[] localId = {-1};
                new Thread(() -> {
                    UserDao dao = AppDatabase.getInstance(LoginActivity.this).userDao();
                    User user = dao.findByUsername(username);
                    if (user == null) {
                        user = new User(username, "");
                        long id = dao.insert(user);
                        localId[0] = (int) id;
                    } else {
                        localId[0] = user.id;
                    }

                    final String themeRaw = profile.theme != null ? profile.theme : "light";
                    new Handler(Looper.getMainLooper()).post(() -> {
                        session.login(localId[0], profile.userId, profile.username, token);
                        session.setThemeRaw(themeRaw);
                        Toast.makeText(LoginActivity.this,
                                "Добро пожаловать, " + profile.username + "!",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                }).start();
            }

            @Override
            public void onFailure(Call<AuthDtos.ProfileResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Профиль недоступен: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
