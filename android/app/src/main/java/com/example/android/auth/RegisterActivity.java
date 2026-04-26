package com.example.android.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android.R;
import com.example.android.data.AppDatabase;
import com.example.android.data.SessionManager;
import com.example.android.data.dao.UserDao;
import com.example.android.data.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword, tilPasswordConfirm;
    private TextInputEditText etUsername, etPassword, etPasswordConfirm;

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

        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvToLogin = findViewById(R.id.tv_to_login);

        btnRegister.setOnClickListener(v -> tryRegister());
        tvToLogin.setOnClickListener(v -> finish());
    }

    private void tryRegister() {
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilPasswordConfirm.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
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

        UserDao dao = AppDatabase.getInstance(this).userDao();
        if (dao.countByUsername(username) > 0) {
            tilUsername.setError("Имя уже занято");
            Toast.makeText(this, "Регистрация не удалась", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, password);
        long id = dao.insert(user);
        if (id <= 0) {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Аккаунт создан. Войдите.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
