package com.romerofernandez.meteoduo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparativaActivity extends AppCompatActivity {

    // UI
    private AutoCompleteTextView spProvA, spMunA, spProvB, spMunB;

    // Volley
    private RequestQueue queue;

    // Provincias
    private final List<String> provinciaNombres = new ArrayList<>();
    private final Map<String, String> provinciaNombreToCpro = new HashMap<>();

    // Municipios A / B
    private final List<String> municipiosA = new ArrayList<>();
    private final List<String> municipiosB = new ArrayList<>();

    // Adapters
    private ArrayAdapter<String> adapterProv;
    private ArrayAdapter<String> adapterMunA;
    private ArrayAdapter<String> adapterMunB;

    // GeoAPI
    private static final String BASE = "https://apiv1.geoapi.es";

    // ✅ PON AQUÍ TU KEY (para que funcione al clonar tu repo)
    private static final String KEY = "1fc368b23d25818cdd878134ec20df90e9e67a7a38481c4e395d0a6d56f99abc";

    // ✅ Params centralizados
    private static final String COMMON_PARAMS = "FORMAT=json&PAGE_SIZE=1000&KEY=" + KEY;

    // Para no recargar municipios todo el rato si escribe lo mismo
    private String ultimaProvA = "";
    private String ultimaProvB = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        queue = Volley.newRequestQueue(this);

        spProvA = findViewById(R.id.autoCompleteTextView);
        spMunA  = findViewById(R.id.autoCompleteTextView3);

        spProvB = findViewById(R.id.autoCompleteTextView2);
        spMunB  = findViewById(R.id.autoCompleteTextView4);

        // Threshold: con 1 letra ya sugiere
        spProvA.setThreshold(1);
        spProvB.setThreshold(1);
        spMunA.setThreshold(1);
        spMunB.setThreshold(1);

        // Adapter provincias (compartido)
        adapterProv = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                provinciaNombres
        );
        spProvA.setAdapter(adapterProv);
        spProvB.setAdapter(adapterProv);

        // Adapters municipios
        adapterMunA = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                municipiosA
        );
        spMunA.setAdapter(adapterMunA);

        adapterMunB = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                municipiosB
        );
        spMunB.setAdapter(adapterMunB);

        // Mostrar sugerencias automáticamente (y al tocar)
        autoShowDropdown(spProvA);
        autoShowDropdown(spProvB);
        autoShowDropdown(spMunA);
        autoShowDropdown(spMunB);

        // Si el usuario escribe y NO pulsa sugerencia, igual cargamos municipios
        vincularProvinciaConMunicipios(spProvA, spMunA, true);
        vincularProvinciaConMunicipios(spProvB, spMunB, false);

        // Si pulsa una sugerencia, también carga
        spProvA.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvA.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, true);
        });

        spProvB.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvB.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, false);
        });

        // ✅ Comprobación rápida de KEY
        if (KEY == null || KEY.trim().isEmpty() || KEY.equals("PON_AQUI_TU_KEY")) {
            Toast.makeText(this, "Falta la KEY de GeoAPI en ComparativaActivity (variable KEY).", Toast.LENGTH_LONG).show();
            return;
        }

        cargarProvincias();
    }

    private void autoShowDropdown(AutoCompleteTextView actv) {
        actv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actv.post(actv::showDropDown);
        });

        // Al tocar el campo, abre siempre
        actv.setOnClickListener(v -> actv.showDropDown());

        actv.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() >= 1 && actv.hasFocus()) {
                    actv.post(actv::showDropDown);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void vincularProvinciaConMunicipios(AutoCompleteTextView prov, AutoCompleteTextView mun, boolean esPuntoA) {
        prov.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) return;
                String texto = s.toString().trim();

                if (esPuntoA) {
                    if (texto.equals(ultimaProvA)) return;
                    ultimaProvA = texto;
                } else {
                    if (texto.equals(ultimaProvB)) return;
                    ultimaProvB = texto;
                }

                if (provinciaNombreToCpro.containsKey(texto)) {
                    android.util.Log.d("MeteoDuo", "Provincia detectada: " + texto + ". Cargando municipios...");
                    mun.setText("");
                    cargarMunicipiosSiProvinciaValida(texto, esPuntoA);
                } else {
                    if (texto.isEmpty()) limpiarMunicipios(esPuntoA);
                }
            }
        });
    }

    private void limpiarMunicipios(boolean esPuntoA) {
        if (esPuntoA) {
            municipiosA.clear();
            adapterMunA.notifyDataSetChanged();
        } else {
            municipiosB.clear();
            adapterMunB.notifyDataSetChanged();
        }
    }

    private void cargarMunicipiosSiProvinciaValida(String nombreProvincia, boolean esPuntoA) {
        String cpro = provinciaNombreToCpro.get(nombreProvincia);
        if (cpro != null) cargarMunicipios(cpro, esPuntoA);
    }

    private void cargarProvincias() {
        provinciaNombres.clear();
        provinciaNombreToCpro.clear();
        adapterProv.notifyDataSetChanged();

        String url = BASE + "/provincias/?" + COMMON_PARAMS;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        android.util.Log.d("MeteoDuo", "Provincias recibidas API: " + data.length());

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);

                            String cpro = item.optString("CPRO", "").trim();
                            String nombre = item.optString("PRO", "").trim();

                            if (!cpro.isEmpty() && !nombre.isEmpty()) {
                                String nombreOk = capitalizar(nombre);
                                provinciaNombres.add(nombreOk);
                                provinciaNombreToCpro.put(nombreOk, cpro);
                            }
                        }

                        android.util.Log.d("MeteoDuo", "Provincias en lista: " + provinciaNombres.size());

                        adapterProv.notifyDataSetChanged();
                        spProvA.post(() -> spProvA.showDropDown());

                        if (provinciaNombres.isEmpty()) {
                            Toast.makeText(this, "No se han recibido provincias.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parseando provincias: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    String body = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        body = new String(error.networkResponse.data);
                    }
                    android.util.Log.e("MeteoDuo", "Volley error: " + error.toString() + " BODY=" + body);

                    // ✅ Mensaje claro si la KEY falla
                    if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        Toast.makeText(this, "GeoAPI 403: KEY inválida o no proporcionada.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error red. Mira Logcat", Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(req);
    }

    private void cargarMunicipios(String cpro, boolean esPuntoA) {
        List<String> destino = esPuntoA ? municipiosA : municipiosB;
        ArrayAdapter<String> adapter = esPuntoA ? adapterMunA : adapterMunB;

        String url = BASE + "/municipios/?" + COMMON_PARAMS + "&CPRO=" + cpro;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");

                        destino.clear();
                        adapter.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String nombre = item.optString("DMUN50", "").trim();
                            if (!nombre.isEmpty()) destino.add(capitalizar(nombre));
                        }

                        adapter.addAll(destino);
                        adapter.notifyDataSetChanged();

                        if (destino.isEmpty()) {
                            Toast.makeText(this, "No hay municipios", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parseando municipios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    String body = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        body = new String(error.networkResponse.data);
                    }
                    android.util.Log.e("MeteoDuo", "Volley error: " + error.toString() + " BODY=" + body);

                    if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        Toast.makeText(this, "GeoAPI 403: KEY inválida o no proporcionada.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error red. Mira Logcat", Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(req);
    }

    private String capitalizar(String s) {
        if (s == null) return "";
        s = s.toLowerCase().trim();
        if (s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
