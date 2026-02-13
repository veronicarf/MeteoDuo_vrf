package com.romerofernandez.meteoduo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity de Ajustes.
 * Permite al usuario configurar:
 * <ul>
 *     <li>Nombre de usuario</li>
 *     <li>Unidad de temperatura (C/F)</li>
 *     <li>Mostrar/ocultar bloques (viento, cielo, lluvia)</li>
 *     <li>Avatar </li>
 * </ul>
 * Los ajustes se guardan y se cargan desde Firestore en la colección "ajustes"
 * usando el UID del usuario autenticado.
 */
public class AjustesActivity extends AppCompatActivity {

    // Variables UI
    private EditText etNombreUsuario;
    private RadioGroup rgTemp;
    private RadioButton rbC, rbF;
    private CheckBox cbViento, cbCielo, cbLluvia;

    private ImageView imgAvatarPreview;
    private Button btnCambiarImagen, btnGuardar, btnSalir;

    // Variables Firebase
    private FirebaseFirestore db;
    private String uid;


    // Variables para avatar y nombre
    private String avatarSeleccionado = "avatar1";
    private String unidadTemp = "C";

    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asocia la Activity con su layout XML
        setContentView(R.layout.activity_ajustes);

        // Inicializa Firebase y obtiene el UID del usuario autenticado
        if (!initFirebaseAndUid()) return;

        // Vincula vistas del XML con variables Java
        initViews();

        // Configura listeners de botones y controles
        configurarListeners();

        // Carga ajustes guardados en Firestore (o aplica valores por defecto)
        cargarAjustes();
    }

    /**
     * Método que nicializa Firestore y valida que exista un usuario autenticado.
     *
     * @return true si hay usuario autenticado y se obtuvo el UID; false en caso contrario
     */
    private boolean initFirebaseAndUid() {
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay sesión iniciada", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        uid = user.getUid();
        return true;
    }

    /**
     * Vincula los componentes del layout con las variables Java (findViewById).
     */
    private void initViews() {

        // Campos de texto y selección de temperatura
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        rgTemp = findViewById(R.id.rgTemp);
        rbC = findViewById(R.id.rbC);
        rbF = findViewById(R.id.rbF);

        // Checkboxes
        cbViento = findViewById(R.id.cbViento);
        cbCielo = findViewById(R.id.cbCielo);
        cbLluvia = findViewById(R.id.cbLluvia);

        // Avatar
        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);
        btnCambiarImagen = findViewById(R.id.btnCambiarImagen);

        // Botones
        btnGuardar = findViewById(R.id.btnGuardarAjustes);
        btnSalir = findViewById(R.id.btnSalirAjustes);
    }

    /**
     *Método que configura los listeners de la interfaz:
     */

    private void configurarListeners() {

        // Abre el selector de avatar en un diálogo
        btnCambiarImagen.setOnClickListener(v -> abrirSelectorAvatar());

        // Detecta cambios en el RadioGroup de temperatura
        rgTemp.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbF) {
                unidadTemp = "F";
            } else {
                unidadTemp = "C";
            }
        });

        // Guarda los ajustes en Firestore
        btnGuardar.setOnClickListener(v -> guardarAjustes());

        // Cierra la pantalla de ajustes
        btnSalir.setOnClickListener(v -> finish());
    }

    /**
     * Método para argar los ajustes del usuario desde Firestore.
     * Si no existen ajustes previos, aplica valores por defecto
     */

    private void cargarAjustes() {
        db.collection("ajustes").document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    // Si no hay documento, aplicamos defaults
                    if (doc == null || !doc.exists()) {
                        pintarAvatar(avatarSeleccionado);
                        rbC.setChecked(true);
                        return;
                    }

                    // Lectura de campos desde Firestore
                    String nombre = doc.getString("nombreUsuario");
                    String unidad = doc.getString("unidadTemp"); // "C" o "F"
                    Boolean viento = doc.getBoolean("mostrarViento");
                    Boolean cielo = doc.getBoolean("mostrarCielo");
                    Boolean lluvia = doc.getBoolean("mostrarLluvia");
                    String avatar = doc.getString("avatar");

                    // Pinta nombre si existe
                    if (nombre != null) etNombreUsuario.setText(nombre);

                    // Unidad de temperatura
                    if ("F".equalsIgnoreCase(unidad)) {
                        unidadTemp = "F";
                        rbF.setChecked(true);
                    } else {
                        unidadTemp = "C";
                        rbC.setChecked(true);
                    }

                    // Checkboxes
                    cbViento.setChecked(viento != null && viento);
                    cbCielo.setChecked(cielo != null && cielo);
                    cbLluvia.setChecked(lluvia != null && lluvia);

                    // Avatar
                    if (avatar != null && !avatar.trim().isEmpty()) {
                        avatarSeleccionado = avatar.trim();
                    }

                    // Pinta avatar final
                    pintarAvatar(avatarSeleccionado);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error cargando ajustes: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    /**
     * Método para guardar los ajustes actuales en Firestore.
     */
    private void guardarAjustes() {

        // Lee el nombre en el caso de estar vacío pone Usuario
        String nombre = etNombreUsuario.getText().toString().trim();
        if (nombre.isEmpty()) nombre = "Usuario";

        // Prepara mapa de datos para Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("nombreUsuario", nombre);
        data.put("unidadTemp", unidadTemp);
        data.put("mostrarViento", cbViento.isChecked());
        data.put("mostrarCielo", cbCielo.isChecked());
        data.put("mostrarLluvia", cbLluvia.isChecked());
        data.put("avatar", avatarSeleccionado);

        // Guarda los ajustes
        db.collection("ajustes").document(uid)
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error guardando ajustes: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }


    /**
     * Método pra abrir un diálogo con un RecyclerView y elegir la imagen
     */
    private void abrirSelectorAvatar() {

        // Infla el layout del diálogo
        View v = getLayoutInflater().inflate(R.layout.dialog_avatar_picker, null);

        // RecyclerView del diálogo
        RecyclerView rv = v.findViewById(R.id.rvAvatares);

        // Lista de nombres de drawables disponibles
        List<String> avatares = new ArrayList<>();
        avatares.add("avatar1");
        avatares.add("avatar2");
        avatares.add("avatar3");
        avatares.add("avatar4");
        avatares.add("avatar5");
        avatares.add("avatar6");
        avatares.add("avatar7");

        // Layout
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        // Crea el diálogo con el RecyclerView dentro
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Elige una imagen")
                .setView(v)
                .setNegativeButton("Cancelar", null)
                .create();

        // Adapter: recibe la lista y una lambda para el click
        AvatarAdapter adapter = new AvatarAdapter(avatares, avatarKey -> {

            // Guarda selección
            avatarSeleccionado = avatarKey;

            // Pinta avatar elegido
            pintarAvatar(avatarSeleccionado);

            // Cierra el diálogo
            dialog.dismiss();
        });

        // Asigna el adapter al RecyclerView
        rv.setAdapter(adapter);

        // Muestra el diálogo
        dialog.show();
    }

    /**
     * Método para mostrar en la previsualización el avatar indicado.
     * @param avatarKey nombre del drawable sin extensión (ej: "avatar3")
     */
    private void pintarAvatar(@NonNull String avatarKey) {

        // Busca el id del recurso drawable a partir del nombre
        int resId = getResources().getIdentifier(avatarKey, "drawable", getPackageName());

        // Si existe lo pinta, en caso contrario elegimos uno predeterminado
        if (resId != 0) {
            imgAvatarPreview.setImageResource(resId);
        } else {
            imgAvatarPreview.setImageResource(R.drawable.avatar1);
            avatarSeleccionado = "avatar1";
        }
    }
}