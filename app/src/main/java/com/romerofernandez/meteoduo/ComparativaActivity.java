package com.romerofernandez.meteoduo;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class ComparativaActivity extends AppCompatActivity {


        private FirebaseFirestore db;

    private AutoCompleteTextView spProvA, spMunA, spProvB, spMunB;


    private final List<String> provinciaIds = new ArrayList<>();
        private final List<String> provinciaNombres = new ArrayList<>();

        private ArrayAdapter<String> adapterProvA;
        private ArrayAdapter<String> adapterProvB;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_comparativa); // <-- pon aquí tu layout real

            db = FirebaseFirestore.getInstance();

            spProvA = findViewById(R.id.autoCompleteTextView);
            spMunA  = findViewById(R.id.autoCompleteTextView2);
            spProvB = findViewById(R.id.autoCompleteTextView3);
            spMunB  = findViewById(R.id.autoCompleteTextView4);

            adapterProvA = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinciaNombres);
            adapterProvA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spProvA.setAdapter(adapterProvA);

            adapterProvB = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinciaNombres);
            adapterProvB.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spProvB.setAdapter(adapterProvB);

            cargarProvincias();
        }

        private void cargarProvincias() {
            provinciaIds.clear();
            provinciaNombres.clear();

            db.collection("provincias")
                    .orderBy("nombre")
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            provinciaIds.add(doc.getId());
                            provinciaNombres.add(doc.getString("nombre"));
                        }
                        adapterProvA.notifyDataSetChanged();
                        adapterProvB.notifyDataSetChanged();

                        Toast.makeText(this, "Provincias cargadas: " + provinciaNombres.size(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error cargando provincias: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }

