package com.example.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoomEventAdapter extends RecyclerView.Adapter<RoomEventAdapter.VH> {

    public static class Item {
        public final String text;
        public final long timestamp;
        public final String icon;

        public Item(String text, long timestamp, String icon) {
            this.text = text;
            this.timestamp = timestamp;
            this.icon = icon;
        }
    }

    private final List<Item> items;
    private final SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public RoomEventAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = items.get(position);
        h.tvIcon.setText(it.icon == null || it.icon.isEmpty() ? "•" : it.icon);
        h.tvTime.setText(tf.format(new Date(it.timestamp)));
        h.tvText.setText(it.text == null ? "" : it.text);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIcon, tvTime, tvText;
        VH(@NonNull View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tv_event_icon);
            tvTime = v.findViewById(R.id.tv_event_time);
            tvText = v.findViewById(R.id.tv_event_text);
        }
    }
}
