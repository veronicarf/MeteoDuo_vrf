package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityUsuario  extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        // Accedemos a ajustes
        ImageButton btnMenu = findViewById(R.id.btnMenu);

        // Accedemos a ajustes
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityUsuario.this, AjustesActivity.class);
            startActivity(intent);
        });

        // Accedemos a ajustes
        Button btnHistorial = findViewById(R.id.btnHistorial);

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityUsuario.this, HistorialActivity.class);
            startActivity(intent);
        });





    }


}