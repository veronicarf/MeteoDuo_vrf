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

/**
 * ActivityAdministrador
 *
 * Actividad principal destinada a los usuarios con rol de administrador.
 * Desde esta pantalla se puede acceder a las diferentes funcionalidades
 *
 * @author Verónica
 */
public class ActivityAdministrador extends AppCompatActivity {

    /**
     * Método que se ejecuta al crear la actividad.
     * Inicializa la interfaz y asigna los eventos a los botones.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asocia la actividad con su layout XML
        setContentView(R.layout.activity_administrador);

        // Referencias a los elementos de la interfaz
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        Button btnConsultar = findViewById(R.id.btnConsultar);
        Button btnHistorial = findViewById(R.id.btnHistorial);
        Button btnApp = findViewById(R.id.btnApp);

        /**
         * Se accede a la pantalla de ajustes de la aplicación
         */
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, AjustesActivity.class);
            startActivity(intent);
        });

        /**
         * Se accede al historial de consultas o acciones realizadas
         */
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, HistorialActivity.class);
            startActivity(intent);
        });

        /**
         * Se accede a la pantalla de comparativas
         */
        btnConsultar.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, ComparativaActivity.class);
            startActivity(intent);
        });

        /**
         * Accede a la vetana única para el admisnitrador
         */
        btnApp.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdministrador.this, AppActivity.class);
            startActivity(intent);
        });
    }
}
