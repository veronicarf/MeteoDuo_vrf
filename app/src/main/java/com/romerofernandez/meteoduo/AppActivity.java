package com.romerofernandez.meteoduo;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppActivity extends AppCompatActivity {

    private String avatarSeleccionado = "avatar1";
    private ImageView imgAvatarPreview;

    private static final String TAG = "AppActivity";

    // UI
    private RecyclerView rvApp;
    private Button btnVolverApp, btnEliminarApp;

    // Adapter + datos
    private AppLogAdapter adapter;
    private final List<AppLog> data = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private ListenerRegistration registroListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        db = FirebaseFirestore.getInstance();

        // Enlazo UI (evita NPE)
        rvApp = findViewById(R.id.rvApp);
        btnVolverApp = findViewById(R.id.btnVolverApp);
        btnEliminarApp = findViewById(R.id.btnEliminarApp);

        if (rvApp == null || btnVolverApp == null || btnEliminarApp == null) {
            Toast.makeText(this, "Error UI: revisa activity_app.xml", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarRecycler();

        btnVolverApp.setOnClickListener(v -> finish());
        btnEliminarApp.setOnClickListener(v -> mostrarConfirmacionBorrado());

        escucharLogsFirestore(); // ✅ tiempo real
    }

    private void inicializarRecycler() {
        rvApp.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppLogAdapter(data, this::mostrarDetalleEvento);
        rvApp.setAdapter(adapter);
    }

    /**
     * Escucha en tiempo real logConexiones ordenado por timestamp (lo mejor para evitar desfases).
     */
    private void escucharLogsFirestore() {
        Query q = db.collection("logConexiones")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(200); // ajusta si quieres

        registroListener = q.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Log.e(TAG, "Error escuchando Firestore", e);
                Toast.makeText(this, "Error leyendo logs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snap == null) return;

            data.clear();

            for (DocumentSnapshot d : snap.getDocuments()) {
                String usuario = d.getString("usuario");
                String tipo = d.getString("tipo");
                String detalle = d.getString("detalleError");

                // Formateo hora/día desde timestamp (evita 1h de retraso)
                String dia = "-";
                String hora = "-";
                Timestamp ts = d.getTimestamp("timestamp");
                if (ts != null) {
                    Date date = ts.toDate();
                    dia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                    hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
                } else {
                    // si no hay timestamp, usa lo guardado (si existe)
                    String diaDb = d.getString("dia");
                    String horaDb = d.getString("hora");
                    if (!TextUtils.isEmpty(diaDb)) dia = diaDb;
                    if (!TextUtils.isEmpty(horaDb)) hora = horaDb;
                }

                if (usuario == null) usuario = "desconocido";
                if (tipo == null) tipo = "OTRO";
                if (detalle == null) detalle = "";

                data.add(new AppLog(d.getId(), dia, hora, usuario, tipo, detalle));
            }

            adapter.notifyDataSetChanged();
        });
    }

    private void abrirSelectorAvatar() {

        String[] avatares = {"avatar1", "avatar2", "avatar3", "avatar4"};

        new AlertDialog.Builder(this)
                .setTitle("Elige una imagen")
                .setItems(avatares, (dialog, which) -> {
                    avatarSeleccionado = avatares[which];
                    pintarAvatar(avatarSeleccionado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void pintarAvatar(String avatarKey) {
        int resId = getResources().getIdentifier(
                avatarKey,
                "drawable",
                getPackageName()
        );

        if (resId != 0) {
            imgAvatarPreview.setImageResource(resId);
        }
    }


    /**
     * Al pulsar una fila: muestra un diálogo con detalle del evento.
     * - Si es correcto: indica que no hubo error
     * - Si es error: muestra el tipo y el mensaje
     * + Botón "Ver errores de este usuario" (lista todos los errores del mismo email)
     */
    private void mostrarDetalleEvento(AppLog item) {
        String titulo = item.esError() ? "Detalle del error" : "Detalle";
        StringBuilder msg = new StringBuilder();
        msg.append("Usuario: ").append(item.usuario).append("\n");
        msg.append("Fecha: ").append(item.dia).append(" ").append(item.hora).append("\n");
        msg.append("Tipo: ").append(item.tipo).append("\n\n");

        if (item.esError()) {
            msg.append(item.detalleError);
        } else {
            msg.append("CORRECTO (sin errores).");
        }

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(msg.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /**
     * Muestra un diálogo con todos los errores (detalleError != "") de ese usuario.
     */
    private void mostrarErroresUsuario(String email) {
        db.collection("logConexiones")
                .whereEqualTo("usuario", email)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(qs -> {
                    StringBuilder sb = new StringBuilder();
                    int count = 0;

                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String detalle = d.getString("detalleError");
                        if (detalle == null || detalle.trim().isEmpty()) continue; // solo errores

                        String tipo = d.getString("tipo");
                        if (tipo == null) tipo = "OTRO";

                        String dia = d.getString("dia");
                        String hora = d.getString("hora");

                        Timestamp ts = d.getTimestamp("timestamp");
                        if (ts != null) {
                            Date date = ts.toDate();
                            dia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                            hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
                        }
                        if (dia == null) dia = "-";
                        if (hora == null) hora = "-";

                        count++;
                        sb.append(count).append(") [").append(dia).append(" ").append(hora).append("] ")
                                .append(tipo).append("\n")
                                .append(detalle).append("\n\n");
                    }

                    if (count == 0) {
                        sb.append("No hay errores registrados para este usuario.");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Errores de " + email)
                            .setMessage(sb.toString())
                            .setPositiveButton("Cerrar", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudieron cargar errores: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    /**
     * Confirmación antes de borrar.
     */
    private void mostrarConfirmacionBorrado() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar historial")
                .setMessage("¿Seguro que quieres borrar todo el historial de la app?")
                .setPositiveButton("Sí, borrar", (dialog, which) -> borrarHistorialFirestore())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Borra TODOS los documentos de logConexiones en lotes de 500.
     */
    private void borrarHistorialFirestore() {
        db.collection("logConexiones")
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Toast.makeText(this, "No hay registros para eliminar", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> docs = qs.getDocuments();

                    // Batches de 500
                    for (int i = 0; i < docs.size(); i += 500) {
                        int end = Math.min(i + 500, docs.size());
                        WriteBatch batch = db.batch();

                        for (int j = i; j < end; j++) {
                            batch.delete(docs.get(j).getReference());
                        }

                        batch.commit()
                                .addOnSuccessListener(v ->
                                        Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al borrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudo acceder al historial: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registroListener != null) registroListener.remove();
    }
}
