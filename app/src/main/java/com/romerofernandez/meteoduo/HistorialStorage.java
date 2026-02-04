package com.romerofernandez.meteoduo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HistorialStorage {

    private static final String PREFS = "historial_prefs";
    private static final String KEY_PREFIX = "historial_json_"; // + uid

    public static List<JSONObject> getAll(Context ctx, String uid) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PREFIX + uid, "[]");

        List<JSONObject> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                out.add(arr.getJSONObject(i));
            }
        } catch (Exception ignored) {}

        return out;
    }

    public static void add(Context ctx, String uid, JSONObject nuevo) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PREFIX + uid, "[]");

        try {
            JSONArray arr = new JSONArray(raw);
            arr.put(nuevo);
            sp.edit().putString(KEY_PREFIX + uid, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static void clear(Context ctx, String uid) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_PREFIX + uid).apply();
    }
}
