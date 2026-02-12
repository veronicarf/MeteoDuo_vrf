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

public class AjustesActivity extends AppCompatActivity {

    // UI
    private EditText etNombreUsuario;
    private RadioGroup rgTemp;
    private RadioButton rbC, rbF;
    private CheckBox cbViento, cbCielo, cbLluvia;

    private ImageView imgAvatarPreview;
    private Button btnCambiarImagen, btnGuardar, btnSalir;

    // Firebase
    private FirebaseFirestore db;
    private String uid;

    // Estado
    private String avatarSeleccionado = "avatar1";  // nombre del drawable (sin extensión)
    private String unidadTemp = "C";                // "C" o "F"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        // Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay sesión iniciada", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        uid = user.getUid();

        // Bind UI
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        rgTemp = findViewById(R.id.rgTemp);
        rbC = findViewById(R.id.rbC);
        rbF = findViewById(R.id.rbF);

        cbViento = findViewById(R.id.cbViento);
        cbCielo = findViewById(R.id.cbCielo);
        cbLluvia = findViewById(R.id.cbLluvia);

        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);
        btnCambiarImagen = findViewById(R.id.btnCambiarImagen);

        btnGuardar = findViewById(R.id.btnGuardarAjustes);
        btnSalir = findViewById(R.id.btnSalirAjustes);

        // Listeners
        btnCambiarImagen.setOnClickListener(v -> abrirSelectorAvatar());

        rgTemp.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbF) unidadTemp = "F";
            else unidadTemp = "C";
        });

        btnGuardar.setOnClickListener(v -> guardarAjustes());
        btnSalir.setOnClickListener(v -> finish());

        // Cargar datos
        cargarAjustes();
    }

    private void cargarAjustes() {
        db.collection("ajustes").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        // defaults
                        pintarAvatar(avatarSeleccionado);
                        rbC.setChecked(true);
                        return;
                    }

                    String nombre = doc.getString("nombreUsuario");
                    String unidad = doc.getString("unidadTemp"); // "C" o "F"
                    Boolean viento = doc.getBoolean("mostrarViento");
                    Boolean cielo = doc.getBoolean("mostrarCielo");
                    Boolean lluvia = doc.getBoolean("mostrarLluvia");
                    String avatar = doc.getString("avatar");

                    if (nombre != null) etNombreUsuario.setText(nombre);

                    if ("F".equalsIgnoreCase(unidad)) {
                        unidadTemp = "F";
                        rbF.setChecked(true);
                    } else {
                        unidadTemp = "C";
                        rbC.setChecked(true);
                    }

                    cbViento.setChecked(viento != null && viento);
                    cbCielo.setChecked(cielo != null && cielo);
                    cbLluvia.setChecked(lluvia != null && lluvia);

                    if (avatar != null && !avatar.trim().isEmpty()) {
                        avatarSeleccionado = avatar.trim();
                    }
                    pintarAvatar(avatarSeleccionado);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error cargando ajustes: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void guardarAjustes() {
        String nombre = etNombreUsuario.getText().toString().trim();
        if (nombre.isEmpty()) nombre = "Usuario";

        Map<String, Object> data = new HashMap<>();
        data.put("nombreUsuario", nombre);
        data.put("unidadTemp", unidadTemp);

        data.put("mostrarViento", cbViento.isChecked());
        data.put("mostrarCielo", cbCielo.isChecked());
        data.put("mostrarLluvia", cbLluvia.isChecked());

        data.put("avatar", avatarSeleccionado);

        db.collection("ajustes").document(uid)
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error guardando ajustes: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // ---------------------------
    // Selector de avatar (Dialog)
    // ---------------------------

    private void abrirSelectorAvatar() {
        View v = getLayoutInflater().inflate(R.layout.dialog_avatar_picker, null);
        RecyclerView rv = v.findViewById(R.id.rvAvatares);

        // Lista de drawables disponibles
        List<String> avatares = new ArrayList<>();
        avatares.add("avatar1");
        avatares.add("avatar2");
        avatares.add("avatar3");
        avatares.add("avatar4");
        avatares.add("avatar5");
        avatares.add("avatar6");
        avatares.add("avatar7");

        rv.setLayoutManager(new GridLayoutManager(this, 3));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Elige una imagen")
                .setView(v)
                .setNegativeButton("Cancelar", null)
                .create();

        AvatarAdapter adapter = new AvatarAdapter(avatares, avatarKey -> {
            avatarSeleccionado = avatarKey;
            pintarAvatar(avatarSeleccionado);
            dialog.dismiss();
        });

        rv.setAdapter(adapter);

        dialog.show();
    }

    private void pintarAvatar(@NonNull String avatarKey) {
        int resId = getResources().getIdentifier(avatarKey, "drawable", getPackageName());
        if (resId != 0) {
            imgAvatarPreview.setImageResource(resId);
        } else {
            // si no existe, pongo uno por defecto
            imgAvatarPreview.setImageResource(R.drawable.avatar1);
            avatarSeleccionado = "avatar1";
        }
    }
}
