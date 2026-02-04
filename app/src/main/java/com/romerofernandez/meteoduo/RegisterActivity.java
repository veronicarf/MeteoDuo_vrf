package com.romerofernandez.meteoduo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

/**
 * RegisterActivity
 *
 * Activity encargada del registro de nuevos usuarios en la aplicación.
 * Utiliza Firebase Authentication para crear la cuenta y Firebase Firestore
 * para almacenar los datos adicionales del usuario (correo, rol y estado).
 *
 * @author Verónica
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Campo de texto para introducir el correo electrónico.
     */
    private EditText etEmail;

    /**
     * Campo de texto para introducir la contraseña.
     */
    private EditText etPass1;

    /**
     * Campo de texto para repetir la contraseña.
     */
    private EditText etPass2;

    /**
     * Instancia de FirebaseAuth para crear cuentas de usuario.
     */
    private FirebaseAuth auth;

    /**
     * Instancia de FirebaseFirestore para guardar los datos del usuario.
     */
    private FirebaseFirestore db;

    /**
     * Método que se ejecuta al crear la Activity.
     * Inicializa Firebase, enlaza los componentes del layout
     * y configura los eventos de los botones.
     *
     * @param savedInstanceState Estado previamente guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Se inicializa de Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Se enlazan los componentes del layout
        etEmail = findViewById(R.id.etEmailReg);
        etPass1 = findViewById(R.id.etPasswordReg);
        etPass2 = findViewById(R.id.etPasswordReg2);

        Button btnCrear = findViewById(R.id.btnCrearCuenta);
        TextView tvVolver = findViewById(R.id.tvVolverLogin);

        // Para volver a la pantalla de login
        tvVolver.setOnClickListener(v -> finish());

        // Para crear una nueva cuenta
        btnCrear.setOnClickListener(v -> crearCuenta());
    }

    /**
     * Método para crear una nueva cuenta de usuario utilizando Firebase Authentication.
     */
    private void crearCuenta() {

        String email = etEmail.getText().toString().trim();
        String p1 = etPass1.getText().toString().trim();
        String p2 = etPass2.getText().toString().trim();

        // Se valida el email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }

        // Aqui pasamos a validar la contraseña
        if (TextUtils.isEmpty(p1) || p1.length() < 6) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
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
    }
}
