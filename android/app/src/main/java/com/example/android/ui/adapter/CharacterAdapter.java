package com.example.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.R;
import com.example.android.data.model.Character;
import java.util.ArrayList;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterVH> {

    public interface OnCharacterAction {
        void onClick(Character character);
        void onDelete(Character character);
    }

    private final List<Character> items = new ArrayList<>();
    private final OnCharacterAction listener;

    public CharacterAdapter(OnCharacterAction listener) {
        this.listener = listener;
    }

    public void setItems(List<Character> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CharacterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character_card, parent, false);
        return new CharacterVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterVH h, int position) {
        Character c = items.get(position);
        h.tvName.setText(c.characterName);
        String subtitle = c.race + " · " + c.characterClass + " · ур. " + c.level;
        h.tvSubtitle.setText(subtitle);
        h.tvHp.setText("HP: " + c.currentHp + "/" + c.maxHp + "  КД: " + c.armorClass);

        h.itemView.setOnClickListener(v -> listener.onClick(c));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CharacterVH extends RecyclerView.ViewHolder {
        TextView tvName, tvSubtitle, tvHp;
        ImageButton btnDelete;
        CharacterVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_char_name);
            tvSubtitle = v.findViewById(R.id.tv_char_subtitle);
            tvHp = v.findViewById(R.id.tv_char_hp);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
