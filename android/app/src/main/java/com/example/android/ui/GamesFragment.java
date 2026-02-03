package com.example.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.android.R;
import com.google.android.material.textfield.TextInputEditText;

public class GamesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Привязываем XML-макет
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        // 2. Находим элементы управления
        TextInputEditText etCode = view.findViewById(R.id.et_room_code);
        Button btnJoin = view.findViewById(R.id.btn_join);

        // 3. Настраиваем логику нажатия кнопки
        btnJoin.setOnClickListener(v -> {
            String code = etCode.getText() != null ? etCode.getText().toString() : "";
            if (!code.isEmpty()) {
                // TODO: добавить логику подключения к серверу
                Toast.makeText(getContext(), "Подключаемся к комнате: " + code, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Пожалуйста, введите код", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}