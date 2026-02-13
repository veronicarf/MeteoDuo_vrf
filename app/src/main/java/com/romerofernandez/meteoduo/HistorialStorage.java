package com.romerofernandez.meteoduo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase  encargada de gestionar el almacenamiento del historial
 */
public class HistorialStorage {


    //Nombre del fichero  donde se almacena el historial.
       private static final String PREFS = "historial_prefs";


     // Prefijo de la clave utilizada en SharedPreferences.
    private static final String KEY_PREFIX = "historial_json_"; // + uid

    /**
     * Método que obtiene todas las consultas guardadas en el historial de un usuario.
     *
     * @param ctx Contexto de la aplicación o Activity
     * @param uid UID del usuario (Firebase)
     * @return Lista de objetos JSONObject con las consultas guardadas
     */
    public static List<JSONObject> getAll(Context ctx, String uid) {

        // Acceso a las preferencias
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Se obtiene el JSON almacenado; si no existe, se usa un array vacío
        String raw = sp.getString(KEY_PREFIX + uid, "[]");

        List<JSONObject> out = new ArrayList<>();

        try {
            // Se parsea el texto JSON a un JSONArray
            JSONArray arr = new JSONArray(raw);

            // Se convierte cada elemento del array en JSONObject
            for (int i = 0; i < arr.length(); i++) {
                out.add(arr.getJSONObject(i));
            }
        } catch (Exception ignored) {
            // Si hay error de parseo, se devuelve la lista vacía
        }

        return out;
    }

    /**
     * Método que añade una nueva consulta al historial del usuario indicado.
     *
     * @param ctx Contexto de la aplicación o Activity
     * @param uid UID del usuario (Firebase)
     * @param nuevo Objeto JSONObject con los datos de la consulta a guardar
     */
    public static void add(Context ctx, String uid, JSONObject nuevo) {

        // Acceso a las preferencias
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Se recupera el historial actual del usuario
        String raw = sp.getString(KEY_PREFIX + uid, "[]");

        try {
            // Se convierte el texto guardado en JSONArray
            JSONArray arr = new JSONArray(raw);

            // Se añade la nueva consulta al array
            arr.put(nuevo);

            // Se guarda de nuevo el array completo en SharedPreferences
            sp.edit()
                    .putString(KEY_PREFIX + uid, arr.toString())
                    .apply();

        } catch (Exception ignored) {
            // En caso de error, no se guarda la consulta
        }
    }

    /**
     * Método que elimina todo el historial de consultas del usuario indicado.
     *
     * @param ctx Contexto de la aplicación o Activity
     * @param uid UID del usuario (Firebase)
     */
    public static void clear(Context ctx, String uid) {

        // Acceso a las preferencias
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Se elimina la clave correspondiente al historial del usuario
        sp.edit()
                .remove(KEY_PREFIX + uid)
                .apply();
    }
}
