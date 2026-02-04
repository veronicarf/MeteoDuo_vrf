package com.romerofernandez.meteoduo;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity encargada de mostrar el historial de consultas meteorológicas
 */
public class HistorialActivity extends AppCompatActivity {

    /** ListView donde se muestran las consultas guardadas */
    private ListView listHistorial;

    /** Botón para volver a la pantalla anterior */
    private Button btnVolver;

    /** Botón para eliminar todo el historial */
    private Button btnEliminarHistorial;

    /** Lista con los objetos JSON completos del historial */
    private final List<JSONObject> itemsJson = new ArrayList<>();

    /** Lista de textos formateados que se muestran en el ListView */
    private final List<String> itemsTexto = new ArrayList<>();

    /** Adapter que enlaza los textos con el ListView */
    private ArrayAdapter<String> adapter;

    /** UID del usuario actual */
    private String uid;

    /**
     * Método de inicialización de la Activity.
     *
     * @param savedInstanceState estado previo de la Activity (si existe)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // 1) UID del usuario actual (Firebase)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user != null) ? user.getUid() : "guest";

        // 2) Enlazo componentes del layout
        listHistorial = findViewById(R.id.listHistorial);
        btnVolver = findViewById(R.id.btnVolverHistorial);
        btnEliminarHistorial = findViewById(R.id.btnEliminarHistorial);

        // 3) Adapter del ListView
        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_historial,
                R.id.tvItemHistorial,
                itemsTexto
        );
        listHistorial.setAdapter(adapter);

        // 4) Cargo el historial del usuario actual
        cargarHistorial();

        // 5) Click en un item -> abre Resultados
        listHistorial.setOnItemClickListener((parent, view, position, id) -> {

            // Si no hay resultados reales, no hago nada
            if (itemsJson.isEmpty() || position >= itemsJson.size()) return;

            JSONObject obj = itemsJson.get(position);

            Intent i = new Intent(HistorialActivity.this, ResultadosActivity.class);
            i.putExtra("puntoA", obj.optString("puntoA"));
            i.putExtra("puntoB", obj.optString("puntoB"));
            i.putExtra("fechaInicio", obj.optString("fecha"));
            i.putExtra("jsonA", obj.optString("jsonA"));
            i.putExtra("jsonB", obj.optString("jsonB"));
            startActivity(i);
        });

        // 6) Botón volver
        btnVolver.setOnClickListener(v -> finish());

        // 7) Botón eliminar historial
        btnEliminarHistorial.setOnClickListener(v -> mostrarConfirmacionBorrado());
    }

    /**
     * Método para cargar el historial de consultas guardadas para el usuario actual
     * y rellena el ListView con un texto resumen de cada consulta.
     */
    private void cargarHistorial() {
        itemsJson.clear();
        itemsTexto.clear();

        List<JSONObject> list = HistorialStorage.getAll(this, uid);

        if (list.isEmpty()) {
            itemsTexto.add("No hay consultas guardadas todavía.");
            adapter.notifyDataSetChanged();
            return;
        }

        itemsJson.addAll(list);

        for (int i = 0; i < list.size(); i++) {
            JSONObject obj = list.get(i);

            String puntoA = obj.optString("puntoA", "A");
            String puntoB = obj.optString("puntoB", "B");
            String fecha  = obj.optString("fecha", "");

            itemsTexto.add((i + 1) + ". " + puntoA + " - " + puntoB + "   " + fecha);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Método que muestra un diálogo de confirmación antes de borrar todo el historial.
     */
    private void mostrarConfirmacionBorrado() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar historial")
                .setMessage("¿Seguro que quieres borrar todo tu historial?")
                .setPositiveButton("Sí, borrar", (dialog, which) -> {
                    HistorialStorage.clear(this, uid);
                    cargarHistorial();
                    Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
