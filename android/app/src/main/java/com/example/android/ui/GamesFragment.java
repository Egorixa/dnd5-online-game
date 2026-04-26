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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.data.model.Room;
import com.example.android.ui.adapter.RoomAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class GamesFragment extends Fragment {

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
            openRoom(code);
        });

        RecyclerView rv = view.findViewById(R.id.rv_rooms);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        RoomAdapter adapter = new RoomAdapter(room -> openRoom(room.code));
        rv.setAdapter(adapter);

        // Mock публичных комнат для демонстрации
        List<Room> mock = Arrays.asList(
                new Room("AB12-CD34", "Master_DnD", 3, 6, true),
                new Room("XY99-ZZ01", "Гэндальф", 4, 5, true),
                new Room("RS55-PP77", "Tasha", 2, 8, true)
        );
        adapter.setItems(mock);
    }

    private void openRoom(String code) {
        Intent i = new Intent(requireContext(), GameRoomActivity.class);
        i.putExtra(GameRoomActivity.EXTRA_ROOM_CODE, code);
        startActivity(i);
    }
}
