package com.example.android.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.dto.AuthDtos;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword, tilPasswordConfirm;
    private TextInputEditText etUsername, etPassword, etPasswordConfirm;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SessionManager(this).applySavedTheme();
        setContentView(R.layout.activity_register);

        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilPasswordConfirm = findViewById(R.id.til_password_confirm);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);

        btnRegister = findViewById(R.id.btn_register);
        TextView tvToLogin = findViewById(R.id.tv_to_login);

        btnRegister.setOnClickListener(v -> tryRegister());
        tvToLogin.setOnClickListener(v -> finish());
    }

    private void tryRegister() {
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilPasswordConfirm.setError(null);

        final String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        final String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etPasswordConfirm.getText() != null ? etPasswordConfirm.getText().toString() : "";

        if (username.length() < 3 || username.length() > 20) {
            tilUsername.setError("Имя: 3–20 символов");
            return;
        }
        if (!username.matches("[A-Za-zА-Яа-я0-9]+")) {
            tilUsername.setError("Только буквы и цифры");
            return;
        }
        if (password.length() < 6 || password.length() > 30) {
            tilPassword.setError("Пароль: 6–30 символов");
            return;
        }
        if (!TextUtils.equals(password, confirm)) {
            tilPasswordConfirm.setError("Пароли не совпадают");
            return;
        }

        btnRegister.setEnabled(false);
        AuthDtos.RegisterRequest req = new AuthDtos.RegisterRequest(username, password);
        ApiClient.get(this).auth().register(req).enqueue(new Callback<AuthDtos.RegisterResponse>() {
            @Override
            public void onResponse(Call<AuthDtos.RegisterResponse> call, Response<AuthDtos.RegisterResponse> response) {
                btnRegister.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    String msg = ApiErrors.extract(response, "Регистрация не удалась");
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(RegisterActivity.this,
                        "Аккаунт создан. Войдите.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<AuthDtos.RegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this,
                        "Не удалось подключиться: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
