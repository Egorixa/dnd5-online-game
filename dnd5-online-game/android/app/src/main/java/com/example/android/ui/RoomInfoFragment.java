package com.example.android.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.R;
import com.example.android.data.SessionManager;
import com.example.android.net.ApiClient;
import com.example.android.net.dto.RoomDtos;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomInfoFragment extends Fragment {

    public interface RoomInfoHost {
        void onRoomInfoLeaveClicked();
        String getRoomId();
        String getRoomCode();
    }

    private TextView tvCode, tvStatus, tvParticipants, tvEvents;
    private Button btnLeave;
    private final StringBuilder eventsBuffer = new StringBuilder();

    private boolean lastConnected = false;
    @Nullable private String lastConnectionMsg = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCode = view.findViewById(R.id.tv_ri_room_code);
        tvStatus = view.findViewById(R.id.tv_ri_status);
        tvParticipants = view.findViewById(R.id.tv_ri_participants);
        tvEvents = view.findViewById(R.id.tv_ri_events_log);
        btnLeave = view.findViewById(R.id.btn_ri_leave);

        RoomInfoHost host = host();
        if (host != null) {
            tvCode.setText("Комната: " + (host.getRoomCode() != null ? host.getRoomCode() : "—"));
            btnLeave.setOnClickListener(v -> host.onRoomInfoLeaveClicked());
            loadParticipants();
        }
        renderStatus();
        tvEvents.setText(eventsBuffer.toString());
    }

    private RoomInfoHost host() {
        if (getActivity() instanceof RoomInfoHost) return (RoomInfoHost) getActivity();
        return null;
    }

    public void loadParticipants() {
        RoomInfoHost h = host();
        if (h == null || TextUtils.isEmpty(h.getRoomId())) return;
        ApiClient.get(requireContext()).rooms().getState(h.getRoomId())
                .enqueue(new Callback<RoomDtos.RoomStateDto>() {
                    @Override
                    public void onResponse(Call<RoomDtos.RoomStateDto> call,
                                           Response<RoomDtos.RoomStateDto> response) {
                        if (!isAdded() || tvParticipants == null) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            tvParticipants.setText("Не удалось загрузить участников");
                            return;
                        }
                        renderState(response.body());
                    }

                    @Override
                    public void onFailure(Call<RoomDtos.RoomStateDto> call, Throwable t) {
                        if (!isAdded() || tvParticipants == null) return;
                        tvParticipants.setText("Сеть: " + (t.getMessage() != null ? t.getMessage() : "ошибка"));
                    }
                });
    }

    private void renderState(RoomDtos.RoomStateDto state) {
        StringBuilder sb = new StringBuilder();
        if (state.participants != null) {
            String currentUserId = new SessionManager(requireContext()).getServerUserId();
            for (RoomDtos.RoomParticipantDto p : state.participants) {
                String role = RoomDtos.ParticipantRole.MASTER.equals(p.role) ? "👑 " : "• ";
                String you = (currentUserId != null && currentUserId.equals(p.userId)) ? " (вы)" : "";
                sb.append(role).append(p.username == null ? "—" : p.username).append(you).append('\n');
            }
        }
        if (sb.length() == 0) sb.append("(нет участников)");
        tvParticipants.setText(sb.toString().trim());
    }

    public void setConnectionStatus(boolean connected, @Nullable String message) {
        lastConnected = connected;
        lastConnectionMsg = message;
        renderStatus();
    }

    private void renderStatus() {
        if (tvStatus == null) return;
        String connection = lastConnected
                ? "● актуально"
                : "⚠ " + (lastConnectionMsg != null ? lastConnectionMsg : "нет связи");
        tvStatus.setText("Связь: " + connection);
    }

    public void appendEvent(String line) {
        eventsBuffer.insert(0, line + "\n");
        if (tvEvents != null) tvEvents.setText(eventsBuffer.toString());
    }
}
