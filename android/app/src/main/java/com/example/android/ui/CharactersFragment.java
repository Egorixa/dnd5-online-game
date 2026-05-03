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
import com.example.android.data.model.Character;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.api.CharactersApi;
import com.example.android.net.dto.CharacterDtos;
import com.example.android.net.mapper.CharacterMapper;
import com.example.android.ui.adapter.CharacterAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Список персонажей-шаблонов: при наличии серверной сессии
 * грузим GET /characters и кэшируем в Room. Иначе — локальный кэш.
 */
public class CharactersFragment extends Fragment {

    private CharacterAdapter adapter;
    private TextView tvEmpty;
    private SessionManager session;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_characters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());

        RecyclerView rv = view.findViewById(R.id.rv_characters);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new CharacterAdapter(new CharacterAdapter.OnCharacterAction() {
            @Override
            public void onClick(Character c) {
                Bundle args = new Bundle();
                args.putInt(CharacterEditorFragment.ARG_CHARACTER_ID, c.id);
                Navigation.findNavController(view)
                        .navigate(R.id.action_nav_characters_to_editor, args);
            }

            @Override
            public void onDelete(Character c) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Удалить персонажа?")
                        .setMessage(c.characterName)
                        .setPositiveButton("Удалить", (d, w) -> deleteCharacter(c))
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
        if (session.hasServerSession()) {
            fetchFromServer();
        } else {
            renderLocal();
        }
    }

    private void fetchFromServer() {
        ApiClient.get(requireContext()).characters().listTemplates()
                .enqueue(new Callback<CharactersApi.CharactersListResponse>() {
                    @Override
                    public void onResponse(Call<CharactersApi.CharactersListResponse> call,
                                           Response<CharactersApi.CharactersListResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(),
                                    ApiErrors.extract(response, "Не удалось загрузить персонажей"),
                                    Toast.LENGTH_SHORT).show();
                            renderLocal();
                            return;
                        }
                        syncCacheAndRender(response.body().characters);
                    }

                    @Override
                    public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "офлайн"),
                                Toast.LENGTH_SHORT).show();
                        renderLocal();
                    }
                });
    }

    private void syncCacheAndRender(List<CharacterDtos.CharacterResponse> serverList) {
        int userId = session.getUserId();
        CharacterDao dao = AppDatabase.getInstance(requireContext()).characterDao();
        // Стереть устаревшие серверные кэш-записи; локальные (без serverId) оставляем.
        dao.deleteServerCharactersForUser(userId);
        if (serverList != null) {
            for (CharacterDtos.CharacterResponse resp : serverList) {
                Character c = CharacterMapper.fromResponse(resp, null, userId);
                c.id = 0;
                dao.insert(c);
            }
        }
        renderLocal();
    }

    private void renderLocal() {
        int userId = session.getUserId();
        CharacterDao dao = AppDatabase.getInstance(requireContext()).characterDao();
        List<Character> list = dao.getByUser(userId);
        adapter.setItems(list);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void deleteCharacter(Character c) {
        if (session.hasServerSession() && !android.text.TextUtils.isEmpty(c.serverCharacterId)) {
            ApiClient.get(requireContext()).characters()
                    .deleteTemplate(c.serverCharacterId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) return;
                            AppDatabase.getInstance(requireContext()).characterDao().delete(c);
                            reload();
                            Toast.makeText(getContext(), "Удалено", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(),
                                    "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            AppDatabase.getInstance(requireContext()).characterDao().delete(c);
            reload();
            Toast.makeText(getContext(), "Удалено", Toast.LENGTH_SHORT).show();
        }
    }
}
