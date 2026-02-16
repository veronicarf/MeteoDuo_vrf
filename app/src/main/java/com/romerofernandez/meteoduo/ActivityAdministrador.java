package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity principal del administrador.
 *
 * Muestra información básica del usuario (nombre y avatar) y permite navegar a:
 * Ajustes, Historial, Comparativa y App principal de administrador.
 *
 * @author Verónica Romero
 */
public class ActivityAdministrador extends AppCompatActivity {

    // ==============================
   // VARIABLES DE INTERFAZ// ==============================

    /** Imagen que muestra el avatar actual del usuario */
    private ImageView imgAvatar;

    /** TextView que muestra el nombre del usuario autenticado.*/
    private TextView tvNombreUsuario;

    /** Botón de menú lateral o desplegable.*/
    private ImageButton btnMenu;

    /** Botón que permite consultar el clima actual.*/
    private Button btnConsultar;

    /** Botón que redirige a la pantalla de historial.*/
    private Button btnHistorial;

    /** Botón que redirige a información sobre la aplicación */
    private Button btnApp;

    /** Botón para cerrar sesión o apagar la sesión actual. */
    private ImageButton btnApagar;



    // ==============================
   // VARIABLES DE FIREBASE
  // ==============================

    /** Instancia de FirebaseFirestore.*/
    private FirebaseFirestore db;

    /** Instancia de FirebaseAuth.*/
    private FirebaseAuth auth;





    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador);

        initFirebase();              // Inicializa las instancias de Firebase
        initViews();                //Vincula los elementos visuales del XML
        configurarBotones();      // Configura los listeners de los botones.

    }

    /**
     * Método que se ejecuta cada vez que la Activity pasa a estar visible.
     * Se utiliza para recargar los ajustes del usuario desde la nube.
     */
    @Override
    protected void onStart() {
        super.onStart();
        cargarAjustesNube();    // Cargamos los ajustes del usuario que incia sesión
    }

    /**
     * Método que inicializa las instancias de Firebase necesarias en esta pantalla.
     */

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Método que vincula los elementos de la interfaz  con sus IDs del XML.
     */

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        btnMenu = findViewById(R.id.btnMenu);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnApp = findViewById(R.id.btnApp);
        btnApagar = findViewById(R.id.btnclose2);
    }


    /**
     * Método que para Configura los listeners de los botones para la navegación entre pantallas.
     */
    private void configurarBotones() {

        // Ajustes
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, AjustesActivity.class))
        );

        // Historial
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, HistorialActivity.class))
        );

        // Comparativa / Consultar
        btnConsultar.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, ComparativaActivity.class))
        );

        // Ventana  admin
        btnApp.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, AppActivity.class))
        );

        //Apagar
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

        Intent i = new Intent(ActivityAdministrador.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }


    /**
     * Método para pintar el nombre del usuario en la interfaz,
     *
     * @param nombre nombre a mostrar (puede ser null o vacío)
     */
    private void pintarNombre(@Nullable String nombre) {
        if (tvNombreUsuario == null) return;

        if (nombre != null && !nombre.trim().isEmpty()) {
            tvNombreUsuario.setText(nombre.trim());
        }
    }

    /**
     * Método para pintar  el avatar del usuario en la interfaz, si es válido.
     *
     * @param avatar nombre del drawable
     */
    private void pintarAvatar(@Nullable String avatar) {
        if (imgAvatar == null) return;

        if (avatar != null && !avatar.trim().isEmpty()) {
            imgAvatar.setImageResource(drawableFromName(avatar.trim()));
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_default);
        }
    }

    /**
     * Método con el que se obtiene el id del recurso drawable a partir de su nombre.
     *
     * @param name nombre del drawable
     * @return id del recurso drawable correspondiente o avatar por defecto
     */
    @DrawableRes
    private int drawableFromName(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) return R.drawable.avatar_default;

        int resId = getResources().getIdentifier(name.trim(), "drawable", getPackageName());
        return (resId != 0) ? resId : R.drawable.avatar_default;
    }
    /**
     * Método para cargar desde Firestore los ajustes del usuario autenticado
     */
    private void cargarAjustesNube() {

        // Obtiene el usuario actualmente autenticado en Firebase
        FirebaseUser user = auth.getCurrentUser();

        // Si no hay usuario logueado, no se pueden cargar ajustes
        if (user == null) {


            // Salimos del método para evitar NullPointerException
            return;
        }

        // Obtiene el UID
        String uid = user.getUid();

        // Accede a la colección ajustes y al documento cuyo ID es el UID
        db.collection("ajustes").document(uid).get()

                // Si la consulta se realiza correctamente
                .addOnSuccessListener(doc -> {

                    // Si el documento no existe o es null, no hace nada
                    if (doc == null || !doc.exists()) return;

                    // Obtiene el nombre de usuario guardado en Firestore
                    String nombre = doc.getString("nombreUsuario");

                    // Obtiene el nombre del avatar
                    String avatar = doc.getString("avatar");

                    // Si el TextView existe y el nombre es válido, lo muestra
                    if (tvNombreUsuario != null && nombre != null && !nombre.trim().isEmpty()) {
                        tvNombreUsuario.setText(nombre.trim());
                    }

                    // Si el ImageView existe y el avatar es válido, lo carga
                    if (imgAvatar != null && avatar != null && !avatar.trim().isEmpty()) {

                        // Convierte el nombre del drawable en un recurso válido
                        imgAvatar.setImageResource(drawableFromName(avatar.trim()));
                    }
                })

                // Si ocurre un error al acceder a Firestore
                .addOnFailureListener(e ->

                        // Muestra un mensaje informando del error
                        Toast.makeText(this, "No se pudieron cargar ajustes", Toast.LENGTH_SHORT).show()
                );
    }

}


