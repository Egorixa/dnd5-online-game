package com.example.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.R;
import com.example.android.ui.adapter.DiceLogAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Экран бросков кубиков и журнала результатов.
 * Реализует п. 4.1.1.7 ТЗ: d4, d6, d8, d10, d12, d20, d100.
 */
public class DiceFragment extends Fragment {

    private final Random random = new Random();
    private final List<String> log = new ArrayList<>();
    private DiceLogAdapter adapter;
    private final SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private static final int[] SIDES = {4, 6, 8, 10, 12, 20, 100};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] btnIds = {R.id.btn_d4, R.id.btn_d6, R.id.btn_d8, R.id.btn_d10,
                R.id.btn_d12, R.id.btn_d20, R.id.btn_d100};
        for (int i = 0; i < btnIds.length; i++) {
            final int sides = SIDES[i];
            view.findViewById(btnIds[i]).setOnClickListener(v -> roll(sides));
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

    private void roll(int sides) {
        int result = 1 + random.nextInt(sides);
        String entry = tf.format(new Date()) + "  d" + sides + " → " + result;
        log.add(0, entry);
        adapter.notifyItemInserted(0);
    }
}
