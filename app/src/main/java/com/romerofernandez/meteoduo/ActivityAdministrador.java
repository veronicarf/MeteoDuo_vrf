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
 * ActivityAdministrador
 *
 * Actividad principal destinada a los usuarios con rol de administrador.
 * Desde esta pantalla se puede acceder a las diferentes funcionalidades.
 *
 * @author Verónica
 */
public class ActivityAdministrador extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvNombreUsuario;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador);

        // UI (si no existen estos IDs en el XML, cambia los IDs aquí)
        imgAvatar = findViewById(R.id.imgAvatar);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);

        // Firebase (esto evita el NullPointerException)
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Botones
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        Button btnConsultar = findViewById(R.id.btnConsultar);
        Button btnHistorial = findViewById(R.id.btnHistorial);
        Button btnApp = findViewById(R.id.btnApp);

        // Ajustes
        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, AjustesActivity.class))
        );

        // Historial
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, HistorialActivity.class))
        );

        // Comparativa
        btnConsultar.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, ComparativaActivity.class))
        );

        // Ventana única admin
        btnApp.setOnClickListener(v ->
                startActivity(new Intent(ActivityAdministrador.this, AppActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarAjustesNube();
    }

    private void cargarAjustesNube() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Si quieres, aquí puedes redirigir al login
            // startActivity(new Intent(this, LoginActivity.class));
            // finish();
            return;
        }

        String uid = user.getUid();

        db.collection("ajustes").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) return;

                    String nombre = doc.getString("nombreUsuario");
                    String avatar = doc.getString("avatar");

                    if (tvNombreUsuario != null && nombre != null && !nombre.trim().isEmpty()) {
                        tvNombreUsuario.setText(nombre.trim());
                    }

                    if (imgAvatar != null && avatar != null && !avatar.trim().isEmpty()) {
                        imgAvatar.setImageResource(drawableFromName(avatar.trim()));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudieron cargar ajustes", Toast.LENGTH_SHORT).show()
                );
    }

    private int drawableFromName(String name) {
        if (name == null || name.trim().isEmpty()) return R.drawable.avatar_default;
        int resId = getResources().getIdentifier(name.trim(), "drawable", getPackageName());
        return (resId != 0) ? resId : R.drawable.avatar_default;
    }
}
