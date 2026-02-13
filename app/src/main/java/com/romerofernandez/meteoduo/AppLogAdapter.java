package com.romerofernandez.meteoduo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
  * Adapter del RecyclerView encargado de mostrar la lista de registros AppLog.
 * Conecta los datos (AppLog) con la interfaz (item_app_log.xml).
 */
public class AppLogAdapter extends RecyclerView.Adapter<AppLogAdapter.VH> {



    // Lista de registros que se mostrarán en el RecyclerView */
    private final List<AppLog> data;

    //Listener que gestiona el clic sobre un elemento */
    private final OnItemClick listener;

    // Interfaz para gestionar el clic sobre una fila del RecyclerView.

    public interface OnItemClick {
        void onClick(AppLog item);
    }
    /**
     * Constructor del adapter.
     *
     * @param data lista de AppLog a mostrar
     * @param listener acción a ejecutar cuando se pulsa una fila
     */
    public AppLogAdapter(List<AppLog> data, OnItemClick listener) {
        this.data = data;         // Guarda la lista de datos
        this.listener = listener; // Guarda el listener de clics
    }

    /**
    * Representa una fila del RecyclerView
     */
    static class VH extends RecyclerView.ViewHolder {

        // Elementos visuales de la fila
        TextView tvDia, tvHora, tvUsuario, tvEstado;

        /**
         * Constructor del ViewHolder.
         *
         * @param itemView vista completa de la fila
         */
        VH(@NonNull View itemView) {
            super(itemView);

            // Enlaza los TextView del XML con las variables Java
            tvDia = itemView.findViewById(R.id.tvDia);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }

    /**
     * Crea un nuevo ViewHolder cuando el RecyclerView lo necesita.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Infla el layout XML de una fila (item_app_log.xml)
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_log, parent, false);

        // Devuelve un nuevo ViewHolder con esa vista
        return new VH(v);
    }

    /**
     * Asocia los datos de un AppLog con los elementos visuales de la fila.
     *
     * @param h ViewHolder que contiene la vista
     * @param position posición del elemento en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        // Obtiene el AppLog correspondiente a esta posición
        AppLog item = data.get(position);

        // Muestra el día del evento
        h.tvDia.setText(item.dia);

        // Muestra la hora del evento
        h.tvHora.setText(item.hora);

        // Muestra el usuario
        h.tvUsuario.setText(item.usuario);

        // Muestra el estado según sea error o correcto
        h.tvEstado.setText(item.esError() ? "ERROR" : "CORRECTO");

        // Gestiona el clic sobre toda la fila
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item); // Llama al método definido en la Activity
            }
        });
    }

    /**
     * Devuelve el número total de elementos de la lista.
     *
     * @return tamaño de la lista data
     */
    @Override
    public int getItemCount() {
        return data.size();
    }
}

