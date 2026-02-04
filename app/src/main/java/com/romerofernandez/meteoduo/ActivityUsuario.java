package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ActivityUsuario
 *
 * Actividad principal destinada a los usuarios estándar de la aplicación.
 *
 * @author Verónica
 */
public class ActivityUsuario extends AppCompatActivity {

    /**
     * Método que se ejecuta al crear la actividad.
     * Inicializa la interfaz y asigna los eventos a los botones disponibles
     * para el usuario.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se asocia la actividad con su layout XML
        setContentView(R.layout.activity_usuario);

        // Botón para acceder a la pantalla de ajustes
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityUsuario.this, AjustesActivity.class);
            startActivity(intent);
        });

        // Botón para acceder a la pantalla de comparativas
        Button btnConsultar = findViewById(R.id.btnConsultar);
        btnConsultar.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityUsuario.this, ComparativaActivity.class);
            startActivity(intent);
        });

        // Botón para acceder al historial
        Button btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityUsuario.this, HistorialActivity.class);
            startActivity(intent);
        });
    }
}
