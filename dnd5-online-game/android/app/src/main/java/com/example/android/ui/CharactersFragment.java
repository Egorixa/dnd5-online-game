package com.example.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.R;
import com.example.android.data.AppDatabase;
import com.example.android.data.SessionManager;
import com.example.android.data.dao.CharacterDao;
import com.example.android.ui.adapter.CharacterAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CharactersFragment extends Fragment {

    private CharacterAdapter adapter;
    private TextView tvEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_characters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_characters);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new CharacterAdapter(new CharacterAdapter.OnCharacterAction() {
            @Override
            public void onClick(com.example.android.data.model.Character c) {
                Bundle args = new Bundle();
                args.putInt(CharacterEditorFragment.ARG_CHARACTER_ID, c.id);
                Navigation.findNavController(view)
                        .navigate(R.id.action_nav_characters_to_editor, args);
            }

            @Override
            public void onDelete(com.example.android.data.model.Character c) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Удалить персонажа?")
                        .setMessage(c.characterName)
                        .setPositiveButton("Удалить", (d, w) -> {
                            AppDatabase.getInstance(requireContext()).characterDao().delete(c);
                            reload();
                            Toast.makeText(getContext(), "Удалено", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_character);
        fab.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_nav_characters_to_editor));
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        int userId = new SessionManager(requireContext()).getUserId();
        CharacterDao dao = AppDatabase.getInstance(requireContext()).characterDao();
        java.util.List<com.example.android.data.model.Character> list = dao.getByUser(userId);
        adapter.setItems(list);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
