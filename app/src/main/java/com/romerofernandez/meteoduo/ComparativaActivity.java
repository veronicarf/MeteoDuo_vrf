package com.romerofernandez.meteoduo;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * ComparativaActivity
 *
 * Actividad encargada de permitir al usuario comparar el tiempo entre dos puntos:
 * - Punto B: Provincia + Municipio
 *
 * @author Verónica
 */
public class ComparativaActivity extends AppCompatActivity {

    // -----------------------------
    // 1) COMPONENTES DE INTERFAZ
    // -----------------------------

    /**
     * Campos de autocompletado para provincias y municipios.
    */
    private AutoCompleteTextView spProvA, spMunA, spProvB, spMunB;

    // -----------------------------
    // 2) RED (VOLLEY)
    // -----------------------------

    /**
     * Cola de peticiones de red utilizada por Volley.
     */
    private RequestQueue queue;

    // -----------------------------
    // 3) DATOS DE PROVINCIAS
    // -----------------------------

    /**
     * Lista con los nombres de las provincias obtenidas de la API.
     */
    private final List<String> provinciaNombres = new ArrayList<>();

    /**
     * Mapa que relaciona el nombre de una provincia con su código (CPRO).
     */
    private final Map<String, String> provinciaNombreToCpro = new HashMap<>();

    // -----------------------------
    // 4) DATOS DE MUNICIPIOS
    // -----------------------------

    /**
     * Listas de municipios correspondientes a cada punto de comparación.
     */
    private final List<String> municipiosA = new ArrayList<>();
    private final List<String> municipiosB = new ArrayList<>();

    // -----------------------------
    // 5) ADAPTERS
    // -----------------------------

    /**
     * Adapters utilizados por los AutoCompleteTextView:
     */
    private ArrayAdapter<String> adapterProv;
    private ArrayAdapter<String> adapterMunA;
    private ArrayAdapter<String> adapterMunB;

    // -----------------------------
    // 6) CONFIGURACIÓN GEOAPI
    // -----------------------------

    /**
     * URL base de GeoAPI.
     */
    private static final String BASE = "https://apiv1.geoapi.es";

    /**
     * Clave de autenticación necesaria para GeoAPI.
     */
    private static final String KEY = "1fc368b23d25818cdd878134ec20df90e9e67a7a38481c4e395d0a6d56f99abc";

    /**
     * Parámetros para realizar las peticiones
     */
    private static final String COMMON_PARAMS = "FORMAT=json&PAGE_SIZE=1000&KEY=" + KEY;

    // -----------------------------
    // 7) OTROS
    // -----------------------------

    /**
     * Variables auxiliares para evitar recargar municipios
     */
    private String ultimaProvA = "";
    private String ultimaProvB = "";



    /**
     * Método principal de inicialización de la Activity.
     * Inicializa la interfaz, configura autocompletados y carga provincias.
     *
     * @param savedInstanceState Estado previamente guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        // Inicializa la cola de peticiones de Volley
        queue = Volley.newRequestQueue(this);

        // Se enlaza con los elementos definidos en el layout
        spProvA = findViewById(R.id.autoCompleteTextView);
        spMunA  = findViewById(R.id.autoCompleteTextView3);
        spProvB = findViewById(R.id.autoCompleteTextView2);
        spMunB  = findViewById(R.id.autoCompleteTextView4);

        // Botón volver
        Button btnVolver = findViewById(R.id.btnVolvercom);

        // Botón buscar
        Button btnBuscar = findViewById(R.id.btnBuscar);

        // El autocompletado se activa a partir de una letra
        spProvA.setThreshold(1);
        spProvB.setThreshold(1);
        spMunA.setThreshold(1);
        spMunB.setThreshold(1);

        // Adapter de provincias compartido por los campos A y B
        adapterProv = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                provinciaNombres
        );
        spProvA.setAdapter(adapterProv);
        spProvB.setAdapter(adapterProv);

        // Adapters independientes para los municipios de cada punto
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

        // Se configura el autocompletado para  mostrar desplegable al enfocar/click/escribir
        autoShowDropdown(spProvA);
        autoShowDropdown(spProvB);
        autoShowDropdown(spMunA);
        autoShowDropdown(spMunB);

        // Se vincula  provincias con municipios
        vincularProvinciaConMunicipios(spProvA, spMunA, true);
        vincularProvinciaConMunicipios(spProvB, spMunB, false);

        // Y se cargan los municipios al seleccionar provincia desde el desplegable
        spProvA.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvA.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, true);
        });

        spProvB.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvB.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, false);
        });

        // BOTÓN VOLVER
        btnVolver.setOnClickListener(v -> finish());

        // BOTÓN BUSCAR
        btnBuscar.setOnClickListener(v -> buscarComparativa());

        // Carga inicial de provincias
        cargarProvincias();
    }

    /**
     * Método que configura un AutoCompleteTextView para que muestre el desplegable automáticamente
     *
     * @param actv Campo de autocompletado a configurar
     */
    private void autoShowDropdown(AutoCompleteTextView actv) {
        actv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actv.post(actv::showDropDown);
        });

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

    /**
     * Método que vincula el campo de provincia con su correspondiente campo de municipios.
     *
     * @param prov Campo de provincia
     * @param mun Campo de municipio asociado
     * @param esPuntoA True si corresponde al Punto A, false si corresponde al Punto B
     */
    private void vincularProvinciaConMunicipios(AutoCompleteTextView prov, AutoCompleteTextView mun, boolean esPuntoA) {
        prov.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) return;
                String texto = s.toString().trim();

                // Evita recargar si el texto no ha cambiado
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

    /**
     * Método que limpia la lista de municipios del punto indicado y actualiza su adapter.
     *
     * @param esPuntoA True si corresponde al Punto A, false si corresponde al Punto B
     */
    private void limpiarMunicipios(boolean esPuntoA) {
        if (esPuntoA) {
            municipiosA.clear();
            adapterMunA.notifyDataSetChanged();
        } else {
            municipiosB.clear();
            adapterMunB.notifyDataSetChanged();
        }
    }

    /**
     * Método que comprueba si la provincia existe en el mapa y, en ese caso,
     * obtiene su código y carga los municipios correspondientes.
     *
     * @param nombreProvincia Nombre de la provincia
     * @param esPuntoA True si corresponde al Punto A, false si corresponde al Punto B
     */
    private void cargarMunicipiosSiProvinciaValida(String nombreProvincia, boolean esPuntoA) {
        String cpro = provinciaNombreToCpro.get(nombreProvincia);
        if (cpro != null) cargarMunicipios(cpro, esPuntoA);
    }

    /**
     * Método que realiza una petición a GeoAPI para obtener la lista de provincias
     * y preparar los datos necesarios para el autocompletado.
     */
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
                                provinciaNombres.add(Mayuscula(nombre));
                                provinciaNombreToCpro.put(Mayuscula(nombre), cpro);
                            }
                        }

                        adapterProv.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parseando provincias", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Error de red al cargar provincias", Toast.LENGTH_LONG).show()
        );

        queue.add(req);
    }

    /**
     * Método que realiza una petición a GeoAPI para obtener los municipios de una provincia
     *
     * @param cpro Código de la provincia
     * @param esPuntoA True si los datos son para el Punto A, false para el Punto B
     */
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
                            String nombre = data.getJSONObject(i)
                                    .optString("DMUN50", "").trim();
                            if (!nombre.isEmpty()) destino.add(Mayuscula(nombre));
                        }

                        adapter.addAll(destino);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parseando municipios", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Error de red al cargar municipios", Toast.LENGTH_LONG).show()
        );

        queue.add(req);
    }

    /**
     * Método que convierte un texto para mostrarlo con la primera letra en mayúscula.
     *
     * @param s Texto original
     * @return Texto con capitalización básica (Primera letra mayúscula y resto minúsculas)
     */
    private String Mayuscula(String s) {
        if (s == null || s.isEmpty()) return "";
        s = s.toLowerCase().trim();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Método que obtiene coordenadas  a partir de municipio y provincia usando Geocoder.
     *
     * @param municipio Nombre del municipio
     * @param provincia Nombre de la provincia
     * @param onOk Callback si se obtienen coordenadas (lat, lon)
     * @param onFail Callback si falla la búsqueda de coordenadas
     */
    private void geocodeMunicipio(String municipio, String provincia,
                                  java.util.function.BiConsumer<Double, Double> onOk,
                                  Runnable onFail) {

        new Thread(() -> {
            try {
                android.location.Geocoder geocoder =
                        new android.location.Geocoder(this, java.util.Locale.getDefault());

                String query = municipio + ", " + provincia + ", España";
                java.util.List<android.location.Address> res = geocoder.getFromLocationName(query, 1);

                if (res != null && !res.isEmpty()) {
                    double lat = res.get(0).getLatitude();
                    double lon = res.get(0).getLongitude();
                    runOnUiThread(() -> onOk.accept(lat, lon));
                } else {
                    runOnUiThread(onFail);
                }
            } catch (Exception e) {
                runOnUiThread(onFail);
            }
        }).start();
    }

    /**
     * Método que construye la URL de consulta a Open-Meteo para obtener previsión de hoy
     *
     * @param lat Latitud
     * @param lon Longitud
     * @return URL completa lista para la petición GET
     */
    private String buildOpenMeteoUrl(double lat, double lon) {
        return "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max,weathercode"
                + "&forecast_days=3"
                + "&timezone=auto";
    }

    /**
     * Método que ejecuta el flujo completo de búsqueda:
     * 1) Valida selecciones
     * 2) Geocodifica A y B para obtener las coordenadas
     * 3) Consulta el tiempo en Open-Meteo para ambos puntos
     * 4) Abre ResultadosActivity con los datos en JSON
     */
    private void buscarComparativa() {

        String provA = spProvA.getText().toString().trim();
        String munA  = spMunA.getText().toString().trim();
        String provB = spProvB.getText().toString().trim();
        String munB  = spMunB.getText().toString().trim();

        // 1) Se realizan las validaciones
        if (!validarSeleccion(provA, munA, true)) return;
        if (!validarSeleccion(provB, munB, false)) return;

        String puntoA = munA + " (" + provA + ")";
        String puntoB = munB + " (" + provB + ")";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaInicio = sdf.format(Calendar.getInstance().getTime());

        Toast.makeText(this, "Buscando coordenadas y tiempo real...", Toast.LENGTH_SHORT).show();

        // 2) Geocoding A
        geocodeMunicipio(munA, provA, (latA, lonA) -> {

            // 3) Geocoding B
            geocodeMunicipio(munB, provB, (latB, lonB) -> {

                // 4) y por último se carga el  tiempo de  A y B
                cargarTiempoDosPuntos(latA, lonA, latB, lonB, puntoA, puntoB, fechaInicio);

            }, () -> Toast.makeText(this, "No se han encontrado coordenadas para Punto B", Toast.LENGTH_LONG).show());

        }, () -> Toast.makeText(this, "No se han encontrado coordenadas para Punto A", Toast.LENGTH_LONG).show());
    }

    /**
     * Método que  Valida que:
     * - La provincia seleccionada exista en el mapa de provincias
     * - El municipio exista dentro de la lista de municipios cargada para ese punto
     *
     *
     * @param prov Provincia introducida
     * @param mun Municipio introducido
     * @param esPuntoA True si corresponde al Punto A, false si corresponde al Punto B
     * @return True si la selección es válida, false en caso contrario
     */
    private boolean validarSeleccion(String prov, String mun, boolean esPuntoA) {

        if (!provinciaNombreToCpro.containsKey(prov)) {
            Toast.makeText(this,
                    esPuntoA ? "Selecciona una provincia válida en Punto A" : "Selecciona una provincia válida en Punto B",
                    Toast.LENGTH_LONG).show();

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

    /**
     * Método que realiza dos peticiones a Open-Meteo por cada punto
     *
     * @param latA Latitud del Punto A
     * @param lonA Longitud del Punto A
     * @param latB Latitud del Punto B
     * @param lonB Longitud del Punto B
     * @param puntoA Texto descriptivo del Punto A
     * @param puntoB Texto descriptivo del Punto B
     * @param fechaInicio Fecha de inicio de la consulta
     */
    private void cargarTiempoDosPuntos(double latA, double lonA, double latB, double lonB,
                                       String puntoA, String puntoB, String fechaInicio) {

        final org.json.JSONObject[] jsonA = new org.json.JSONObject[1];
        final org.json.JSONObject[] jsonB = new org.json.JSONObject[1];

        cargarTiempoOpenMeteo(latA, lonA,
                respA -> {
                    jsonA[0] = respA;
                    if (jsonB[0] != null) abrirResultados(puntoA, puntoB, fechaInicio, jsonA[0], jsonB[0]);
                },
                err -> Toast.makeText(this, "Error tiempo Punto A", Toast.LENGTH_LONG).show()
        );

        cargarTiempoOpenMeteo(latB, lonB,
                respB -> {
                    jsonB[0] = respB;
                    if (jsonA[0] != null) abrirResultados(puntoA, puntoB, fechaInicio, jsonA[0], jsonB[0]);
                },
                err -> Toast.makeText(this, "Error tiempo Punto B", Toast.LENGTH_LONG).show()
        );
    }

    /**
     * Abre ResultadosActivity enviando por Intent:
     * - nombres de los puntos
     * - fecha de consulta
     * - respuestas JSON de Open-Meteo en formato String
     *
     * @param puntoA Texto descriptivo del Punto A
     * @param puntoB Texto descriptivo del Punto B
     * @param fechaInicio Fecha de inicio (dd/MM/yyyy)
     * @param jsonA Respuesta JSON del Punto A
     * @param jsonB Respuesta JSON del Punto B
     */
    private void abrirResultados(String puntoA, String puntoB, String fechaInicio,
                                 org.json.JSONObject jsonA, org.json.JSONObject jsonB) {

        Intent i = new Intent(ComparativaActivity.this, ResultadosActivity.class);
        i.putExtra("puntoA", puntoA);
        i.putExtra("puntoB", puntoB);
        i.putExtra("fechaInicio", fechaInicio);
        i.putExtra("jsonA", jsonA.toString());
        i.putExtra("jsonB", jsonB.toString());
        startActivity(i);
    }

    /**
     * Método que realiza una petición GET a Open-Meteo para obtener la previsión meteorológica
     *
     * @param lat Latitud del punto
     * @param lon Longitud del punto
     * @param ok Callback si la respuesta es correcta
     * @param fail Callback si ocurre un error
     */
    private void cargarTiempoOpenMeteo(double lat, double lon,
                                       com.android.volley.Response.Listener<org.json.JSONObject> ok,
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
