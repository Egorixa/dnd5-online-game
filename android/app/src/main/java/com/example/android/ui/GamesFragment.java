package com.example.android.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.data.model.Room;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.api.RoomsApi;
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
    /** Маппинг roomCode -> roomId для перехода в комнату по карточке. */
    private final Map<String, String> codeToRoomId = new HashMap<>();

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
        Button btnCreate = view.findViewById(R.id.btn_create_room);

        btnJoin.setOnClickListener(v -> {
            String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
            if (code.isEmpty()) {
                Toast.makeText(getContext(), "Введите код комнаты", Toast.LENGTH_SHORT).show();
                return;
            }
            joinRoomByCode(code);
        });

        btnCreate.setOnClickListener(v -> showCreateRoomDialog());

        RecyclerView rv = view.findViewById(R.id.rv_rooms);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoomAdapter(room -> joinRoomByCode(room.code));
        rv.setAdapter(adapter);

        loadPublicRooms();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isApiAvailable()) loadPublicRooms();
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
                List<Room> ui = new ArrayList<>();
                for (RoomDtos.PublicRoomDto r : response.body().rooms) {
                    codeToRoomId.put(r.roomCode, r.roomId);
                    ui.add(new Room(r.roomCode,
                            (r.masterUsername == null ? "" : r.masterUsername) + " · " + (r.name == null ? "" : r.name),
                            r.playersCount, /*max — backend не возвращает*/ 8, true));
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

    private void showCreateRoomDialog() {
        if (!isApiAvailable()) {
            Toast.makeText(getContext(), "Нет авторизации на сервере", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);
        EditText etName = new EditText(requireContext());
        etName.setHint("Название комнаты");
        container.addView(etName);

        new AlertDialog.Builder(requireContext())
                .setTitle("Новая комната")
                .setView(container)
                .setPositiveButton("Создать публичную", (d, w) ->
                        createRoom(etName.getText().toString().trim(), RoomDtos.AccessMode.PUBLIC))
                .setNeutralButton("Создать приватную", (d, w) ->
                        createRoom(etName.getText().toString().trim(), RoomDtos.AccessMode.PRIVATE))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void createRoom(String name, String accessMode) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }
        RoomDtos.CreateRoomRequest req = new RoomDtos.CreateRoomRequest(name, accessMode);
        ApiClient.get(requireContext()).rooms().create(req).enqueue(new Callback<RoomDtos.CreateRoomResponse>() {
            @Override
            public void onResponse(Call<RoomDtos.CreateRoomResponse> call, Response<RoomDtos.CreateRoomResponse> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            ApiErrors.extract(response, "Не удалось создать комнату"),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                RoomDtos.CreateRoomResponse r = response.body();
                Toast.makeText(getContext(),
                        "Комната создана. Код: " + r.roomCode, Toast.LENGTH_LONG).show();
                openRoom(r.roomId, r.roomCode, true);
            }

            @Override
            public void onFailure(Call<RoomDtos.CreateRoomResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Сервер недоступен: " + ApiErrors.fromThrowable(t, "сеть"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void joinRoomByCode(String code) {
        if (!isApiAvailable()) {
            Toast.makeText(getContext(), "Нет авторизации на сервере", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.get(requireContext()).rooms().join(code).enqueue(new Callback<RoomDtos.JoinRoomResponse>() {
            @Override
            public void onResponse(Call<RoomDtos.JoinRoomResponse> call, Response<RoomDtos.JoinRoomResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    RoomDtos.JoinRoomResponse j = response.body();
                    boolean isMaster = j.role != null
                            && RoomDtos.ParticipantRole.MASTER.equalsIgnoreCase(j.role);
                    openRoom(j.roomId, j.roomCode, isMaster);
                    return;
                }
                // 409 ALREADY_JOINED — пользователь уже в этой комнате (например, после
                // нажатия "назад" из GameRoomActivity без вызова leave). В этом случае
                // просто открываем экран комнаты, используя сохранённые ID/код.
                if (response.code() == 409) {
                    String roomId = codeToRoomId.get(code);
                    SessionManager sm = new SessionManager(requireContext());
                    if (roomId == null) roomId = sm.getActiveRoomId();
                    if (roomId != null) {
                        // Узнать роль точно мы здесь не можем, но обычно "уже в комнате"
                        // означает мастера, который её создал. На всякий случай уточним
                        // через GET /rooms/{id}/state — но проще довериться флагу из сессии.
                        boolean isMaster = code.equalsIgnoreCase(sm.getActiveRoomCode());
                        openRoom(roomId, code, isMaster);
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

    private void openRoom(String roomId, String roomCode, boolean isMaster) {
        new SessionManager(requireContext()).setActiveRoom(roomId, roomCode);
        Intent i = new Intent(requireContext(), GameRoomActivity.class);
        i.putExtra(GameRoomActivity.EXTRA_ROOM_CODE, roomCode);
        i.putExtra(GameRoomActivity.EXTRA_ROOM_ID, roomId);
        i.putExtra(GameRoomActivity.EXTRA_IS_MASTER, isMaster);
        startActivity(i);
    }
}
