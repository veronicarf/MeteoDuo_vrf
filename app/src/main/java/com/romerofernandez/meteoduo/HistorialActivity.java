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
 * Activity encargada de mostrar el historial de consultas guardadas del usuario actual.
 */
public class HistorialActivity extends AppCompatActivity {

  //ListView donde se muestran las consultas guardadas.
    private ListView listHistorial;

  //Botones
   private Button btnVolver;
   private Button btnEliminarHistorial;

    //Lista con los objetos JSON completos del historial.
    private final List<JSONObject> itemsJson = new ArrayList<>();

   //Lista de textos formateados que se muestran en el ListView.
    private final List<String> itemsTexto = new ArrayList<>();

    // Adapter que enlaza los textos con el ListView.
    private ArrayAdapter<String> adapter;

    //UID del usuario actual
    private String uid;

    /**
     * Método de inicialización de la Activity.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        if (!inicializarSesion()) return;

        enlazarVistas();
        if (!uiValida()) return;

        configurarListView();
        cargarHistorial();
        configurarEventos();
    }
    /**
     * Obtiene el usuario actual de Firebase y guarda su UID.
     *
     * @return true si hay sesión activa, false si no la hay (y cierra la Activity).
     */
    private boolean inicializarSesion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            RegistroConexiones.error("desconocido", "SEGURIDAD",
                    "HistorialActivity abierta sin sesión (user=null)");
            finish();
            return false;
        }
        uid = user.getUid();
        return true;
    }

    /** Enlaza los componentes del layout con las variables Java. */
    private void enlazarVistas() {
        listHistorial = findViewById(R.id.listHistorial);
        btnVolver = findViewById(R.id.btnVolverHistorial);
        btnEliminarHistorial = findViewById(R.id.btnEliminarHistorial);
    }

    /**
     * Comprueba que los componentes de la interfaz existen.
     *
     * @return true si la UI está bien enlazada, false si hay algún null
     */
    private boolean uiValida() {
        if (listHistorial == null || btnVolver == null || btnEliminarHistorial == null) {
            RegistroConexiones.error(emailActual(), "UI",
                    "HistorialActivity: algún componente es null (revisa IDs en activity_historial.xml)");
            finish();
            return false;
        }
        return true;
    }

    /** Inicializa el adapter del ListView y lo asigna a la lista. */
    private void configurarListView() {
        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_historial,
                R.id.tvItemHistorial,
                itemsTexto
        );
        listHistorial.setAdapter(adapter);
    }

    /** Configura listeners del ListView y de los botones. */
    private void configurarEventos() {

        // Click en un item -> abre ResultadosActivity con los datos guardados
        listHistorial.setOnItemClickListener((parent, view, position, id) -> abrirResultadoDesdeItem(position));

        // Botón volver
        btnVolver.setOnClickListener(v -> finish());

        // Botón eliminar historial (con confirmación)
        btnEliminarHistorial.setOnClickListener(v -> mostrarConfirmacionBorrado());
    }

    /**
     * Abre ResultadosActivity con la consulta seleccionada del historial.
     *
     * @param position posición pulsada en el ListView
     */
    private void abrirResultadoDesdeItem(int position) {

        // Si no hay resultados reales, no hace nada
        if (itemsJson.isEmpty() || position >= itemsJson.size()) return;

        JSONObject obj = itemsJson.get(position);

        Intent i = new Intent(HistorialActivity.this, ResultadosActivity.class);
        i.putExtra("puntoA", obj.optString("puntoA"));
        i.putExtra("puntoB", obj.optString("puntoB"));
        i.putExtra("fechaInicio", obj.optString("fecha"));
        i.putExtra("jsonA", obj.optString("jsonA"));
        i.putExtra("jsonB", obj.optString("jsonB"));
        startActivity(i);
    }


    /**
     * Método que carga el historial de consultas del usuario actual y actualiza el ListView.
     */
    private void cargarHistorial() {
        // Limpia las listas para reconstruir el contenido desde cero
        itemsJson.clear();
        itemsTexto.clear();

        // Obtiene todas las consultas guardadas para el usuario
        List<JSONObject> list = HistorialStorage.getAll(this, uid);

        // Si no hay elementos y se muestra un texto informativo
        if (list.isEmpty()) {
            itemsTexto.add("No hay consultas guardadas todavía.");
            adapter.notifyDataSetChanged();
            return;
        }

        // Guarda los JSON
        itemsJson.addAll(list);

        // Construye el texto que se verá en el ListView
        for (int i = 0; i < list.size(); i++) {
            JSONObject obj = list.get(i);

            String puntoA = obj.optString("puntoA", "A");
            String puntoB = obj.optString("puntoB", "B");
            String fecha  = obj.optString("fecha", "");

            itemsTexto.add((i + 1) + ". " + puntoA + " - " + puntoB + "   " + fecha);
        }

        // Refresca el ListView
        adapter.notifyDataSetChanged();
    }

    /**
     * Método que obtiene el email del usuario autenticado
     *
     * @return email del usuario actual o {@code "desconocido"} si no hay sesión/email
     */
    private String emailActual() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return (u != null && u.getEmail() != null) ? u.getEmail() : "desconocido";
    }

    /**
     * Muestra un diálogo de confirmación antes de borrar todo el historial.
     */
    private void mostrarConfirmacionBorrado() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar historial")
                .setMessage("¿Seguro que quieres borrar todo tu historial?")
                .setPositiveButton("Sí, borrar", (dialog, which) -> {
                    try {
                        // Borra el historial del usuario actual
                        HistorialStorage.clear(this, uid);

                        // Registra borrado correcto
                        RegistroConexiones.ok(emailActual(), "HISTORIAL");

                        // Recarga la lista para reflejar los cambios
                        cargarHistorial();

                        // Informa al usuario
                        Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        String msg = (e.getMessage() != null)
                                ? e.getMessage()
                                : "Error borrando historial (Storage)";
                        RegistroConexiones.error(emailActual(), "STORAGE",
                                "Error borrando historial: " + msg);
                        Toast.makeText(this, "No se pudo borrar el historial", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
