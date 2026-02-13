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
 * <p>
 * Activity encargada de autenticar al usuario mediante Firebase Authentication.
 * Tras un login correcto, consulta en Firestore el documento del usuario para:
 * <ul>
 *   <li>Comprobar si está bloqueado (campo {@code activo}).</li>
 *   <li>Leer su rol (campo {@code rol}) y redirigir a la pantalla correspondiente.</li>
 * </ul>
 * Además registra intentos correctos y fallidos mediante {@code RegistroConexiones}.
 */
public class LoginActivity extends AppCompatActivity {

  //Variables UI
    private EditText etEmail;
    private EditText etPassword;

    // Variables) Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;


    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        inicializarFirebase();
        enlazarVistas();
        configurarEventos();
    }

    /** Inicializa los servicios de Firebase usados en el login. */
    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /** Enlaza los componentes de la interfaz con las variables Java. */
    private void enlazarVistas() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    /** Configura los listeners de los botones y enlaces de la pantalla. */
    private void configurarEventos() {

        findViewById(R.id.btnLogin)
                .setOnClickListener(v -> iniciarSesion());

        findViewById(R.id.tvIrRegistro)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, RegisterActivity.class))
                );
    }

    /**
     * Método que  los datos de entrada e intenta iniciar sesión con FirebaseAuth.
     */
    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // Validación  email y contraseña mínima de 6 caracteres
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || TextUtils.isEmpty(pass) || pass.length() < 6) {

            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();

            // Log de intento fallido por validación local
            RegistroConexiones.error(email, "LOGIN", "Validación local: email/contraseña inválidos");
            return;
        }

        // Autenticación con Firebase
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();

                    //  Usurio erroneo y login correcto
                    if (user == null) {
                        Toast.makeText(this, "Error inesperado al iniciar sesión.", Toast.LENGTH_LONG).show();
                        RegistroConexiones.error(email, "LOGIN", "Login correcto pero user=null (FirebaseUser)");
                        return;
                    }

                    // Carga el rol
                    cargarRolYRedirigir(user);

                })
                .addOnFailureListener(e -> {
                    // Login fallido
                    Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_LONG).show();

                    // Registra el error devuelto por FirebaseAuth
                    String msg = (e.getMessage() != null) ? e.getMessage() : "Error desconocido FirebaseAuth";
                    RegistroConexiones.error(email, "LOGIN", msg);
                });
    }



    /**
     * Método que consulta en Firestore el documento del usuario para obtener estado y el rol
     *
     * @param user usuario autenticado por FirebaseAuth
     */
    private void cargarRolYRedirigir(FirebaseUser user) {

        final String uid = user.getUid();
        final String email = (user.getEmail() != null) ? user.getEmail() : "desconocido";

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {

                    // Si no existe el documento, se trata como usuario normal
                    if (!doc.exists()) {
                        RegistroConexiones.ok(email, "LOGIN");
                        startActivity(new Intent(this, ActivityUsuario.class));
                        finish();
                        return;
                    }

                    // Si está bloqueado  se informa y se cierra sesión
                    Boolean activo = doc.getBoolean("activo");
                    if (activo != null && !activo) {
                        Toast.makeText(this, "Usuario bloqueado.", Toast.LENGTH_LONG).show();
                        RegistroConexiones.error(email, "LOGIN", "Usuario bloqueado (activo=false)");
                        auth.signOut();
                        return;
                    }

                    // Obtiene el rol del usuario
                    String rol = doc.getString("rol");
                    rol = (rol == null) ? "" : rol.trim();

                    // Login correcto registrado
                    RegistroConexiones.ok(email, "LOGIN");

                    // Redirección según rol
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
                    String msg = (e.getMessage() != null) ? e.getMessage() : "Error Firestore comprobando rol";
                    RegistroConexiones.error(email, "FIRESTORE", msg);

                    // Cierra sesión
                    auth.signOut();
                });
    }
}

