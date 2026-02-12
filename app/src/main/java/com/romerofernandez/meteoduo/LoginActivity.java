package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);

        btnLogin.setOnClickListener(v -> iniciarSesion());

        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // Validación
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || TextUtils.isEmpty(pass) || pass.length() < 6) {

            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();

            // Log de intento fallido por validación local
            RegistroConexiones.error(email, "LOGIN", "Validación local: email/contraseña inválidos");
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        Toast.makeText(this, "Error inesperado al iniciar sesión.", Toast.LENGTH_LONG).show();
                        RegistroConexiones.error(email, "LOGIN", "Login correcto pero user=null (FirebaseUser)");
                        return;
                    }

                    cargarRolYRedirigir(user);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_LONG).show();
                    String msg = (e.getMessage() != null) ? e.getMessage() : "Error desconocido FirebaseAuth";
                    RegistroConexiones.error(email, "LOGIN", msg);
                });
    }

    private void cargarRolYRedirigir(FirebaseUser user) {

        final String uid = user.getUid();
        final String email = (user.getEmail() != null) ? user.getEmail() : "desconocido";

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {

                    // Si no existe doc -> usuario normal
                    if (!doc.exists()) {
                        RegistroConexiones.ok(email, "LOGIN");
                        startActivity(new Intent(this, ActivityUsuario.class));
                        finish();
                        return;
                    }

                    // Bloqueado
                    Boolean activo = doc.getBoolean("activo");
                    if (activo != null && !activo) {
                        Toast.makeText(this, "Usuario bloqueado.", Toast.LENGTH_LONG).show();
                        RegistroConexiones.error(email, "LOGIN", "Usuario bloqueado (activo=false)");
                        auth.signOut();
                        return;
                    }

                    // Rol
                    String rol = doc.getString("rol");
                    rol = (rol == null) ? "" : rol.trim();

                    RegistroConexiones.ok(email, "LOGIN");

                    if ("administrador".equalsIgnoreCase(rol)) {
                        startActivity(new Intent(this, ActivityAdministrador.class));
                    } else {
                        startActivity(new Intent(this, ActivityUsuario.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "No se pudo comprobar el rol. Revisa conexión o permisos.",
                            Toast.LENGTH_LONG).show();

                    // Log si falla Firestore al comprobar rol
                    String msg = (e.getMessage() != null) ? e.getMessage() : "Error Firestore comprobando rol";
                    RegistroConexiones.error(email, "FIRESTORE", msg);
                    auth.signOut();
                });
    }
}


