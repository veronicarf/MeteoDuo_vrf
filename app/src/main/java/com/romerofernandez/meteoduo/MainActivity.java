package com.romerofernandez.meteoduo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainActivity
 * Activity principal de arranque de la aplicación.
 *
 * @author Verónica
 * */
public class MainActivity extends AppCompatActivity {

    /**
     * Método que se ejecuta al crear la Activity.
     * Configura el modo Edge-to-Edge y aplica los márgenes adecuados
     * para evitar que el contenido quede oculto por las barras del sistema.
     *
     * @param savedInstanceState Estado previamente guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se habilita el modo Edge-to-Edge para ocupar toda la pantalla
        EdgeToEdge.enable(this);

        // Se asocia la Activity con su layout XML
        setContentView(R.layout.activity_main);

        // Se ajusta los márgenes dinámicamente según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}