package com.example.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.android.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CharactersFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characters, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_character);

        fab.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_nav_characters_to_editor);
        });

        return view;
    }
}
