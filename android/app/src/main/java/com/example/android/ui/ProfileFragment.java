package com.example.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.android.R;
import com.example.android.auth.LoginActivity;
import com.example.android.data.SessionManager;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.dto.AuthDtos;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUsername;
    private TextView tvStats;
    private TextView tvRegDate;
    private MaterialSwitch switchTheme;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        tvUsername = view.findViewById(R.id.tv_username);
        tvStats = view.findViewById(R.id.tv_stats);
        tvRegDate = view.findViewById(R.id.tv_reg_date);

        // Стартовое отображение из сессии до прихода ответа.
        tvUsername.setText(session.getUsername().isEmpty() ? "Гость" : session.getUsername());
        tvStats.setText("Загрузка статистики…");
        tvRegDate.setText("");

        switchTheme = view.findViewById(R.id.switch_theme);
        switchTheme.setChecked(session.isDarkTheme());
        switchTheme.setOnCheckedChangeListener((b, checked) -> updateTheme(checked));

        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> doLogout());

        if (session.hasServerSession()) {
            loadProfile();
        } else {
            tvStats.setText("Нет соединения с сервером");
        }
    }

    private void loadProfile() {
        ApiClient.get(requireContext()).auth().profile().enqueue(new Callback<AuthDtos.ProfileResponse>() {
            @Override
            public void onResponse(Call<AuthDtos.ProfileResponse> call, Response<AuthDtos.ProfileResponse> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    tvStats.setText("Не удалось загрузить статистику");
                    return;
                }
                AuthDtos.ProfileResponse p = response.body();
                tvUsername.setText(p.username);
                int wins = p.stats != null ? p.stats.wins : 0;
                int defeats = p.stats != null ? p.stats.defeats : 0;
                int master = p.stats != null ? p.stats.masterCount : 0;
                int total = wins + defeats;
                tvStats.setText(String.format(Locale.getDefault(),
                        "Игр сыграно: %d  ·  Побед: %d  ·  Поражений: %d  ·  Был мастером: %d раз",
                        total, wins, defeats, master));
                tvRegDate.setText("Дата регистрации: " + formatDate(p.registrationDate));
            }

            @Override
            public void onFailure(Call<AuthDtos.ProfileResponse> call, Throwable t) {
                if (!isAdded()) return;
                tvStats.setText("Не удалось подключиться: " + ApiErrors.fromThrowable(t, "сеть"));
            }
        });
    }

    private void updateTheme(boolean dark) {
        SessionManager session = new SessionManager(requireContext());
        session.setDarkTheme(dark); // локально применить сразу
        if (session.hasServerSession()) {
            String value = dark ? "dark" : "light";
            ApiClient.get(requireContext()).auth().updateTheme(new AuthDtos.UpdateThemeRequest(value))
                    .enqueue(new Callback<AuthDtos.ProfileResponse>() {
                        @Override
                        public void onResponse(Call<AuthDtos.ProfileResponse> call, Response<AuthDtos.ProfileResponse> response) {
                            if (isAdded()) requireActivity().recreate();
                        }

                        @Override
                        public void onFailure(Call<AuthDtos.ProfileResponse> call, Throwable t) {
                            if (isAdded()) requireActivity().recreate();
                        }
                    });
        } else {
            requireActivity().recreate();
        }
    }

    private void doLogout() {
        SessionManager session = new SessionManager(requireContext());
        // Best-effort серверный logout — не блокируем UI на ответе.
        if (session.hasServerSession()) {
            ApiClient.get(requireContext()).auth().logout().enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
        session.logout();
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        requireActivity().finish();
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        // Берём только первые 10 символов "yyyy-MM-dd" если ISO-8601, иначе как есть.
        try {
            // Возможный формат "2024-01-31T12:34:56Z" — обрежем до дня.
            String day = iso.length() >= 10 ? iso.substring(0, 10) : iso;
            String[] p = day.split("-");
            if (p.length == 3) {
                return p[2] + "." + p[1] + "." + p[0];
            }
            return day;
        } catch (Exception e) {
            return iso;
        }
    }
}
