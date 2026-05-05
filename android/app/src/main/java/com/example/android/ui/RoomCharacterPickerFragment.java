package com.example.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.R;

/**
 * Заглушка для вкладки «Лист», когда в комнате ещё не выбран персонаж.
 * Вызывает callback Activity для показа диалога выбора шаблона.
 */
public class RoomCharacterPickerFragment extends Fragment {

    public interface PickerHost {
        void onPickCharacterClicked();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_character_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btn = view.findViewById(R.id.btn_pick_character);
        btn.setOnClickListener(v -> {
            if (getActivity() instanceof PickerHost) {
                ((PickerHost) getActivity()).onPickCharacterClicked();
            }
        });
    }
}
