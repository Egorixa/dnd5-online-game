package com.example.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.api.CharactersApi;
import com.example.android.net.dto.CharacterDtos;
import com.example.android.net.dto.RoomDtos;
import com.example.android.net.realtime.RoomHubClient;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Экран игровой комнаты (п. 4.1.1.6 ТЗ).
 * Использует SignalR для приёма событий комнаты и REST для leave/finish.
 */
public class GameRoomActivity extends AppCompatActivity implements RoomHubClient.Listener {

    public static final String EXTRA_ROOM_ID = "room_id";
    public static final String EXTRA_ROOM_CODE = "room_code";
    public static final String EXTRA_CHARACTER_ID = "character_id";
    public static final String EXTRA_IS_MASTER = "is_master";

    private TextView tvSyncStatus;
    private TextView tvEventsLog;
    private TextView tvCharInfo;
    private Button btnFinish;
    private Button btnSelectCharacter;
    private String activeCharacterId;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder eventsBuffer = new StringBuilder();

    private RoomHubClient hubClient;
    private SessionManager session;

    private String roomId;
    private String roomCode;
    private boolean isMaster;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        session = new SessionManager(getApplicationContext());

        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        roomCode = getIntent().getStringExtra(EXTRA_ROOM_CODE);
        int characterId = getIntent().getIntExtra(EXTRA_CHARACTER_ID, -1);
        isMaster = getIntent().getBooleanExtra(EXTRA_IS_MASTER, false);

        if (TextUtils.isEmpty(roomId)) {
            roomId = session.getActiveRoomId();
        }
        if (TextUtils.isEmpty(roomCode)) {
            roomCode = session.getActiveRoomCode();
        }

        TextView tvCode = findViewById(R.id.tv_room_code);
        tvCode.setText("Комната: " + (roomCode != null ? roomCode : "—"));

        tvCharInfo = findViewById(R.id.tv_room_character);
        tvCharInfo.setText("Персонаж не выбран. Нажмите «Выбрать персонажа».");

        btnSelectCharacter = findViewById(R.id.btn_select_character);
        if (btnSelectCharacter != null) {
            btnSelectCharacter.setOnClickListener(v -> showSelectCharacterDialog());
        }

        tvSyncStatus = findViewById(R.id.tv_sync_status);
        tvSyncStatus.setText("Статус: ⟳ подключение…");

        tvEventsLog = findViewById(R.id.tv_events_log);
        if (tvEventsLog != null) tvEventsLog.setText("");

        Button btnLeave = findViewById(R.id.btn_leave_room);
        btnLeave.setOnClickListener(v -> confirmLeave());

        btnFinish = findViewById(R.id.btn_simulate_end);
        if (btnFinish != null) {
            btnFinish.setText("Завершить сессию (мастер)");
            btnFinish.setVisibility(isMaster ? View.VISIBLE : View.GONE);
            btnFinish.setOnClickListener(v -> confirmFinish());
        }

        startHub();
        loadActiveCharacter();
    }

    private void loadActiveCharacter() {
        if (TextUtils.isEmpty(roomId)) return;
        ApiClient.get(this).characters().listInRoom(roomId).enqueue(new Callback<CharactersApi.CharactersListResponse>() {
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
                tvCharInfo.setText("Активный персонаж: " + (c.name == null ? "(без имени)" : c.name));
            }

            @Override
            public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) { /* ignore */ }
        });
    }

    private void showSelectCharacterDialog() {
        ApiClient.get(this).characters().listTemplates().enqueue(new Callback<CharactersApi.CharactersListResponse>() {
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
                        "Сеть: " + ApiErrors.fromThrowable(t),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyTemplateToRoom(CharacterDtos.CharacterResponse template) {
        if (TextUtils.isEmpty(roomId)) return;
        CharacterDtos.CharacterUpsertRequest req = templateToUpsert(template);
        ApiClient.get(this).characters().createInRoom(roomId, req).enqueue(new Callback<CharacterDtos.CharacterResponse>() {
            @Override
            public void onResponse(Call<CharacterDtos.CharacterResponse> call,
                                   Response<CharacterDtos.CharacterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeCharacterId = response.body().characterId;
                    String name = response.body().name == null ? "(без имени)" : response.body().name;
                    tvCharInfo.setText("Активный персонаж: " + name);
                    Toast.makeText(GameRoomActivity.this,
                            "Персонаж добавлен в комнату",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GameRoomActivity.this,
                            ApiErrors.extract(response, "Не удалось добавить персонажа"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CharacterDtos.CharacterResponse> call, Throwable t) {
                Toast.makeText(GameRoomActivity.this,
                        "Сеть: " + ApiErrors.fromThrowable(t),
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
        // skills/saves: SkillView/SaveView → ProficiencyLevel.level
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

    private void startHub() {
        if (TextUtils.isEmpty(roomId)) {
            tvSyncStatus.setText("Статус: ⚠ нет ID комнаты");
            return;
        }
        String token = session.getToken();
        if (TextUtils.isEmpty(token)) {
            tvSyncStatus.setText("Статус: ⚠ нет токена");
            return;
        }
        hubClient = new RoomHubClient(token, this);
        hubClient.start(roomId);
    }

    private void confirmLeave() {
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
                        "Ошибка выхода: " + ApiErrors.fromThrowable(t),
                        Toast.LENGTH_SHORT).show();
                session.clearActiveRoom();
                finish();
            }
        });
    }

    private void confirmFinish() {
        final boolean[] victory = {true};
        new AlertDialog.Builder(this)
                .setTitle("Завершить сессию")
                .setItems(new CharSequence[]{"Победа", "Поражение"}, (d, which) -> {
                    victory[0] = which == 0;
                    sendFinish(victory[0]);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void sendFinish(boolean victory) {
        RoomDtos.FinishRoomRequest req = new RoomDtos.FinishRoomRequest(
                new java.util.ArrayList<>(), new java.util.ArrayList<>());
        ApiClient.get(this).rooms().finish(roomId, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showEndSessionDialog(victory);
                } else {
                    Toast.makeText(GameRoomActivity.this,
                            "Не удалось завершить: " + ApiErrors.extract(response),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GameRoomActivity.this,
                        "Сеть: " + ApiErrors.fromThrowable(t),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEndSessionDialog(boolean victory) {
        new AlertDialog.Builder(this)
                .setTitle("Сессия завершена")
                .setMessage(victory ? "Победа! Лист персонажа сохранён." : "Поражение.")
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> {
                    session.clearActiveRoom();
                    finish();
                })
                .show();
    }

    private void appendEvent(String line) {
        eventsBuffer.insert(0, line + "\n");
        if (tvEventsLog != null) {
            tvEventsLog.setText(eventsBuffer.toString());
        }
    }

    // ===== RoomHubClient.Listener =====

    @Override
    public void onConnectionStateChanged(boolean connected, String message) {
        mainHandler.post(() -> tvSyncStatus.setText(connected
                ? "Статус: ● актуально"
                : "Статус: ⚠ " + (message != null ? message : "нет связи")));
    }

    @Override
    public void onRoomUpdated(JsonObject payload) {
        mainHandler.post(() -> appendEvent("Комната обновлена"));
    }

    @Override
    public void onCharacterUpdated(JsonObject payload) {
        mainHandler.post(() -> appendEvent("Персонаж обновлён"));
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
            appendEvent("🎲 " + userName + ": " + kind + " = " + result);
        });
    }

    @Override
    public void onParticipantJoined(JsonObject payload) {
        mainHandler.post(() -> {
            String userName = payload.has("userName") && !payload.get("userName").isJsonNull()
                    ? payload.get("userName").getAsString() : "Игрок";
            appendEvent("→ " + userName + " вошёл");
        });
    }

    @Override
    public void onParticipantLeft(JsonObject payload) {
        mainHandler.post(() -> {
            String userName = payload.has("userName") && !payload.get("userName").isJsonNull()
                    ? payload.get("userName").getAsString() : "Игрок";
            appendEvent("← " + userName + " вышел");
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
