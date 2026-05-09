package com.example.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.data.model.Room;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.api.CharactersApi;
import com.example.android.net.api.RoomsApi;
import com.example.android.net.dto.CharacterDtos;
import com.example.android.net.dto.RoomDtos;
import com.example.android.ui.adapter.RoomAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GamesFragment extends Fragment {

    private RoomAdapter adapter;
    private final Map<String, String> codeToRoomId = new HashMap<>();
    private final Map<String, String> codeToMasterId = new HashMap<>();

    private static final long ROOMS_REFRESH_INTERVAL_MS = 3000L;
    private android.os.Handler refreshHandler;
    private final Runnable refreshTask = new Runnable() {
        @Override public void run() {
            if (!isAdded()) return;
            loadPublicRooms();
            if (refreshHandler != null) {
                refreshHandler.postDelayed(this, ROOMS_REFRESH_INTERVAL_MS);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvWelcome = view.findViewById(R.id.tv_welcome);
        String username = new SessionManager(requireContext()).getUsername();
        tvWelcome.setText("Привет, " + (TextUtils.isEmpty(username) ? "игрок" : username) + "!");

        TextInputEditText etCode = view.findViewById(R.id.et_room_code);
        Button btnJoin = view.findViewById(R.id.btn_join);

        btnJoin.setOnClickListener(v -> {
            String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
            if (code.isEmpty()) {
                Toast.makeText(getContext(), "Введите код комнаты", Toast.LENGTH_SHORT).show();
                return;
            }
            joinRoomByCode(code);
        });

        RecyclerView rv = view.findViewById(R.id.rv_rooms);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoomAdapter(room -> joinRoomByCode(room.code));
        rv.setAdapter(adapter);

        loadPublicRooms();
        updateActiveRoomBanner();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isApiAvailable()) loadPublicRooms();
        updateActiveRoomBanner();
        if (refreshHandler == null) {
            refreshHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        refreshHandler.removeCallbacks(refreshTask);
        refreshHandler.postDelayed(refreshTask, ROOMS_REFRESH_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshTask);
        }
    }

    private void updateActiveRoomBanner() {
        View root = getView();
        if (root == null) return;
        View card = root.findViewById(R.id.card_active_room);
        TextView tvCode = root.findViewById(R.id.tv_active_room_code);
        if (card == null || tvCode == null) return;
        SessionManager sm = new SessionManager(requireContext());
        String roomId = sm.getActiveRoomId();
        String roomCode = sm.getActiveRoomCode();
        if (TextUtils.isEmpty(roomId)) {
            card.setVisibility(View.GONE);
            return;
        }
        card.setVisibility(View.VISIBLE);
        tvCode.setText("Код: " + (TextUtils.isEmpty(roomCode) ? "—" : roomCode));
        final String fRoomId = roomId;
        final String fRoomCode = roomCode;
        card.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), GameRoomActivity.class);
            i.putExtra(GameRoomActivity.EXTRA_ROOM_ID, fRoomId);
            i.putExtra(GameRoomActivity.EXTRA_ROOM_CODE, fRoomCode);
            startActivity(i);
        });
    }

    private boolean isApiAvailable() {
        return new SessionManager(requireContext()).hasServerSession();
    }

    private void loadPublicRooms() {
        if (!isApiAvailable()) {
            adapter.setItems(new ArrayList<>());
            return;
        }
        ApiClient.get(requireContext()).rooms().getPublic(50, 0).enqueue(new Callback<RoomsApi.PublicRoomsResponse>() {
            @Override
            public void onResponse(Call<RoomsApi.PublicRoomsResponse> call, Response<RoomsApi.PublicRoomsResponse> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null || response.body().rooms == null) {
                    Toast.makeText(getContext(),
                            ApiErrors.extract(response, "Не удалось загрузить список комнат"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                codeToRoomId.clear();
                codeToMasterId.clear();
                String myUserId = new SessionManager(requireContext()).getServerUserId();
                List<Room> ui = new ArrayList<>();
                for (RoomDtos.PublicRoomDto r : response.body().rooms) {
                    if (myUserId != null && !myUserId.isEmpty()
                            && r.masterId != null && myUserId.equals(r.masterId)) {

                        continue;
                    }
                    codeToRoomId.put(r.roomCode, r.roomId);
                    if (r.masterId != null) codeToMasterId.put(r.roomCode, r.masterId);
                    ui.add(new Room(r.roomCode,
                            (r.masterUsername == null ? "" : r.masterUsername) + " · " + (r.name == null ? "" : r.name),
                            r.playersCount, 8, true));
                }
                adapter.setItems(ui);
            }

            @Override
            public void onFailure(Call<RoomsApi.PublicRoomsResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Сервер недоступен: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinRoomByCode(String code) {
        if (!isApiAvailable()) {
            Toast.makeText(getContext(), "Нет авторизации на сервере", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sm = new SessionManager(requireContext());
        String activeRoomId = sm.getActiveRoomId();
        String activeRoomCode = sm.getActiveRoomCode();
        String targetRoomId = codeToRoomId.get(code);
        boolean sameRoom = (!TextUtils.isEmpty(activeRoomCode) && activeRoomCode.equalsIgnoreCase(code))
                || (!TextUtils.isEmpty(targetRoomId) && !TextUtils.isEmpty(activeRoomId)
                        && targetRoomId.equals(activeRoomId));
        if (!TextUtils.isEmpty(activeRoomId) && !sameRoom) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Вы уже в комнате")
                    .setMessage("Сначала выйдите из текущей комнаты (код "
                            + (TextUtils.isEmpty(activeRoomCode) ? "—" : activeRoomCode)
                            + "), чтобы присоединиться к другой.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String myUserId = sm.getServerUserId();
        String masterIdHint = codeToMasterId.get(code);
        if (myUserId != null && !myUserId.isEmpty()
                && masterIdHint != null && myUserId.equals(masterIdHint)) {
            Toast.makeText(getContext(),
                    "Вы мастер этой комнаты — войдите в неё через раздел «Мои комнаты» на веб-клиенте, либо создайте отдельный аккаунт игрока.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        checkHasTemplatesThen(() -> doJoinRoomByCode(code));
    }

    private void checkHasTemplatesThen(Runnable onHas) {
        ApiClient.get(requireContext()).characters().listTemplates()
                .enqueue(new Callback<CharactersApi.CharactersListResponse>() {
                    @Override
                    public void onResponse(Call<CharactersApi.CharactersListResponse> call,
                                           Response<CharactersApi.CharactersListResponse> response) {
                        if (!isAdded()) return;
                        boolean has = response.isSuccessful()
                                && response.body() != null
                                && response.body().characters != null
                                && !response.body().characters.isEmpty();
                        if (!has) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Нет персонажей")
                                    .setMessage("Перед входом в комнату создайте хотя бы одного персонажа в разделе «Персонажи».")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        onHas.run();
                    }

                    @Override
                    public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void doJoinRoomByCode(String code) {
        ApiClient.get(requireContext()).rooms().join(code).enqueue(new Callback<RoomDtos.JoinRoomResponse>() {
            @Override
            public void onResponse(Call<RoomDtos.JoinRoomResponse> call, Response<RoomDtos.JoinRoomResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    RoomDtos.JoinRoomResponse j = response.body();
                    if (RoomDtos.ParticipantRole.MASTER.equalsIgnoreCase(j.role)) {

                        Toast.makeText(getContext(),
                                "Вы мастер этой комнаты. Зайти игроком в собственную комнату нельзя.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    promptCharacterAndOpen(j.roomId, j.roomCode);
                    return;
                }
                if (response.code() == 409) {
                    String roomId = codeToRoomId.get(code);
                    SessionManager sm = new SessionManager(requireContext());
                    if (roomId == null) roomId = sm.getActiveRoomId();
                    if (roomId != null) {
                        promptCharacterAndOpen(roomId, code);
                        return;
                    }
                }
                Toast.makeText(getContext(),
                        ApiErrors.extract(response, "Не удалось присоединиться"),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<RoomDtos.JoinRoomResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Сервер недоступен: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void promptCharacterAndOpen(String roomId, String roomCode) {
        if (!isAdded()) return;
        showTemplatePicker(roomId, roomCode);
    }

    private void showTemplatePicker(String roomId, String roomCode) {
        ApiClient.get(requireContext()).characters().listTemplates()
                .enqueue(new Callback<CharactersApi.CharactersListResponse>() {
                    @Override
                    public void onResponse(Call<CharactersApi.CharactersListResponse> call,
                                           Response<CharactersApi.CharactersListResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().characters == null
                                || response.body().characters.isEmpty()) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Нет шаблонов персонажей")
                                    .setMessage("Создайте лист персонажа в разделе «Персонажи» и попробуйте ещё раз. Войти в комнату без листа?")
                                    .setPositiveButton("Войти", (d, w) -> openRoom(roomId, roomCode))
                                    .setNegativeButton("Отмена", null)
                                    .show();
                            return;
                        }
                        final List<CharacterDtos.CharacterResponse> list =
                                new ArrayList<>(response.body().characters);
                        CharSequence[] titles = new CharSequence[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            CharacterDtos.CharacterResponse c = list.get(i);
                            String name = c.name == null || c.name.trim().isEmpty() ? "(без имени)" : c.name;
                            String clsRu = com.example.android.net.mapper.CharacterMapper.classFromKey(c.characterClass);
                            String raceRu = com.example.android.net.mapper.CharacterMapper.raceFromKey(c.race);
                            StringBuilder sb = new StringBuilder(name);
                            if (raceRu != null && !raceRu.isEmpty()) sb.append(" · ").append(raceRu);
                            if (clsRu != null && !clsRu.isEmpty()) sb.append(" · ").append(clsRu);
                            sb.append(" · ур. ").append(c.level);
                            titles[i] = sb.toString();
                        }
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Выберите персонажа для входа")
                                .setItems(titles, (d, which) -> applyTemplateAndOpen(roomId, roomCode, list.get(which)))
                                .setNegativeButton("Отмена", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_SHORT).show();
                        openRoom(roomId, roomCode);
                    }
                });
    }

    private void applyTemplateAndOpen(String roomId, String roomCode, CharacterDtos.CharacterResponse template) {
        CharacterDtos.CharacterUpsertRequest req = templateToUpsert(template);
        ApiClient.get(requireContext()).characters().createInRoom(roomId, req)
                .enqueue(new Callback<CharacterDtos.CharacterResponse>() {
                    @Override
                    public void onResponse(Call<CharacterDtos.CharacterResponse> call,
                                           Response<CharacterDtos.CharacterResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Intent i = new Intent(requireContext(), GameRoomActivity.class);
                            i.putExtra(GameRoomActivity.EXTRA_ROOM_CODE, roomCode);
                            i.putExtra(GameRoomActivity.EXTRA_ROOM_ID, roomId);
                            i.putExtra(GameRoomActivity.EXTRA_CHARACTER_ID, response.body().characterId);
                            new SessionManager(requireContext()).setActiveRoom(roomId, roomCode);
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(),
                                    ApiErrors.extract(response, "Не удалось добавить персонажа"),
                                    Toast.LENGTH_LONG).show();
                            openRoom(roomId, roomCode);
                        }
                    }

                    @Override
                    public void onFailure(Call<CharacterDtos.CharacterResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_SHORT).show();
                        openRoom(roomId, roomCode);
                    }
                });
    }

    private static CharacterDtos.CharacterUpsertRequest templateToUpsert(CharacterDtos.CharacterResponse t) {
        CharacterDtos.CharacterUpsertRequest r = new CharacterDtos.CharacterUpsertRequest();
        r.name = t.name; r.playerName = t.playerName; r.race = t.race;
        r.characterClass = t.characterClass; r.level = t.level;
        r.background = t.background; r.alignment = t.alignment;
        r.experiencePoints = t.experiencePoints;
        r.strength = t.strength; r.dexterity = t.dexterity; r.constitution = t.constitution;
        r.intelligence = t.intelligence; r.wisdom = t.wisdom; r.charisma = t.charisma;
        r.armorClass = t.armorClass; r.initiativeBonus = t.initiativeBonus; r.speed = t.speed;
        r.maxHp = t.maxHp; r.currentHp = t.currentHp; r.tempHp = t.tempHp;
        r.hitDieType = t.hitDieType; r.hitDiceRemaining = t.hitDiceRemaining;
        r.deathSaveSuccesses = t.deathSaveSuccesses; r.deathSaveFailures = t.deathSaveFailures;
        r.inspiration = t.inspiration;
        r.copperPieces = t.copperPieces; r.silverPieces = t.silverPieces;
        r.electrumPieces = t.electrumPieces; r.goldPieces = t.goldPieces;
        r.platinumPieces = t.platinumPieces;
        r.equipment = t.equipment; r.otherProficiencies = t.otherProficiencies;
        r.characterTraits = t.characterTraits; r.ideals = t.ideals;
        r.bonds = t.bonds; r.flaws = t.flaws; r.featuresAndTraits = t.featuresAndTraits;
        r.eyes = t.eyes; r.age = t.age; r.height = t.height; r.weight = t.weight;
        r.skin = t.skin; r.hair = t.hair;
        r.alliesAndOrganizations = t.alliesAndOrganizations;
        r.backstory = t.backstory; r.treasure = t.treasure;
        r.additionalNotes = t.additionalNotes; r.distinguishingMarks = t.distinguishingMarks;
        if (t.skills != null) {
            java.util.LinkedHashMap<String, String> m = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<String, CharacterDtos.SkillView> e : t.skills.entrySet()) {
                if (e.getValue() != null) m.put(e.getKey(), e.getValue().level);
            }
            r.skillProficiencies = m;
        }
        if (t.saves != null) {
            java.util.LinkedHashMap<String, String> m = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<String, CharacterDtos.SaveView> e : t.saves.entrySet()) {
                if (e.getValue() != null) m.put(e.getKey(), e.getValue().level);
            }
            r.saveProficiencies = m;
        }
        r.attacks = t.attacks;
        r.spellcastingClass = t.spellcastingClass;
        r.spellSlotsTotal = t.spellSlotsTotal;
        r.spellSlotsUsed = t.spellSlotsUsed;
        r.preparedLimit = t.preparedLimit;
        r.spells = t.spells;
        return r;
    }

    private void openRoom(String roomId, String roomCode) {
        new SessionManager(requireContext()).setActiveRoom(roomId, roomCode);
        Intent i = new Intent(requireContext(), GameRoomActivity.class);
        i.putExtra(GameRoomActivity.EXTRA_ROOM_CODE, roomCode);
        i.putExtra(GameRoomActivity.EXTRA_ROOM_ID, roomId);
        startActivity(i);
    }
}
