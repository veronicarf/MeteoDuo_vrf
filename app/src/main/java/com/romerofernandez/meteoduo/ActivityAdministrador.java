package com.romerofernandez.meteoduo;

import static com.romerofernandez.meteoduo.R.id.btnApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityAdministrador  extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        Button btnConsultar = findViewById(R.id.btnConsultar);
        Button btnHistorial = findViewById(R.id.btnHistorial);
        Button btnApp = findViewById(R.id.btnApp);


        // Accedemos a ajustes

        // Accedemos a ajustes
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, AjustesActivity.class);
            startActivity(intent);
        });

        // Accedemos a ajustes

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, HistorialActivity.class);
            startActivity(intent);
        });

        // Accedemos a comparativa

        btnConsultar.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, ComparativaActivity.class);
            startActivity(intent);
        });

        // Accedemos al historial

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, HistorialActivity.class);
            startActivity(intent);
        });
        // Accedemos al apartado del admisnitrador

        btnApp.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, AppActivity.class);
            startActivity(intent);
        });



    }


}
