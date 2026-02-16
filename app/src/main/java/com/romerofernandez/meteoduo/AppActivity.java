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

/**
 * AppActivity
 * Actividad encargada de mostrar el historial de eventos de la aplicación.
 *
 *  @author Verónica Romero
 */
public class AppActivity extends AppCompatActivity {

    // ==============================
   // CONSTANTES
  // ==============================

    /** Etiqueta utilizada para el registro de logs en Logcat.*/
    private static final String TAG = "AppActivity";


     // ==============================
    // ATRIBUTOS RELACIONADOS CON AVATAR
   // ==============================

    /** Nombre del avatar seleccionado */
    private String avatarSeleccionado = "avatar1";

    /** Vista previa del avatar actual del usuario.*/
    private ImageView imgAvatarPreview;


    // ==============================
   // VARIABLES DE INTERFAZ
  // ==============================

    /** RecyclerView que muestra el listado de registros*/
    private RecyclerView rvApp;

    /** Botón para volver a la pantalla anterior.*/
    private Button btnVolverApp;

    /** Botón que permite eliminar los registros almacenados*/
    private Button btnEliminarApp;


   // ==============================
  // ADAPTER Y DATOS
 // ==============================

    /** Adaptador encargado de enlazar los datos del modelo AppLog*/
    private AppLogAdapter adapter;

    /** Lista que almacena los registros recuperados desde Firestore.*/

    private final List<AppLog> data = new ArrayList<>();


      // ==============================
     // VARIABLES DE FIREBASE
    // ==============================

    /**Instancia de FirebaseFirestore.*/

    private FirebaseFirestore db;

    /** ListenerRegistration que mantiene una suscripción en tiempo real */
    private ListenerRegistration registroListener;




    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        // instancia de Firestore para poder consultar la BD
        inicializarFirestore();

        // Enlaza los componentes del XML con las variables
        enlazarVistas();

        // Configura RecyclerView y listeners de botones
        configurarUI();

        // Inicia la escucha en tiempo real de la colección logConexiones
        escucharLogsFirestore();
    }

    /**
     * Método que inicializa la instancia de Firestore.
     */
    private void inicializarFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Método para enlazar los elementos de la interfaz y comprueba que existan.
     */
    private void enlazarVistas() {
        //Enazamos RecyclerView
        rvApp = findViewById(R.id.rvApp);
        btnVolverApp = findViewById(R.id.btnVolverApp);
        btnEliminarApp = findViewById(R.id.btnEliminarApp);

        // Si alguna vista no existe en el layout se evita el crash cerrando la actividad

        if (rvApp == null || btnVolverApp == null || btnEliminarApp == null) {
            Toast.makeText(this, "Error UI: revisa activity_app.xml", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Configura el RecyclerView y los listeners de los botones.
     */
    private void configurarUI() {
        inicializarRecycler();

        btnVolverApp.setOnClickListener(v -> finish());
        btnEliminarApp.setOnClickListener(v -> mostrarConfirmacionBorrado());
    }


    /**
     * Método que inicializa el RecyclerView con su LayoutManager y adapter.
     */
    private void inicializarRecycler() {

        // la lista se muestra en vertical
        rvApp.setLayoutManager(new LinearLayoutManager(this));

        // Crea el adapter y define qué pasa al pulsar una fila
        adapter = new AppLogAdapter(data, this::mostrarDetalleEvento);

        // Conecta el adapter con el RecyclerView para que pinte los datos
        rvApp.setAdapter(adapter);
    }


    /**
     * Método que escucha en tiempo real la colección logConexiones ordenada por timestamp
     */
    private void escucharLogsFirestore() {

       // Se crea una consulta a Firestore sobre la colección "logConexiones" / orden ascndente /limite de 200 documentos
        Query q = db.collection("logConexiones")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(200);

        //Se añade un listener en tiempo real que se ejecuta al cargar y cada vez que cambie la colección
        registroListener = q.addSnapshotListener((snap, e) -> {

       // Si Firestore devuelve un error, lo registramos y avisamos al usuario
            if (e != null) {
                Log.e(TAG, "Error escuchando Firestore", e);
                Toast.makeText(this, "Error leyendo logs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            //Si no hay snapshot, no hay nada que procesar
            if (snap == null) return;

            // Se elimina la lista para reconstruirla y evitar duplicados
            data.clear();

            //Recorremos los documentos obtenidos en la consulta

            for (DocumentSnapshot d : snap.getDocuments()) {

                //campos del documento
                String usuario = d.getString("usuario");
                String tipo = d.getString("tipo");
                String detalle = d.getString("detalleError");

                // Fecha y hora
                String dia = "-";
                String hora = "-";

                //obtenemos time del documento
                Timestamp ts = d.getTimestamp("timestamp");
                     // Exite : convertimos a date
                if (ts != null) {
                    Date date = ts.toDate();
                    dia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                    hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);

                    // Si no hay timestamp, se intenta usar "dia" y "hora" guardados como texto
                } else {
                    String diaDb = d.getString("dia");
                    String horaDb = d.getString("hora");
                    if (!TextUtils.isEmpty(diaDb)) dia = diaDb;
                    if (!TextUtils.isEmpty(horaDb)) hora = horaDb;
                }
                   //Si los valores existen  se asignan
                if (usuario == null) usuario = "desconocido";
                if (tipo == null) tipo = "OTRO";
                if (detalle == null) detalle = "";

                   //Se crea un AppLog con los datos del documento y lo añade a la lista del RecyclerView
                data.add(new AppLog(d.getId(), dia, hora, usuario, tipo, detalle));
            }
                  // Se notifica al adapter para que refresque la lista en pantalla
            adapter.notifyDataSetChanged();
        });
    }



    /**
     * Muestra el detalle de un evento al pulsar una fila del RecyclerView.
     *
     * @param item evento seleccionado
     */
    private void mostrarDetalleEvento(AppLog item) {
        String titulo = item.esError() ? "Detalle del error" : "Detalle"; // Título distinto si es error o correcto
        StringBuilder msg = new StringBuilder();

        // Añadimos datos
        msg.append("Usuario: ").append(item.usuario).append("\n");
        msg.append("Fecha: ").append(item.dia).append(" ").append(item.hora).append("\n");
        msg.append("Tipo: ").append(item.tipo).append("\n\n");

        //Muestra errores o correcto
        if (item.esError()) {
            msg.append(item.detalleError);
        } else {
            msg.append("CORRECTO (sin errores).");
        }

        // Crea y muestra el AlertDialog con el detalle
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(msg.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /**
     * Muestra un diálogo con todos los errores registrados para un usuario.
     *
     * @param email email del usuario
     */
    private void mostrarErroresUsuario(String email) {

        // Accedemos a la collección
        db.collection("logConexiones")
                .whereEqualTo("usuario", email) //filtramos por usuario
                .orderBy("timestamp", Query.Direction.DESCENDING)//ordena por más reciente
                .limit(50)       //limita a 50 ressultados
                .get() //consultamos

                //Contruyee el ttexto  del dialogo mientras cuenta errores
                .addOnSuccessListener(qs -> {
                    StringBuilder sb = new StringBuilder();
                    int count = 0;

                    //Una vez recibimos los documentos los recorremos
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String detalle = d.getString("detalleError"); //lee error
                        if (detalle == null || detalle.trim().isEmpty()) continue; // no hayy , contunua

                        //leemos los datos
                        String tipo = d.getString("tipo");
                        if (tipo == null) tipo = "OTRO";

                        String dia = d.getString("dia");
                        String hora = d.getString("hora");

                        //hay timestamp, lo convertimos a date
                        Timestamp ts = d.getTimestamp("timestamp");
                        if (ts != null) {
                            Date date = ts.toDate();
                            dia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                            hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
                        }

                        if (dia == null) dia = "-";
                        if (hora == null) hora = "-";


                        // datos
                        count++;
                        sb.append(count).append(") [").append(dia).append(" ").append(hora).append("] ")
                                .append(tipo).append("\n")
                                .append(detalle).append("\n\n");
                    }

                    if (count == 0) {
                        sb.append("No hay errores registrados para este usuario.");
                    }

                    // Muestra el diálogo con todos los errores
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
     * Muestra un diálogo de confirmación antes de borrar el historial.
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
     * Borra todos los documentos de la colección logConexiones
     */
    private void borrarHistorialFirestore() {

        //Accedemos a la colección y obtenemos datos

        db.collection("logConexiones")
                .get()
                .addOnSuccessListener(qs -> {

                    //Esta vacía
                    if (qs.isEmpty()) {
                        Toast.makeText(this, "No hay registros para eliminar", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Hay datos y borramos
                    List<DocumentSnapshot> docs = qs.getDocuments();

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



    /**
     * Libera el listener de Firestore al destruir la actividad.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registroListener != null) {
            registroListener.remove();
        }
    }}
