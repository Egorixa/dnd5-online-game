package com.example.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.android.R;

public class CharacterEditorFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character_editor, container, false);

        // --- Настройка выпадающего списка РАС
        AutoCompleteTextView raceSpinner = view.findViewById(R.id.spinner_race);
        String[] races = {"Дварф", "Эльф", "Полурослик", "Человек", "Драконорождённый", "Гном", "Полуэльф", "Полуорк", "Тифлинг"};
        ArrayAdapter<String> raceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, races);
        raceSpinner.setAdapter(raceAdapter);

        // --- Настройка выпадающего списка КЛАССОВ
        AutoCompleteTextView classSpinner = view.findViewById(R.id.spinner_class);
        String[] classes = {"Варвар", "Бард", "Жрец", "Друид", "Воин", "Монах", "Паладин", "Следопыт", "Плут", "Чародей", "Колдун", "Волшебник"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, classes);
        classSpinner.setAdapter(classAdapter);

        // TODO: добавить логику сохранения
        Button btnSave = view.findViewById(R.id.btn_save_char);
        btnSave.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Персонаж сохранен локально!", Toast.LENGTH_SHORT).show();

            // Возвращаемся назад к списку персонажей
            Navigation.findNavController(view).popBackStack();
        });

        return view;
    }
}