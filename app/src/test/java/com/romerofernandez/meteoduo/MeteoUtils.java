package com.romerofernandez.meteoduo;
import org.json.JSONArray;
import java.util.Locale;

public class MeteoUtils {

    public static double celsiusToF(double c) {
        return (c * 9.0 / 5.0) + 32.0;
    }

    public static String formatTemp(double celsius, String unit) {
        if ("F".equalsIgnoreCase(unit)) {
            return String.format(Locale.getDefault(), "%.0f °F", celsiusToF(celsius));
        }
        return String.format(Locale.getDefault(), "%.0f °C", celsius);
    }

    public static String CodeTiempo(int code) {
        if (code == 0) return "Despejado";
        if (code == 1 || code == 2) return "Poco nuboso";
        if (code == 3) return "Nublado";
        if (code >= 45 && code <= 48) return "Niebla";
        if (code >= 51 && code <= 67) return "Lluvia";
        if (code >= 71 && code <= 77) return "Nieve";
        if (code >= 80 && code <= 82) return "Chubascos";
        if (code >= 95) return "Tormenta";
        return "Variable";
    }

    public static int safeInt(JSONArray arr, int index, int fallback) {
        try {
            if (arr == null || index < 0 || index >= arr.length()) return fallback;
            return arr.getInt(index);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static double safeDouble(JSONArray arr, int index, double fallback) {
        try {
            if (arr == null || index < 0 || index >= arr.length()) return fallback;
            return arr.getDouble(index);
        } catch (Exception e) {
            return fallback;
        }
    }
}
