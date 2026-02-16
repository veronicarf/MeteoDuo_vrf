package com.romerofernandez.meteoduo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter del RecyclerView encargado de mostrar una lista de avatares.
 *
 *  @author Verónica Romero
 */
public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.VH> {

    // ==============================
   // DATOS DE AVATARES
  // ==============================

    /** Lista de claves que identifican los avatares disponibles. */
    private final List<String> avatarKeys;

    /** Listener que gestiona el clic sobre un avatar seleccionado. */
    private final OnAvatarClick listener;


    // ==============================
   // INTERFAZ DE SELECCIÓN
  // ==============================

    /** Interfaz que define la acción al seleccionar un avatar. */
    public interface OnAvatarClick {
        void onClick(String avatarKey);
    }


    /**
     * Constructor del adapter.
     *
     * @param avatarKeys lista de claves de avatares a mostrar
     * @param listener acción a ejecutar cuando se pulsa un avatar
     */
    public AvatarAdapter(List<String> avatarKeys, OnAvatarClick listener) {
        this.avatarKeys = avatarKeys; // Guarda la lista de avatares
        this.listener = listener;     // Guarda el listener de clics
    }


    /**
     * ViewHolder.
     *
     * Representa una celda del RecyclerView que contiene la imagen de un avatar.
     */
    static class VH extends RecyclerView.ViewHolder {

      // ImageView donde se muestra el avatar
        ImageView img;

        /**
         * Constructor del ViewHolder.
         *
         * @param itemView vista completa del item
         */
        VH(@NonNull View itemView) {
            super(itemView);

            // Enlaza el ImageView del layout item_avatar.xml
            img = itemView.findViewById(R.id.imgAvatarItem);
        }
    }

    /**
     * Crea un nuevo ViewHolder inflando el layout del item de avatar.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Infla el layout XML que representa un avatar
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_avatar, parent, false);

        // Devuelve el ViewHolder asociado a esa vista
        return new VH(v);
    }

    /**
     * Asocia cada clave de avatar con su imagen y gestiona el clic.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        // Obtiene la clave del avatar correspondiente a esta posición
        String key = avatarKeys.get(position);

        // Obtiene el identificador del drawable a partir del nombre
        int resId = h.itemView.getContext()
                .getResources()
                .getIdentifier(
                        key,                           // Nombre del drawable
                        "drawable",                    // Tipo de recurso
                        h.itemView.getContext().getPackageName() // Paquete de la app
                );

        // Si el drawable existe, se muestra en el ImageView
        if (resId != 0) {
            h.img.setImageResource(resId);
        }

        // Gestiona el clic sobre el avatar
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(key); // Notifica a la Activity qué avatar se ha seleccionado
            }
        });
    }

    /**
     * Devuelve el número total de avatares disponibles.
     */
    @Override
    public int getItemCount() {
        return avatarKeys.size();
    }
}
