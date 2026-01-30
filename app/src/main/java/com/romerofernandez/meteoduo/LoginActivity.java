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

    private EditText etEmail, etPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //PROYECTO FIREBASE REAL
        String projectId = com.google.firebase.FirebaseApp.getInstance()
                .getOptions().getProjectId();
        Toast.makeText(this, "projectId=" + projectId, Toast.LENGTH_LONG).show();

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

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || TextUtils.isEmpty(pass) || pass.length() < 6) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        Toast.makeText(this,
                                "Error inesperado al iniciar sesión.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                  
                    cargarRolYRedirigir(user.getUid());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.error_login),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void cargarRolYRedirigir(String uid) {

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        startActivity(new Intent(this, ActivityUsuario.class));
                        finish();
                        return;
                    }

                    Boolean activo = doc.getBoolean("activo");
                    if (activo != null && !activo) {
                        Toast.makeText(this,
                                "Usuario bloqueado.",
                                Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    String rol = doc.getString("rol");
                    rol = (rol == null) ? "" : rol.trim();

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
                });
    }

}


