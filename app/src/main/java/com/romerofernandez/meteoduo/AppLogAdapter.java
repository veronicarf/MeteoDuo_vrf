package com.romerofernandez.meteoduo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppLogAdapter extends RecyclerView.Adapter<AppLogAdapter.VH> {

    public interface OnItemClick {
        void onClick(AppLog item);
    }

    private final List<AppLog> data;
    private final OnItemClick listener;

    public AppLogAdapter(List<AppLog> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDia, tvHora, tvUsuario, tvEstado;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDia = itemView.findViewById(R.id.tvDia);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppLog item = data.get(position);

        h.tvDia.setText(item.dia);
        h.tvHora.setText(item.hora);
        h.tvUsuario.setText(item.usuario);

        h.tvEstado.setText(item.esError() ? "ERROR" : "CORRECTO");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
