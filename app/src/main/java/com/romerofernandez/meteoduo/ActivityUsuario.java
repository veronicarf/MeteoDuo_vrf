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
 * ActivityUsuario
 *
 * Actividad principal destinada a los usuarios estándar de la aplicación.
 *
 * @author Verónica
 */
public class ActivityUsuario extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvNombreUsuario;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(ActivityUsuario.this, AjustesActivity.class))
        );

        Button btnConsultar = findViewById(R.id.btnConsultar);
        btnConsultar.setOnClickListener(v ->
                startActivity(new Intent(ActivityUsuario.this, ComparativaActivity.class))
        );

        Button btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(ActivityUsuario.this, HistorialActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarAjustesNube();
    }

    private void cargarAjustesNube() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("ajustes").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) return;

                    String nombre = doc.getString("nombreUsuario");
                    String avatar = doc.getString("avatar");

                    if (nombre != null && !nombre.trim().isEmpty()) {
                        tvNombreUsuario.setText(nombre);
                    }

                    if (avatar != null && !avatar.trim().isEmpty()) {
                        imgAvatar.setImageResource(drawableFromName(avatar.trim()));
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "No se pudieron cargar ajustes", Toast.LENGTH_SHORT).show();
                });
    }

    private int drawableFromName(String name) {
        if (name == null || name.trim().isEmpty()) return R.drawable.avatar_default;
        int resId = getResources().getIdentifier(name.trim(), "drawable", getPackageName());
        return (resId != 0) ? resId : R.drawable.avatar_default;
    }
}
