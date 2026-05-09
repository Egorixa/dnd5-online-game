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
import com.example.android.util.NotificationHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private boolean connected;
    @Nullable private String connectionMessage;

    private final List<String> eventLog = new ArrayList<>();

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

        if (!TextUtils.isEmpty(roomId)) {
            session.setActiveRoom(roomId, roomCode == null ? "" : roomCode);
        }

        TabLayout tabs = findViewById(R.id.tabs_room);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        if (savedInstanceState == null) {
            showTab(0);
        }

        startHub();
        loadActiveCharacter();
    }

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

                        TabLayout tabs = findViewById(R.id.tabs_room);
                        if (tabs != null && tabs.getSelectedTabPosition() == 0) {
                            resetSheetTab();
                        }
                    }

                    @Override
                    public void onFailure(Call<CharactersApi.CharactersListResponse> call, Throwable t) {  }
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
                            String clsRu = com.example.android.net.mapper.CharacterMapper.classFromKey(c.characterClass);
                            String raceRu = com.example.android.net.mapper.CharacterMapper.raceFromKey(c.race);
                            StringBuilder sb = new StringBuilder(name);
                            if (raceRu != null && !raceRu.isEmpty()) sb.append(" · ").append(raceRu);
                            if (clsRu != null && !clsRu.isEmpty()) sb.append(" · ").append(clsRu);
                            sb.append(" · ур. ").append(c.level);
                            titles[i] = sb.toString();
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

    private void startHub() {
        if (TextUtils.isEmpty(roomId)) return;
        String token = session.getToken();
        if (TextUtils.isEmpty(token)) return;
        hubClient = new RoomHubClient(token, this);
        hubClient.start(roomId);
    }

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
        mainHandler.post(() -> {
            JsonObject inner = null;
            if (payload != null && payload.has("character")
                    && payload.get("character").isJsonObject()) {
                inner = payload.getAsJsonObject("character");
            }
            String action = readString(payload, "action");

            String who = extractCharacterOwnerLabel(payload, inner);
            String prefix;
            if ("created".equalsIgnoreCase(action)) prefix = "➕ Лист создан: ";
            else if ("deleted".equalsIgnoreCase(action)) prefix = "🗑 Лист удалён: ";
            else prefix = "✏ Лист обновлён: ";
            postEvent(prefix + who);

            String me = session != null ? session.getServerUserId() : null;
            String owner = readString(payload, "ownerUserId", "userId");
            if (owner == null && inner != null) {
                owner = readString(inner, "ownerUserId", "userId");
            }
            boolean mySheet = me != null && owner != null && me.equalsIgnoreCase(owner);

            if ("deleted".equalsIgnoreCase(action)) {
                return;
            }

            Fragment sheet = getSupportFragmentManager().findFragmentByTag(TAG_SHEET);
            if (sheet instanceof CharacterEditorFragment && mySheet) {
                ((CharacterEditorFragment) sheet).reloadFromServerIfRoomMode();
                Toast.makeText(this, "Мастер обновил ваш лист", Toast.LENGTH_SHORT).show();
            }
            if (mySheet) {
                NotificationHelper.notifyCharacterUpdated(
                        getApplicationContext(),
                        "Мастер изменил ваш лист персонажа");
            }
        });
    }

    @Override
    public void onDiceRolled(JsonObject payload) {
        mainHandler.post(() -> {
            String dice = readString(payload, "dice", "kind");

            if (dice == null && payload != null) {
                for (String key : new String[]{"dice", "kind"}) {
                    if (payload.has(key) && !payload.get(key).isJsonNull()) {
                        JsonElement el = payload.get(key);
                        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                            dice = mapDiceKindIndex(el.getAsInt());
                            break;
                        }
                    }
                }
            }

            if (dice != null) {
                try {
                    int idx = Integer.parseInt(dice);
                    dice = mapDiceKindIndex(idx);
                } catch (NumberFormatException ignored) {
                }
            }

            if (dice == null || dice.isEmpty() || "?".equals(dice)) dice = "d?";

            String resultStr = readString(payload, "result");
            String totalStr = readString(payload, "total");
            String magic = readString(payload, "magicBallAnswer");

            String userLabel = extractDiceUserLabel(payload);

            StringBuilder line = new StringBuilder("🎲 ").append(userLabel).append(": ");
            if (magic != null && !magic.isEmpty()) {
                line.append("шар → ").append(magic);
            } else {
                line.append(dice);
                if (resultStr != null) line.append(" = ").append(resultStr);
                if (totalStr != null && !totalStr.equals(resultStr)) {
                    line.append(" (итого ").append(totalStr).append(")");
                }
            }
            postEvent(line.toString());
        });
    }

    private static String mapDiceKindIndex(int idx) {
        switch (idx) {
            case 0: return "d4";
            case 1: return "d6";
            case 2: return "d8";
            case 3: return "d10";
            case 4: return "d12";
            case 5: return "d20";
            case 6: return "d100";
            case 7: return "MAGIC_BALL";
            default: return "d?";
        }
    }

    @Nullable
    private static String readString(JsonObject payload, String... keys) {
        if (payload == null) return null;
        for (String key : keys) {
            if (payload.has(key) && !payload.get(key).isJsonNull()) {
                JsonElement el = payload.get(key);
                if (el.isJsonPrimitive()) {
                    String v = el.getAsString();
                    if (v != null && !v.isEmpty()) return v;
                }
            }
        }
        return null;
    }

    private String extractDiceUserLabel(JsonObject payload) {
        String name = readString(payload, "userName", "playerName", "characterName");
        if (name != null) return name;
        String actor = readString(payload, "actorUserId", "userId");
        if (actor != null) {
            String me = session != null ? session.getServerUserId() : null;
            if (actor.equalsIgnoreCase(me)) return "Вы";
            return "Игрок " + actor.substring(0, Math.min(6, actor.length()));
        }
        return "Игрок";
    }

    private String extractCharacterOwnerLabel(JsonObject payload, JsonObject inner) {
        if (payload == null && inner == null) return "персонаж";
        String name = payload != null ? readString(payload, "characterName") : null;
        if (name == null && inner != null) name = readString(inner, "name", "characterName");
        if (name == null && payload != null) name = readString(payload, "name");
        String userName = payload != null ? readString(payload, "ownerUserName") : null;
        if (name != null && userName != null) return name + " (" + userName + ")";
        if (name != null) return name;
        if (userName != null) return userName;
        String owner = payload != null ? readString(payload, "ownerUserId", "userId") : null;
        if (owner == null && inner != null) owner = readString(inner, "ownerUserId", "userId");
        if (owner != null) {
            String me = session != null ? session.getServerUserId() : null;
            if (owner.equalsIgnoreCase(me)) return "ваш лист";
            return "лист игрока " + owner.substring(0, Math.min(6, owner.length()));
        }
        return "персонаж";
    }

    @Override
    public void onParticipantJoined(JsonObject payload) {
        mainHandler.post(() -> {
            String userName = readString(payload, "username", "userName");
            if (userName == null) userName = "Игрок";
            postEvent("→ " + userName + " вошёл");
            RoomInfoFragment ri = findRoomInfo();
            if (ri != null) ri.loadParticipants();
        });
    }

    @Override
    public void onParticipantLeft(JsonObject payload) {
        mainHandler.post(() -> {
            String userName = readString(payload, "username", "userName");
            if (userName == null) userName = "Игрок";
            boolean kicked = payload != null && payload.has("kicked")
                    && !payload.get("kicked").isJsonNull()
                    && payload.get("kicked").getAsBoolean();
            postEvent((kicked ? "✖ " : "← ") + userName + (kicked ? " исключён мастером" : " вышел"));
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
