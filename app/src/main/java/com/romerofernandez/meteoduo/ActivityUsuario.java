package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity principal del usuario estándar.
  * Muestra el nombre y avatar del usuario autenticado
 * y permite navegar a Ajustes, Consultar y Historial.
 *
 * @author Verónica Romero
 */
public class ActivityUsuario extends AppCompatActivity {

     // ==============================
    // VARIABLES DE INTERFAZ
   // ==============================

    /** Imagen del avatar del usuario autenticado. */
    private ImageView imgAvatar;

    /** Muestra el nombre del usuario actualmente autenticado.*/
    private TextView tvNombreUsuario;

    /** Botón para abrir el menú de ajustes*/
    private ImageButton btnMenu;

    /** Botón que permite realizar una consulta meteorológica.*/
    private Button btnConsultar;

    /** Botón que redirige a la pantalla de historial de consultas.*/
    private Button btnHistorial;

    /** Botón para cerrar sesión del usuario actual.*/
    private ImageButton btnApagar;


    // ==============================
   // VARIABLES DE FIREBASE
  // ==============================

    /** Instancia de FirebaseFirestore*/
    private FirebaseFirestore db;

    /** Instancia de FirebaseAuth*/
    private FirebaseAuth auth;






    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asocia la clase con el layout XML correspondiente
        setContentView(R.layout.activity_usuario);

        // Inicializa Firebase
        initFirebase();

        // Vincula los elementos del XML con las variables
        initViews();

        // Configura los botones y su navegación
        configurarBotones();

        //En caso de nuevo usuario, llamará al metodo para mostrar tutorial
        mostrarTutorialSiPrimeraVez();

    }

    /**
     * Método que se ejecuta cada vez que la Activity pasa a estar visible.
     * Se utiliza para recargar los ajustes del usuario desde la nube.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Recarga nombre y avatar por si han cambiado en Ajustes
        cargarAjustesNube();
    }

    /**
     * Método que inicializa las instancias de Firebase necesarias en esta pantalla.
     */
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Método que vincula los elementos de la interfaz con sus IDs del XML.
     */
    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        btnMenu = findViewById(R.id.btnMenu);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnApagar = findViewById(R.id.btnclose);
    }

    /**
     * Método que para Configura los listeners de los botones para la navegación entre pantallas.
     */
    private void configurarBotones() {

        // Ir a Ajustes
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(ActivityUsuario.this, AjustesActivity.class))
        );

        // Ir a Comparativa / Consultar
        btnConsultar.setOnClickListener(v ->
                startActivity(new Intent(ActivityUsuario.this, ComparativaActivity.class))
        );

        // Ir a Historial
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(ActivityUsuario.this, HistorialActivity.class))
        );

        //Cerrar sesión
        btnApagar.setOnClickListener(v -> mostrarConfirmacionCerrarSesion());

    }

    /**
     * Muestra un diálogo de confirmación antes de cerrar sesión.
     */
    private void mostrarConfirmacionCerrarSesion() {

        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar la sesión?")
                .setPositiveButton("Sí", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Cierra la sesión del usuario y vuelve a la pantalla de login.
     */
    private void cerrarSesion() {
        auth.signOut();

        Intent i = new Intent(ActivityUsuario.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    /**
     * Carga desde Firestore los ajustes del usuario autenticado
     * (nombre y avatar) y los muestra en la interfaz.
     */
    private void cargarAjustesNube() {

        // Obtiene el usuario autenticado actualmente
        FirebaseUser user = auth.getCurrentUser();

        // Si no hay usuario logueado, se cancela la operación
        if (user == null) return;

        // Obtiene el UID del usuario
        String uid = user.getUid();

        // Accede al documento del usuario dentro de la colección "ajustes"
        db.collection("ajustes").document(uid).get()

                .addOnSuccessListener(doc -> {

                    // Si el documento no existe, no hacemos nada
                    if (doc == null || !doc.exists()) return;

                    // Obtiene el nombre guardado
                    String nombre = doc.getString("nombreUsuario");

                    // Obtiene el avatar guardado
                    String avatar = doc.getString("avatar");

                    // Muestra el nombre si es válido
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        tvNombreUsuario.setText(nombre.trim());
                    }

                    // Muestra el avatar si es válido
                    if (avatar != null && !avatar.trim().isEmpty()) {
                        imgAvatar.setImageResource(drawableFromName(avatar.trim()));
                    }
                })

                // En caso de error en la consulta
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudieron cargar ajustes", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Método con el que se obtiene el id del recurso drawable a partir de su nombre.
     *
     * @param name nombre del drawable
     * @return id del recurso drawable correspondiente o avatar por defecto
     */
    private int drawableFromName(String name) {

        if (name == null || name.trim().isEmpty())
            return R.drawable.avatar_default;

        int resId = getResources().getIdentifier(name.trim(), "drawable", getPackageName());

        return (resId != 0) ? resId : R.drawable.avatar_default;
    }



    /**
     * Método para mostrar tutorial incial a nuevos usuarios.
     * */
    private void mostrarTutorialSiPrimeraVez() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);

        boolean yaVisto = sp.getBoolean("tutorial_" + uid, false);

        if (!yaVisto) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, TutorialOverlayFragment.newInstance(), "TUTORIAL")
                    .commit();
        }
    }


}
