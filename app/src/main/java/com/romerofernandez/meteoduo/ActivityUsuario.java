package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity principal del usuario estándar.
  * Muestra el nombre y avatar del usuario autenticado
 * y permite navegar a Ajustes, Consultar y Historial.
 */
public class ActivityUsuario extends AppCompatActivity {

    // Variables UI

    private ImageView imgAvatar;
    private TextView tvNombreUsuario;
    private ImageButton btnMenu;
    private Button btnConsultar;
    private Button btnHistorial;

    // Variables  Firebase

    private FirebaseFirestore db;
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
}
