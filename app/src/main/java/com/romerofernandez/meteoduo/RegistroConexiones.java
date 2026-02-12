package com.romerofernandez.meteoduo;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class RegistroConexiones {

    public static void guardarEvento(String email, String tipo, String detalleError) {

        if (email == null || email.trim().isEmpty()) email = "desconocido";
        if (tipo == null || tipo.trim().isEmpty()) tipo = "OTRO";
        if (detalleError == null) detalleError = "";

        // Si quieres seguir guardando dia/hora como texto:
        String dia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        Map<String, Object> doc = new HashMap<>();
        doc.put("usuario", email);
        doc.put("tipo", tipo);                 // "LOGIN", "GEOAPI", "WEATHER_API", etc.
        doc.put("detalleError", detalleError); // vacío => correcto
        doc.put("dia", dia);
        doc.put("hora", hora);
        doc.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("logConexiones")
                .add(doc);
    }

    // Atajo para “correcto”
    public static void ok(String email, String tipo) {
        guardarEvento(email, tipo, "");
    }

    // Atajo para “error”
    public static void error(String email, String tipo, String mensaje) {
        guardarEvento(email, tipo, mensaje);
    }
}
