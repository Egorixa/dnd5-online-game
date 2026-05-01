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
import com.example.android.net.dto.RoomDtos;
import com.example.android.net.realtime.RoomHubClient;
import com.google.gson.JsonObject;

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
    private Button btnFinish;

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

        TextView tvCharInfo = findViewById(R.id.tv_room_character);
        tvCharInfo.setText(characterId == -1
                ? "Персонаж не выбран. Откройте раздел «Персонажи» для выбора."
                : "Лист персонажа №" + characterId + " активен в режиме игровой сессии.");

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
