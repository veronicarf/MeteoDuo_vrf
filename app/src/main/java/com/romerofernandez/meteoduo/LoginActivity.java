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

/**
 * LoginActivity
 *
 * Activity encargada de gestionar el inicio de sesión de usuarios mediante Firebase Authentication.
 * Tras autenticarse correctamente, consulta en Firestore el documento del usuario (colección "usuarios")
 * para comprobar:
 * - si existe el documento
 * - si el usuario está activo
 * - el rol del usuario (administrador o usuario)
 *
 * En función del rol, redirige a ActivityAdministrador o ActivityUsuario.
 *
 * @author Verónica
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Campo de texto para introducir el correo del usuario.
     */
    private EditText etEmail;

    /**
     * Campo de texto para introducir la contraseña del usuario.
     */
    private EditText etPassword;

    /**
     * Instancia de FirebaseAuth para autenticar usuarios.
     */
    private FirebaseAuth auth;

    /**
     * Instancia de FirebaseFirestore para consultar datos del usuario
     */
    private FirebaseFirestore db;

    /**
     * Método que se ejecuta al crear la Activity.
     * Inicializa Firebase, enlaza los componentes del layout y configura los eventos de la interfaz.
     *
     * @param savedInstanceState Estado previamente guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Muestra el ProjectId real del proyecto Firebase
        String projectId = com.google.firebase.FirebaseApp.getInstance()
                .getOptions().getProjectId();
        Toast.makeText(this, "projectId=" + projectId, Toast.LENGTH_LONG).show();

        // Inicialización de Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Se enlaza los componentes del layout
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);

        // Evento para  iniciar sesión
        btnLogin.setOnClickListener(v -> iniciarSesion());

        // Evento para ir a la pantalla de registro
        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    /**
     * Método para iniciar sesión con FirebaseAuth usando email y contraseña.
     * Se realiza comprobaciones de usuario y contraseña.
     * Una vez Verificada, según el rol, redirige a la ventana de usuario o administrador
     *
     */
    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        //Se validan las ceredenciales
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || TextUtils.isEmpty(pass) || pass.length() < 6) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }

        // Autentificación con Firebase
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();

                    // Se comprueba que el usuario exista
                    if (user == null) {
                        Toast.makeText(this,
                                "Error inesperado al iniciar sesión.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

        //Se consulta el Rol
                    cargarRolYRedirigir(user.getUid());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.error_login),
                                Toast.LENGTH_LONG).show()
                );
    }


    /**
     * Obtiene el documento del usuario en Firestore para comprobar:
     * - si el documento existe
     * - si está activo
     * - el rol del usuario
     *
     * Según el rol redirige a ActivityAdministrador  o ActivityUsuario
     *
     * @param uid UID del usuario autenticado en FirebaseAuth
     */
    private void cargarRolYRedirigir(String uid) {

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {

                    // Si no existe el documento en Firestore, se entra como usuario normal

                    if (!doc.exists()) {
                        startActivity(new Intent(this, ActivityUsuario.class));
                        finish();
                        return;
                    }

                    // Se comprueba si el usuario está bloqueado
                    Boolean activo = doc.getBoolean("activo");
                    if (activo != null && !activo) {
                        Toast.makeText(this,
                                "Usuario bloqueado.",
                                Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    // Se obtiene el rol
                    String rol = doc.getString("rol");
                    rol = (rol == null) ? "" : rol.trim();

                    // Se redirige al usuario a la página principal según su Rol
                    if ("administrador".equalsIgnoreCase(rol)) {
                        startActivity(new Intent(this, ActivityAdministrador.class));
                    } else {
                        startActivity(new Intent(this, ActivityUsuario.class));
                    }

                    // Se cierra LoginActivity
                    finish();
                });
                 /*.addOnFailureListener(e -> {
                     Toast.makeText(this,
                    "No se pudo comprobar el rol. Revisa conexión o permisos.",
                    Toast.LENGTH_LONG).show();
                });*/
    }

}


