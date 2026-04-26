package com.example.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.R;
import com.example.android.data.model.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomVH> {

    public interface OnRoomConnect {
        void onConnect(Room room);
    }

    private final List<Room> items = new ArrayList<>();
    private final OnRoomConnect listener;

    public RoomAdapter(OnRoomConnect listener) { this.listener = listener; }

    public void setItems(List<Room> rooms) {
        items.clear();
        if (rooms != null) items.addAll(rooms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomVH h, int position) {
        Room r = items.get(position);
        h.tvMaster.setText("Мастер: " + r.masterName);
        h.tvInfo.setText("Игроков: " + r.playersCount + "/" + r.maxPlayers + "  ·  Код: " + r.code);
        h.btnConnect.setOnClickListener(v -> listener.onConnect(r));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class RoomVH extends RecyclerView.ViewHolder {
        TextView tvMaster, tvInfo;
        Button btnConnect;
        RoomVH(View v) {
            super(v);
            tvMaster = v.findViewById(R.id.tv_master_name);
            tvInfo = v.findViewById(R.id.tv_room_info);
            btnConnect = v.findViewById(R.id.btn_connect_room);
        }
    }
}
