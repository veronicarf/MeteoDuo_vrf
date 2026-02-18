package com.romerofernandez.meteoduo;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

/**
 * Activity encargada del registro de nuevos usuarios en la aplicación.
 *
 * @author Verónica
 */
public class RegisterActivity extends AppCompatActivity {

// ==============================
// COMPONENTES DE INTERFAZ
// ==============================

    /** Campo de texto para introducir el correo electrónico del usuario. */
    private EditText etEmail;

    /** Campo de texto para introducir la contraseña. */
    private EditText etPass1;

    /** Campo de texto para confirmar la contraseña introducida. */
    private EditText etPass2;


// ==============================
// SERVICIOS FIREBASE
// ==============================

    /** Instancia de FirebaseAuth  */
    private FirebaseAuth auth;

    /** Instancia de FirebaseFirestore */
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
        setContentView(R.layout.activity_register);

        inicializarFirebase();
        enlazarVistas();
        configurarEventos();
    }

    /** Inicializa los servicios de Firebase usados en el registro. */
    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /** Enlaza los campos de entrada del formulario de registro. */
    private void enlazarVistas() {
        etEmail = findViewById(R.id.etEmailReg);
        etPass1 = findViewById(R.id.etPasswordReg);
        etPass2 = findViewById(R.id.etPasswordReg2);
    }

    /** Configura los listeners de los botones de la pantalla de registro. */
    private void configurarEventos() {

        findViewById(R.id.tvVolverLogin)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btnCrearCuenta)
                .setOnClickListener(v -> crearCuenta());
    }


    /**
     * Método para crear una nueva cuenta de usuario utilizando Firebase Authentication.
     */


    private void crearCuenta() {

        String email = etEmail.getText().toString().trim();
        String p1 = etPass1.getText().toString().trim();
        String p2 = etPass2.getText().toString().trim();
        boolean tieneLetra = p1.matches(".*[A-Za-z].*");
        boolean tieneNumero = p1.matches(".*\\d.*");
        // Validar email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar contraseña: mínimo 6
        if (TextUtils.isEmpty(p1) || p1.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar contraseña: al menos una letra y un número


        if (!tieneLetra || !tieneNumero) {
            Toast.makeText(this, "La contraseña debe contener al menos una letra y un número", Toast.LENGTH_SHORT).show();
            return;
        }


        // Confirmar que coinciden
        if (!p1.equals(p2)) {
            Toast.makeText(this, getString(R.string.error_password_repeat), Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, p1)
                .addOnSuccessListener(result -> {

                    String uid = (result.getUser() != null) ? result.getUser().getUid() : null;

                    if (uid == null) {
                        Toast.makeText(this, "Error creando usuario", Toast.LENGTH_LONG).show();
                        return;
                    }

                    DocumentReference ref = db.collection("usuarios").document(uid);

                    ref.get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("correoElectronico", email);
                                    data.put("rol", "usuario");
                                    data.put("activo", true);

                                    ref.set(data)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(this, getString(R.string.register_ok), Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Error Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                            );

                                } else {
                                    Toast.makeText(this, getString(R.string.register_ok), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error leyendo Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .addOnFailureListener(e -> {

                    if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
                        String code = ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode();

                        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                            Toast.makeText(this, "Ese usuario ya existe. Inicia sesión o usa otro email.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    Toast.makeText(this,
                            "No se ha podido completar la operación. Inténtalo de nuevo.",
                            Toast.LENGTH_LONG).show();
                });

    }

    /*private void crearCuenta() {

        String email = etEmail.getText().toString().trim();
        String p1 = etPass1.getText().toString().trim();
        String p2 = etPass2.getText().toString().trim();

        // Se valida el email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }

          // Aqui pasamos a validar la contraseña
       /* if (TextUtils.isEmpty(p1) || p1.length() < 6) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(p1) || p1.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

           // Validar que tenga al menos una letra y un número
        if (!p1.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            Toast.makeText(this, "Debe contener al menos una letra y un número", Toast.LENGTH_SHORT).show();
            return;
        }


        // Se comprueba que coincidan las contraseñas
        if (!p1.equals(p2)) {
            Toast.makeText(this, getString(R.string.error_password_repeat), Toast.LENGTH_SHORT).show();
            return;
        }

        // Una vez realizada las comprobaciones se crea el usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, p1)
                .addOnSuccessListener(result -> {

                    String uid = (result.getUser() != null)
                            ? result.getUser().getUid()
                            : null;

                    if (uid == null) {
                        Toast.makeText(this, "Error creando usuario", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Se hace referencia al documento del usuario en Firestore
                    DocumentReference ref = db.collection("usuarios").document(uid);

                    // Y se crea el documento  si no existe
                    ref.get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("correoElectronico", email);
                                    data.put("rol", "usuario");
                                    data.put("activo", true);

                                    ref.set(data)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(this,
                                                        getString(R.string.register_ok),
                                                        Toast.LENGTH_SHORT).show();
                                                finish(); // vuelve al login
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this,
                                                            "Error Firestore: " + e.getMessage(),
                                                            Toast.LENGTH_LONG).show()
                                            );
                                } else {
                                    // Si el documento ya existe, no se modifica
                                    Toast.makeText(this,
                                            getString(R.string.register_ok),
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Error leyendo Firestore: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "No se ha podido completar la operación. Inténtalo de nuevo.",
                                Toast.LENGTH_LONG).show()
                );
    }*/
}
