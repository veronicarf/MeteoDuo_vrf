package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class ComparativaActivity extends AppCompatActivity {

    // -----------------------------
    // 1) COMPONENTES DE INTERFAZ
    // -----------------------------
    private AutoCompleteTextView spProvA, spMunA, spProvB, spMunB;

    // -----------------------------
    // 2) RED (VOLLEY)
    // -----------------------------
    private RequestQueue queue;

    // -----------------------------
    // 3) DATOS DE PROVINCIAS
    // -----------------------------
    private final List<String> provinciaNombres = new ArrayList<>();
    private final Map<String, String> provinciaNombreToCpro = new HashMap<>();

    // -----------------------------
    // 4) DATOS DE MUNICIPIOS
    // -----------------------------
    private final List<String> municipiosA = new ArrayList<>();
    private final List<String> municipiosB = new ArrayList<>();

    // -----------------------------
    // 5) ADAPTERS
    // -----------------------------
    private ArrayAdapter<String> adapterProv;
    private ArrayAdapter<String> adapterMunA;
    private ArrayAdapter<String> adapterMunB;

    // -----------------------------
    // 6) CONFIGURACIÓN GEOAPI
    // -----------------------------
    private static final String BASE = "https://apiv1.geoapi.es";
    private static final String KEY = "1fc368b23d25818cdd878134ec20df90e9e67a7a38481c4e395d0a6d56f99abc";
    private static final String COMMON_PARAMS = "FORMAT=json&PAGE_SIZE=1000&KEY=" + KEY;
    // -----------------------------
    // 7) OTROS
    // -----------------------------
    private String ultimaProvA = "";
    private String ultimaProvB = "";

    /** Email del usuario actual para loguear errores */
    private String emailActual() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return (u != null && u.getEmail() != null) ? u.getEmail() : "desconocido";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        queue = Volley.newRequestQueue(this);

        spProvA = findViewById(R.id.autoCompleteTextView);
        spMunA  = findViewById(R.id.autoCompleteTextView3);
        spProvB = findViewById(R.id.autoCompleteTextView2);
        spMunB  = findViewById(R.id.autoCompleteTextView4);

        Button btnVolver = findViewById(R.id.btnVolvercom);
        Button btnBuscar = findViewById(R.id.btnBuscar);

        spProvA.setThreshold(1);
        spProvB.setThreshold(1);
        spMunA.setThreshold(1);
        spMunB.setThreshold(1);

        adapterProv = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, provinciaNombres);
        spProvA.setAdapter(adapterProv);
        spProvB.setAdapter(adapterProv);

        adapterMunA = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, municipiosA);
        spMunA.setAdapter(adapterMunA);

        adapterMunB = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, municipiosB);
        spMunB.setAdapter(adapterMunB);

        autoShowDropdown(spProvA);
        autoShowDropdown(spProvB);
        autoShowDropdown(spMunA);
        autoShowDropdown(spMunB);

        vincularProvinciaConMunicipios(spProvA, spMunA, true);
        vincularProvinciaConMunicipios(spProvB, spMunB, false);

        spProvA.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvA.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, true);
            spMunA.post(spMunA::showDropDown);
        });

        spProvB.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvB.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, false);
            spMunB.post(spMunB::showDropDown);
        });

        btnVolver.setOnClickListener(v -> finish());
        btnBuscar.setOnClickListener(v -> buscarComparativa());

        cargarProvincias();
    }

    private void autoShowDropdown(AutoCompleteTextView actv) {
        actv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actv.post(actv::showDropDown);
        });

        actv.setOnClickListener(v -> actv.showDropDown());

        actv.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (actv.hasFocus() && s != null && s.length() > 0) {
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
                    mun.setText("");
                    cargarMunicipiosSiProvinciaValida(texto, esPuntoA);
                } else if (texto.isEmpty()) {
                    limpiarMunicipios(esPuntoA);
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
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String cpro = item.optString("CPRO", "").trim();
                            String nombre = item.optString("PRO", "").trim();

                            if (!cpro.isEmpty() && !nombre.isEmpty()) {
                                String nombreFmt = Mayuscula(nombre);
                                provinciaNombres.add(nombreFmt);
                                provinciaNombreToCpro.put(nombreFmt, cpro);
                            }
                        }
                        adapterProv.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parseando provincias", Toast.LENGTH_LONG).show();
                        RegistroConexiones.error(emailActual(), "GEOAPI", "Parse provincias: " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "Error de red al cargar provincias", Toast.LENGTH_LONG).show();
                    RegistroConexiones.error(emailActual(), "GEOAPI", "Red provincias: " + error.toString());
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
                            String nombre = data.getJSONObject(i).optString("DMUN50", "").trim();
                            if (!nombre.isEmpty()) destino.add(Mayuscula(nombre));
                        }

                        adapter.addAll(destino);
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(this, "Error parseando municipios", Toast.LENGTH_LONG).show();
                        RegistroConexiones.error(emailActual(), "GEOAPI",
                                "Parse municipios CPRO=" + cpro + ": " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "Error de red al cargar municipios", Toast.LENGTH_LONG).show();
                    RegistroConexiones.error(emailActual(), "GEOAPI",
                            "Red municipios CPRO=" + cpro + ": " + error.toString());
                }
        );

        queue.add(req);
    }

    private String Mayuscula(String s) {
        if (s == null || s.isEmpty()) return "";
        s = s.toLowerCase().trim();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void geocodeMunicipio(String municipio, String provincia,
                                  BiConsumer<Double, Double> onOk,
                                  Runnable onFail) {

        String mun = (municipio == null) ? "" : municipio.trim();
        String prov = (provincia == null) ? "" : provincia.trim();

        Log.d("COORD", "Buscando coordenadas de: [" + mun + "] prov=[" + prov + "]");

        if (mun.isEmpty() || prov.isEmpty()) {
            RegistroConexiones.error(emailActual(), "GEOCODER",
                    "Municipio/provincia vacíos. mun=" + mun + " prov=" + prov);
            runOnUiThread(onFail);
            return;
        }

        new Thread(() -> {
            String query = mun + ", " + prov + ", España";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> res = geocoder.getFromLocationName(query, 1);

                if (res != null && !res.isEmpty() && res.get(0).hasLatitude() && res.get(0).hasLongitude()) {
                    double lat = res.get(0).getLatitude();
                    double lon = res.get(0).getLongitude();
                    runOnUiThread(() -> onOk.accept(lat, lon));
                } else {
                    RegistroConexiones.error(emailActual(), "GEOCODER", "Sin coordenadas para: " + query);
                    runOnUiThread(onFail);
                }

            } catch (Exception e) {
                Log.e("COORD", "Error geocoding: " + query, e);
                RegistroConexiones.error(emailActual(), "GEOCODER",
                        "Excepción geocoding " + query + ": " + e.getMessage());
                runOnUiThread(onFail);
            }
        }).start();
    }

    private String buildOpenMeteoUrl(double lat, double lon) {
        return "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max,weathercode"
                + "&forecast_days=3"
                + "&timezone=auto";
    }

    private void buscarComparativa() {

        String provA = spProvA.getText().toString().trim();
        String munA  = spMunA.getText().toString().trim();
        String provB = spProvB.getText().toString().trim();
        String munB  = spMunB.getText().toString().trim();

        if (!validarSeleccion(provA, munA, true)) return;
        if (!validarSeleccion(provB, munB, false)) return;

        String puntoA = munA + " (" + provA + ")";
        String puntoB = munB + " (" + provB + ")";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaInicio = sdf.format(Calendar.getInstance().getTime());

        Toast.makeText(this, "Buscando coordenadas y tiempo real...", Toast.LENGTH_SHORT).show();

        geocodeMunicipio(munA, provA, (latA, lonA) -> {

            geocodeMunicipio(munB, provB, (latB, lonB) -> {

                cargarTiempoDosPuntos(latA, lonA, latB, lonB, puntoA, puntoB, fechaInicio);

            }, () -> {
                Toast.makeText(this, "No se han encontrado coordenadas para Punto B", Toast.LENGTH_LONG).show();
                RegistroConexiones.error(emailActual(), "GEOCODER", "No coords Punto B: " + puntoB);
            });

        }, () -> {
            Toast.makeText(this, "No se han encontrado coordenadas para Punto A", Toast.LENGTH_LONG).show();
            RegistroConexiones.error(emailActual(), "GEOCODER", "No coords Punto A: " + puntoA);
        });
    }

    private boolean validarSeleccion(String prov, String mun, boolean esPuntoA) {

        if (!provinciaNombreToCpro.containsKey(prov)) {
            Toast.makeText(this,
                    esPuntoA ? "Selecciona una provincia válida en Punto A" : "Selecciona una provincia válida en Punto B",
                    Toast.LENGTH_LONG).show();

            RegistroConexiones.error(emailActual(), "VALIDACION",
                    (esPuntoA ? "Provincia inválida A: " : "Provincia inválida B: ") + prov);

            if (esPuntoA) {
                spProvA.requestFocus();
                spProvA.showDropDown();
            } else {
                spProvB.requestFocus();
                spProvB.showDropDown();
            }
            return false;
        }

        List<String> listaMun = esPuntoA ? municipiosA : municipiosB;
        if (!listaMun.contains(mun)) {
            Toast.makeText(this,
                    esPuntoA ? "Selecciona un municipio válido en Punto A" : "Selecciona un municipio válido en Punto B",
                    Toast.LENGTH_LONG).show();

            RegistroConexiones.error(emailActual(), "VALIDACION",
                    (esPuntoA ? "Municipio inválido A: " : "Municipio inválido B: ") + mun + " (prov=" + prov + ")");

            if (esPuntoA) {
                spMunA.requestFocus();
                spMunA.showDropDown();
            } else {
                spMunB.requestFocus();
                spMunB.showDropDown();
            }
            return false;
        }

        return true;
    }

    private void cargarTiempoDosPuntos(double latA, double lonA, double latB, double lonB,
                                       String puntoA, String puntoB, String fechaInicio) {

        final JSONObject[] jsonA = new JSONObject[1];
        final JSONObject[] jsonB = new JSONObject[1];

        cargarTiempoOpenMeteo(latA, lonA,
                respA -> {
                    jsonA[0] = respA;
                    if (jsonB[0] != null) {
                        // OK final comparativa
                        RegistroConexiones.ok(emailActual(), "COMPARATIVA");
                        abrirResultados(puntoA, puntoB, fechaInicio, jsonA[0], jsonB[0]);
                    }
                },
                err -> {
                    Toast.makeText(this, "Error tiempo Punto A", Toast.LENGTH_LONG).show();
                    RegistroConexiones.error(emailActual(), "OPEN_METEO", "Error tiempo Punto A: " + err.toString());
                }
        );

        cargarTiempoOpenMeteo(latB, lonB,
                respB -> {
                    jsonB[0] = respB;
                    if (jsonA[0] != null) {
                        // OK final comparativa
                        RegistroConexiones.ok(emailActual(), "COMPARATIVA");
                        abrirResultados(puntoA, puntoB, fechaInicio, jsonA[0], jsonB[0]);
                    }
                },
                err -> {
                    Toast.makeText(this, "Error tiempo Punto B", Toast.LENGTH_LONG).show();
                    RegistroConexiones.error(emailActual(), "OPEN_METEO", "Error tiempo Punto B: " + err.toString());
                }
        );
    }

    private void abrirResultados(String puntoA, String puntoB, String fechaInicio,
                                 JSONObject jsonA, JSONObject jsonB) {

        Intent i = new Intent(ComparativaActivity.this, ResultadosActivity.class);
        i.putExtra("puntoA", puntoA);
        i.putExtra("puntoB", puntoB);
        i.putExtra("fechaInicio", fechaInicio);
        i.putExtra("jsonA", jsonA.toString());
        i.putExtra("jsonB", jsonB.toString());
        startActivity(i);
    }

    private void cargarTiempoOpenMeteo(double lat, double lon,
                                       com.android.volley.Response.Listener<JSONObject> ok,
                                       com.android.volley.Response.ErrorListener fail) {

        String url = buildOpenMeteoUrl(lat, lon);

        com.android.volley.toolbox.JsonObjectRequest req =
                new com.android.volley.toolbox.JsonObjectRequest(
                        com.android.volley.Request.Method.GET,
                        url,
                        null,
                        ok,
                        fail
                );

        queue.add(req);
    }
}
