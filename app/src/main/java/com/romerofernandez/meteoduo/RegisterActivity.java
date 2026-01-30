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

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPass1, etPass2;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmailReg);
        etPass1 = findViewById(R.id.etPasswordReg);
        etPass2 = findViewById(R.id.etPasswordReg2);

        Button btnCrear = findViewById(R.id.btnCrearCuenta);
        TextView tvVolver = findViewById(R.id.tvVolverLogin);

        tvVolver.setOnClickListener(v -> finish());
        btnCrear.setOnClickListener(v -> crearCuenta());
    }

    private void crearCuenta() {
        String email = etEmail.getText().toString().trim();
        String p1 = etPass1.getText().toString().trim();
        String p2 = etPass2.getText().toString().trim();

        // Validación mínima
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(p1) || p1.length() < 6) {
            Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!p1.equals(p2)) {
            Toast.makeText(this, getString(R.string.error_password_repeat), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, p1)
                .addOnSuccessListener(result -> {
                    String uid = (result.getUser() != null) ? result.getUser().getUid() : null;
                    if (uid == null) {
                        Toast.makeText(this, "Error creando usuario", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ OPCIÓN 1: SOLO CREAR DOCUMENTO SI NO EXISTE
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
                                                finish(); // vuelve al login
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Error Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                            );
                                } else {
                                    // Ya existe: NO TOCAMOS el rol ni nada
                                    Toast.makeText(this, getString(R.string.register_ok), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error leyendo Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se ha podido completar la operación. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                );
    }
}
