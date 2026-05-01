package com.example.android.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.dto.DiceDtos;
import com.example.android.ui.adapter.DiceLogAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Экран бросков кубиков (п. 4.1.1.7 ТЗ).
 * При активной серверной комнате — POST /rooms/{id}/dice/roll;
 * иначе — локальный random.
 */
public class DiceFragment extends Fragment {

    private final Random random = new Random();
    private final List<String> log = new ArrayList<>();
    private DiceLogAdapter adapter;
    private final SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private static final int[] SIDES = {4, 6, 8, 10, 12, 20, 100};
    private static final String[] DICE_KINDS = {
            DiceDtos.DiceKind.D4, DiceDtos.DiceKind.D6, DiceDtos.DiceKind.D8,
            DiceDtos.DiceKind.D10, DiceDtos.DiceKind.D12, DiceDtos.DiceKind.D20,
            DiceDtos.DiceKind.D100
    };

    private SessionManager session;
    private TextView tvStatus;
    private SwitchMaterial swHidden;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        tvStatus = view.findViewById(R.id.tv_dice_room_status);
        swHidden = view.findViewById(R.id.sw_dice_hidden);

        int[] btnIds = {R.id.btn_d4, R.id.btn_d6, R.id.btn_d8, R.id.btn_d10,
                R.id.btn_d12, R.id.btn_d20, R.id.btn_d100};
        for (int i = 0; i < btnIds.length; i++) {
            final int sides = SIDES[i];
            final String kind = DICE_KINDS[i];
            view.findViewById(btnIds[i]).setOnClickListener(v -> roll(sides, kind));
        }

        view.findViewById(R.id.btn_clear_log).setOnClickListener(v -> {
            log.clear();
            adapter.notifyDataSetChanged();
        });

        RecyclerView rv = view.findViewById(R.id.rv_dice_log);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DiceLogAdapter(log);
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        String roomId = session.getActiveRoomId();
        String roomCode = session.getActiveRoomCode();
        if (!TextUtils.isEmpty(roomId)) {
            tvStatus.setText("Режим: комната " + (TextUtils.isEmpty(roomCode) ? "—" : roomCode));
            swHidden.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("Режим: локальный (нет активной комнаты)");
            swHidden.setVisibility(View.GONE);
        }
    }

    private void roll(int sides, String kind) {
        String roomId = session.getActiveRoomId();
        if (!TextUtils.isEmpty(roomId) && session.hasServerSession()) {
            rollServer(roomId, kind, sides);
        } else {
            rollLocal(sides);
        }
    }

    private void rollLocal(int sides) {
        int result = 1 + random.nextInt(sides);
        appendLog(tf.format(new Date()) + "  d" + sides + " → " + result);
    }

    private void rollServer(String roomId, String kind, int sides) {
        String mode = (swHidden != null && swHidden.isChecked())
                ? DiceDtos.DiceMode.HIDDEN : DiceDtos.DiceMode.PUBLIC;
        DiceDtos.DiceRollRequest req = new DiceDtos.DiceRollRequest(kind, mode, 0);
        ApiClient.get(requireContext()).dice().roll(roomId, req)
                .enqueue(new Callback<DiceDtos.DiceRollResponse>() {
                    @Override
                    public void onResponse(Call<DiceDtos.DiceRollResponse> call,
                                           Response<DiceDtos.DiceRollResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(),
                                    ApiErrors.extract(response, "Ошибка броска"),
                                    Toast.LENGTH_SHORT).show();
                            // fallback локально
                            rollLocal(sides);
                            return;
                        }
                        DiceDtos.DiceRollResponse r = response.body();
                        StringBuilder line = new StringBuilder(tf.format(new Date()))
                                .append("  ").append(r.dice == null ? kind : r.dice);
                        if (r.result != null) {
                            line.append(" → ").append(r.result);
                        }
                        if (r.total != null && r.modifier != null && r.modifier != 0) {
                            line.append(" (итого ").append(r.total).append(")");
                        }
                        if (DiceDtos.DiceMode.HIDDEN.equalsIgnoreCase(r.mode)) {
                            line.append(" [скрытый]");
                        }
                        appendLog(line.toString());
                    }

                    @Override
                    public void onFailure(Call<DiceDtos.DiceRollResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "офлайн"),
                                Toast.LENGTH_SHORT).show();
                        rollLocal(sides);
                    }
                });
    }

    private void appendLog(String entry) {
        log.add(0, entry);
        adapter.notifyItemInserted(0);
    }
}
