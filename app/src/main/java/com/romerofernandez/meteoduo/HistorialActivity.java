package com.romerofernandez.meteoduo;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HistorialActivity extends AppCompatActivity {

    private ListView listHistorial;
    private Button btnVolver;

    private final List<JSONObject> itemsJson = new ArrayList<>();
    private final List<String> itemsTexto = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user != null) ? user.getUid() : "guest";
        listHistorial = findViewById(R.id.listHistorial);
        btnVolver = findViewById(R.id.btnVolverHistorial);

        adapter = new ArrayAdapter<>(this, R.layout.item_historial, R.id.tvItemHistorial, itemsTexto);
        listHistorial.setAdapter(adapter);

        cargarHistorial();
        listHistorial.setOnItemClickListener((parent, view, position, id) -> {
            JSONObject obj = itemsJson.get(position);

            Intent i = new Intent(HistorialActivity.this, ResultadosActivity.class);
            i.putExtra("puntoA", obj.optString("puntoA"));
            i.putExtra("puntoB", obj.optString("puntoB"));
            i.putExtra("fechaInicio", obj.optString("fecha"));
            i.putExtra("jsonA", obj.optString("jsonA"));
            i.putExtra("jsonB", obj.optString("jsonB"));
            startActivity(i);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarHistorial() {
        itemsJson.clear();
        itemsTexto.clear();

        List<JSONObject> list = HistorialStorage.getAll(this, uid);

        if (list.isEmpty()) {
            itemsTexto.add("No hay consultas guardadas todavía.");
            adapter.notifyDataSetChanged();
            return;
        }

        itemsJson.addAll(list);

        for (int i = 0; i < list.size(); i++) {
            JSONObject obj = list.get(i);

            String puntoA = obj.optString("puntoA", "A");
            String puntoB = obj.optString("puntoB", "B");
            String fecha  = obj.optString("fecha", "");

            itemsTexto.add(
                    (i + 1) + ". " + puntoA + " - " + puntoB + "   " + fecha
            );
        }

        adapter.notifyDataSetChanged();
    }

}
