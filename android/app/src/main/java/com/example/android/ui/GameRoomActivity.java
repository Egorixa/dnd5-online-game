package com.example.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.api.CharactersApi;
import com.example.android.net.dto.CharacterDtos;
import com.example.android.net.realtime.RoomHubClient;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Экран игровой комнаты (п. 4.1.1.6 ТЗ).
 *
 * По ТЗ (3.1, 4.3.4) мобильное приложение предназначено только для игрока.
 * Мастерский функционал (создание/завершение сессии, кик и т. п.) реализуется
 * через веб-интерфейс мастера и здесь отсутствует.
 *
 * Активность содержит 3 вкладки:
 *  - «Лист»: либо пикер персонажа, либо CharacterEditorFragment в room-режиме.
 *  - «Кубики»: DiceFragment (берёт roomId из SessionManager).
 *  - «Комната»: RoomInfoFragment (статус, участники, лог событий, leave).
 *
 * SignalR-подключение и обработка событий — на уровне Activity, события
 * пересылаются в RoomInfoFragment (если активен). При получении
 * room.updated со статусом FINISHED показывается уведомление о завершении
 * сессии мастером (4.1.1.6).
 */
public class GameRoomActivity extends AppCompatActivity
        implements RoomHubClient.Listener,
        RoomInfoFragment.RoomInfoHost,
        RoomCharacterPickerFragment.PickerHost {

    public static final String EXTRA_ROOM_ID = "room_id";
    public static final String EXTRA_ROOM_CODE = "room_code";
    public static final String EXTRA_CHARACTER_ID = "character_id";

    private static final String TAG_SHEET = "tab_sheet";
    private static final String TAG_DICE = "tab_dice";
    private static final String TAG_ROOM = "tab_room";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private RoomHubClient hubClient;
    private SessionManager session;

    private String roomId;
    private String roomCode;
    @Nullable
    private String activeCharacterId;

    /** Текущий статус подключения (для передачи во вновь созданный RoomInfoFragment). */
    private boolean connected;
    @Nullable private String connectionMessage;

    /** Кэш событий: при пересоздании RoomInfoFragment отдаём ему историю. */
    private final List<String> eventLog = new ArrayList<>();

    /** Защита от повторного показа диалога завершения. */
    private boolean sessionFinishedShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        session = new SessionManager(getApplicationContext());
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        roomCode = getIntent().getStringExtra(EXTRA_ROOM_CODE);

        if (TextUtils.isEmpty(roomId)) roomId = session.getActiveRoomId();
        if (TextUtils.isEmpty(roomCode)) roomCode = session.getActiveRoomCode();

        // Гарантируем, что DiceFragment найдёт активную комнату через SessionManager.
        if (!TextUtils.isEmpty(roomId)) {
            session.setActiveRoom(roomId, roomCode == null ? "" : roomCode);
        }

        TabLayout tabs = findViewById(R.id.tabs_room);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Восстановление: если фрагменты ещё не созданы — открываем «Лист» по умолчанию.
        if (savedInstanceState == null) {
            showTab(0);
        }

        startHub();
        loadActiveCharacter();
    }

    // ──────────── Tab management ────────────

    private void showTab(int position) {
        Fragment f;
        String tag;
        switch (position) {
            case 1:
                f = ensureFragment(TAG_DICE, DiceFragment::new);
                tag = TAG_DICE;
                break;
            case 2: {
                Fragment existingRi = getSupportFragmentManager().findFragmentByTag(TAG_ROOM);
                final boolean firstTime = existingRi == null;
                f = existingRi != null ? existingRi : new RoomInfoFragment();
                tag = TAG_ROOM;
                final RoomInfoFragment ri = (RoomInfoFragment) f;
                mainHandler.post(() -> {
                    ri.setConnectionStatus(connected, connectionMessage);
                    if (firstTime) {
                        // Отдаём накопленные события от старых к новым (appendEvent использует insert(0)).
                        for (String line : eventLog) ri.appendEvent(line);
                    }
                    ri.loadParticipants();
                });
                break;
            }
            case 0:
            default:
                if (TextUtils.isEmpty(activeCharacterId)) {
                    f = ensureFragment(TAG_SHEET, RoomCharacterPickerFragment::new);
                } else {
                    // Создаём CharacterEditorFragment в room-mode с актуальным id
                    Fragment existing = getSupportFragmentManager().findFragmentByTag(TAG_SHEET);
                    if (existing instanceof CharacterEditorFragment) {
                        f = existing;
                    } else {
                        CharacterEditorFragment ed = new CharacterEditorFragment();
                        Bundle args = new Bundle();
                        args.putString(CharacterEditorFragment.ARG_ROOM_ID, roomId);
                        args.putString(CharacterEditorFragment.ARG_SERVER_CHARACTER_ID, activeCharacterId);
                        ed.setArguments(args);
                        f = ed;
                    }
                }
                tag = TAG_SHEET;
                break;
        }

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        for (String t : new String[]{TAG_SHEET, TAG_DICE, TAG_ROOM}) {
            Fragment other = getSupportFragmentManager().findFragmentByTag(t);
            if (other != null && other != f) tx.hide(other);
        }
        if (!f.isAdded()) {
            tx.add(R.id.room_fragment_container, f, tag);
        } else {
            tx.show(f);
        }
        tx.commitAllowingStateLoss();
    }

    private Fragment ensureFragment(String tag, FragmentFactory factory) {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        return existing != null ? existing : factory.create();
    }

    private interface FragmentFactory {
        Fragment create();
    }

    /** Принудительно пересоздать вкладку «Лист» (после смены персонажа). */
    private void resetSheetTab() {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(TAG_SHEET);
        if (existing != null) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.remove(existing);
            tx.commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
        }
        TabLayout tabs = findViewById(R.id.tabs_room);
        if (tabs != null && tabs.getSelectedTabPosition() == 0) {
            showTab(0);
        }
    }

    // ──────────── Загрузка / выбор персонажа ────────────

    private void loadActiveCharacter() {
        if (TextUtils.isEmpty(roomId)) return;
        ApiClient.get(this).characters().listInRoom(roomId)
                .enqueue(new Callback<CharactersApi.CharactersListResponse>() {
                    @Override
                    public void onResponse(Call<CharactersApi.CharactersListResponse> call,
                                           Response<CharactersApi.CharactersListResponse> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().characters == null
                                || response.body().characters.isEmpty()) {
                            return;
                        }
                        CharacterDtos.CharacterResponse c = response.body().characters.get(0);
                        activeCharacterId = c.characterId;
                        // Если пользователь сейчас на вкладке «Лист» — обновим её.
                        TabLayout tabs = findViewById(R.id.tabs_room);
                        if (tabs != null && tabs.getSelectedTabPosition() == 0) {
                            resetSheetTab();
                        }
                    }

                    @Override
                    public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) { /* ignore */ }
                });
    }

    @Override
    public void onPickCharacterClicked() {
        showSelectCharacterDialog();
    }

    private void showSelectCharacterDialog() {
        ApiClient.get(this).characters().listTemplates()
                .enqueue(new Callback<CharactersApi.CharactersListResponse>() {
                    @Override
                    public void onResponse(Call<CharactersApi.CharactersListResponse> call,
                                           Response<CharactersApi.CharactersListResponse> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().characters == null
                                || response.body().characters.isEmpty()) {
                            Toast.makeText(GameRoomActivity.this,
                                    "Нет шаблонов персонажей. Создайте лист в разделе «Персонажи».",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        final List<CharacterDtos.CharacterResponse> list =
                                new ArrayList<>(response.body().characters);
                        CharSequence[] titles = new CharSequence[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            CharacterDtos.CharacterResponse c = list.get(i);
                            String name = c.name == null || c.name.trim().isEmpty() ? "(без имени)" : c.name;
                            String cls = c.characterClass == null ? "" : (" · " + c.characterClass);
                            titles[i] = name + cls + " · ур. " + c.level;
                        }
                        new AlertDialog.Builder(GameRoomActivity.this)
                                .setTitle("Выберите персонажа")
                                .setItems(titles, (d, which) -> applyTemplateToRoom(list.get(which)))
                                .setNegativeButton("Отмена", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) {
                        Toast.makeText(GameRoomActivity.this,
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyTemplateToRoom(CharacterDtos.CharacterResponse template) {
        if (TextUtils.isEmpty(roomId)) return;
        CharacterDtos.CharacterUpsertRequest req = templateToUpsert(template);
        ApiClient.get(this).characters().createInRoom(roomId, req)
                .enqueue(new Callback<CharacterDtos.CharacterResponse>() {
                    @Override
                    public void onResponse(Call<CharacterDtos.CharacterResponse> call,
                                           Response<CharacterDtos.CharacterResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            activeCharacterId = response.body().characterId;
                            Toast.makeText(GameRoomActivity.this,
                                    "Персонаж добавлен в комнату",
                                    Toast.LENGTH_SHORT).show();
                            resetSheetTab();
                        } else {
                            Toast.makeText(GameRoomActivity.this,
                                    ApiErrors.extract(response, "Не удалось добавить персонажа"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CharacterDtos.CharacterResponse> call, Throwable t) {
                        Toast.makeText(GameRoomActivity.this,
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Скопировать поля шаблона в запрос на создание персонажа в комнате. */
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

    // ──────────── SignalR ────────────

    private void startHub() {
        if (TextUtils.isEmpty(roomId)) return;
        String token = session.getToken();
        if (TextUtils.isEmpty(token)) return;
        hubClient = new RoomHubClient(token, this);
        hubClient.start(roomId);
    }

    // ──────────── RoomInfoHost ────────────

    @Override public String getRoomId() { return roomId; }
    @Override public String getRoomCode() { return roomCode; }

    @Override
    public void onRoomInfoLeaveClicked() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из комнаты")
                .setMessage("Точно выйти? Изменения будут сохранены.")
                .setPositiveButton("Выйти", (d, w) -> doLeave())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void doLeave() {
        if (TextUtils.isEmpty(roomId)) {
            session.clearActiveRoom();
            finish();
            return;
        }
        ApiClient.get(this).rooms().leave(roomId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                session.clearActiveRoom();
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GameRoomActivity.this,
                        "Ошибка выхода: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_SHORT).show();
                session.clearActiveRoom();
                finish();
            }
        });
    }

    /**
     * Уведомление о завершении сессии мастером (ТЗ 4.1.1.6).
     * Триггерится по SignalR room.updated со status=FINISHED.
     * Если пользователь присутствует в payload.winners → победа,
     * в payload.losers → поражение, иначе — нейтральное сообщение.
     */
    private void showSessionFinishedByMaster(JsonObject payload) {
        if (sessionFinishedShown) return;
        sessionFinishedShown = true;

        String userId = session.getServerUserId();
        boolean victory = userIdInArray(payload, "winners", userId);
        boolean defeat = userIdInArray(payload, "losers", userId);

        String msg;
        if (victory) {
            msg = "Победа! Лист персонажа сохранён.";
        } else if (defeat) {
            msg = "Поражение.";
        } else {
            msg = "Сессия завершена мастером.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Сессия завершена")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> {
                    session.clearActiveRoom();
                    finish();
                })
                .show();
    }

    private static boolean userIdInArray(JsonObject payload, String key, @Nullable String userId) {
        if (TextUtils.isEmpty(userId)) return false;
        if (!payload.has(key) || payload.get(key).isJsonNull()) return false;
        JsonElement el = payload.get(key);
        if (!el.isJsonArray()) return false;
        JsonArray arr = el.getAsJsonArray();
        for (JsonElement e : arr) {
            if (e == null || e.isJsonNull()) continue;
            String v = e.isJsonPrimitive() ? e.getAsString() : e.toString();
            if (userId.equals(v)) return true;
        }
        return false;
    }

    // ──────────── Дополнительный приём событий → RoomInfoFragment ────────────

    @Nullable
    private RoomInfoFragment findRoomInfo() {
        Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_ROOM);
        return f instanceof RoomInfoFragment ? (RoomInfoFragment) f : null;
    }

    private void postEvent(String line) {
        eventLog.add(line);
        if (eventLog.size() > 100) eventLog.remove(0);
        RoomInfoFragment ri = findRoomInfo();
        if (ri != null) ri.appendEvent(line);
    }

    // ──────────── RoomHubClient.Listener ────────────

    @Override
    public void onConnectionStateChanged(boolean isConnected, String message) {
        mainHandler.post(() -> {
            this.connected = isConnected;
            this.connectionMessage = message;
            RoomInfoFragment ri = findRoomInfo();
            if (ri != null) ri.setConnectionStatus(isConnected, message);
        });
    }

    @Override
    public void onRoomUpdated(JsonObject payload) {
        mainHandler.post(() -> {
            postEvent("Комната обновлена");
            // По ТЗ 4.1.1.6: уведомление о завершении сессии мастером.
            if (payload != null && payload.has("status") && !payload.get("status").isJsonNull()) {
                String status = payload.get("status").getAsString();
                if ("FINISHED".equalsIgnoreCase(status)) {
                    showSessionFinishedByMaster(payload);
                }
            }
        });
    }

    @Override
    public void onCharacterUpdated(JsonObject payload) {
        mainHandler.post(() -> postEvent("Персонаж обновлён"));
    }

    @Override
    public void onDiceRolled(JsonObject payload) {
        mainHandler.post(() -> {
            String kind = payload.has("kind") && !payload.get("kind").isJsonNull()
                    ? payload.get("kind").getAsString() : "?";
            String result = payload.has("result") && !payload.get("result").isJsonNull()
                    ? payload.get("result").getAsString() : "?";
            String userName = payload.has("userName") && !payload.get("userName").isJsonNull()
                    ? payload.get("userName").getAsString() : "?";
            postEvent("🎲 " + userName + ": " + kind + " = " + result);
        });
    }

    @Override
    public void onParticipantJoined(JsonObject payload) {
        mainHandler.post(() -> {
            String userName = payload.has("userName") && !payload.get("userName").isJsonNull()
                    ? payload.get("userName").getAsString() : "Игрок";
            postEvent("→ " + userName + " вошёл");
            RoomInfoFragment ri = findRoomInfo();
            if (ri != null) ri.loadParticipants();
        });
    }

    @Override
    public void onParticipantLeft(JsonObject payload) {
        mainHandler.post(() -> {
            String userName = payload.has("userName") && !payload.get("userName").isJsonNull()
                    ? payload.get("userName").getAsString() : "Игрок";
            postEvent("← " + userName + " вышел");
            RoomInfoFragment ri = findRoomInfo();
            if (ri != null) ri.loadParticipants();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hubClient != null) {
            hubClient.stop();
            hubClient = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
    }
}
