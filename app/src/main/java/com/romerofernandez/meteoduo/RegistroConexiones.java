package com.romerofernandez.meteoduo;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Clase utilitaria encargada de registrar eventos de la aplicación en Firestore.
 */
public class RegistroConexiones {

    /**
     * Este método inserta un documento en la colección logConexiones} con los datos del evento.
     *
     * @param email email del usuario que genera el evento
     * @param tipo tipo de evento
     * @param detalleError mensaje de error
     */
    public static void guardarEvento(String email, String tipo, String detalleError) {

        // Valores por defecto para evitar nulos o cadenas vacías
        if (email == null || email.trim().isEmpty()) email = "desconocido";
        if (tipo == null || tipo.trim().isEmpty()) tipo = "OTRO";
        if (detalleError == null) detalleError = "";

        // Obtiene la fecha actual en formato texto
        String dia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        // Obtiene la hora actual en formato texto
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());

        // Construye el documento a guardar en Firestore
        Map<String, Object> doc = new HashMap<>();
        doc.put("usuario", email);
        doc.put("tipo", tipo);
        doc.put("detalleError", detalleError);
        doc.put("dia", dia);
        doc.put("hora", hora);

        // Timestamp del servidor
        doc.put("timestamp", FieldValue.serverTimestamp());

        // Inserta el documento en la colección logConexiones
        FirebaseFirestore.getInstance()
                .collection("logConexiones")
                .add(doc);
    }

    /**

     * Método para registrar operaciones que han finalizado correctamente,
     *
     * @param email email del usuario
     * @param tipo  tipo de evento
     */
    public static void ok(String email, String tipo) {
        guardarEvento(email, tipo, "");
    }

    /**
     *Método para registrar registrar errores producidos durante la ejecución
     *
     * @param email   email del usuario
     * @param tipo    tipo de evento
     * @param mensaje descripción del error ocurrido
     */
    public static void error(String email, String tipo, String mensaje) {
        guardarEvento(email, tipo, mensaje);
    }
}
