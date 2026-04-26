package com.example.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android.R;

/**
 * Экран игровой комнаты (п. 4.1.1.6 ТЗ).
 *
 * - Подтверждение входа в комнату (по коду);
 * - Индикатор синхронизации (актуально / синхронизация / ошибка сети);
 * - Кнопка «Выйти из комнаты»;
 * - Заглушка «Завершение сессии мастером (победа/поражение)».
 *
 * При наличии серверной части подключение и WebSocket-канал заменят имитацию.
 */
public class GameRoomActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_CODE = "room_code";
    public static final String EXTRA_CHARACTER_ID = "character_id";

    private TextView tvSyncStatus;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int tick = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        String code = getIntent().getStringExtra(EXTRA_ROOM_CODE);
        int characterId = getIntent().getIntExtra(EXTRA_CHARACTER_ID, -1);

        TextView tvCode = findViewById(R.id.tv_room_code);
        tvCode.setText("Комната: " + (code != null ? code : "—"));

        TextView tvCharInfo = findViewById(R.id.tv_room_character);
        tvCharInfo.setText(characterId == -1
                ? "Персонаж не выбран. Откройте раздел «Персонажи» для выбора."
                : "Лист персонажа №" + characterId + " активен в режиме игровой сессии.");

        tvSyncStatus = findViewById(R.id.tv_sync_status);
        startSyncSimulation();

        Button btnLeave = findViewById(R.id.btn_leave_room);
        btnLeave.setOnClickListener(v -> confirmLeave());

        Button btnEndSim = findViewById(R.id.btn_simulate_end);
        btnEndSim.setOnClickListener(v -> showEndSessionDialog(true));
    }

    /** Имитация цикла индикатора синхронизации до прикрепления реального WebSocket. */
    private void startSyncSimulation() {
        handler.post(new Runnable() {
            @Override public void run() {
                tick++;
                if (tick % 6 == 0) {
                    tvSyncStatus.setText("Статус: ⚠ нет сети — повтор…");
                } else if (tick % 3 == 0) {
                    tvSyncStatus.setText("Статус: ⟳ синхронизация…");
                } else {
                    tvSyncStatus.setText("Статус: ● актуально");
                }
                handler.postDelayed(this, 1500);
            }
        });
    }

    private void confirmLeave() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из комнаты")
                .setMessage("Точно выйти? Изменения будут сохранены локально.")
                .setPositiveButton("Выйти", (d, w) -> finish())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEndSessionDialog(boolean victory) {
        new AlertDialog.Builder(this)
                .setTitle("Сессия завершена мастером")
                .setMessage(victory ? "Победа! Лист персонажа сохранён." : "Поражение.")
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
