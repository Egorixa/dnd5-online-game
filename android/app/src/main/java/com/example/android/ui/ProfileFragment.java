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
import com.example.android.data.AppDatabase;
import com.example.android.data.SessionManager;
import com.example.android.data.model.User;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        TextView tvUsername = view.findViewById(R.id.tv_username);
        TextView tvStats = view.findViewById(R.id.tv_stats);
        TextView tvRegDate = view.findViewById(R.id.tv_reg_date);

        User user = AppDatabase.getInstance(requireContext()).userDao().findById(session.getUserId());
        if (user != null) {
            tvUsername.setText(user.username);
            tvStats.setText(String.format(Locale.getDefault(),
                    "Игр сыграно: %d  ·  Побед: %d  ·  Поражений: %d  ·  Был мастером: %d раз",
                    user.gamesPlayed, user.gamesWon, user.gamesLost, user.timesMaster));
            SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            tvRegDate.setText("Дата регистрации: " + fmt.format(new Date(user.registrationDate)));
        } else {
            tvUsername.setText("Гость");
            tvStats.setText("Нет данных");
            tvRegDate.setText("");
        }

        MaterialSwitch switchTheme = view.findViewById(R.id.switch_theme);
        switchTheme.setChecked(session.isDarkTheme());
        switchTheme.setOnCheckedChangeListener((b, checked) -> {
            session.setDarkTheme(checked);
            requireActivity().recreate();
        });

        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
        });
    }
}
