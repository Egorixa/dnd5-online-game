package com.example.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiceLogAdapter extends RecyclerView.Adapter<DiceLogAdapter.VH> {

    private final List<String> items;

    public DiceLogAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        tv.setPadding(24, 16, 24, 16);
        return new VH(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ((TextView) h.itemView).setText(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View v) { super(v); }
    }
}
