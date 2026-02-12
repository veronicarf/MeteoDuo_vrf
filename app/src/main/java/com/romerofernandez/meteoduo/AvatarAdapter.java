package com.romerofernandez.meteoduo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.VH> {

    public interface OnAvatarClick {
        void onClick(String avatarKey);
    }

    private final List<String> avatarKeys;
    private final OnAvatarClick listener;

    public AvatarAdapter(List<String> avatarKeys, OnAvatarClick listener) {
        this.avatarKeys = avatarKeys;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgAvatarItem);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String key = avatarKeys.get(position);

        int resId = h.itemView.getContext().getResources()
                .getIdentifier(key, "drawable", h.itemView.getContext().getPackageName());

        if (resId != 0) h.img.setImageResource(resId);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(key);
        });
    }

    @Override
    public int getItemCount() {
        return avatarKeys.size();
    }
}
