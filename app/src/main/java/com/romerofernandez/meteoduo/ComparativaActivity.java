package com.romerofernandez.meteoduo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import com.android.volley.Response;
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

/**
 * Activity que permite comparar la previsión meteorológica
 * de dos puntos distintos (Provincia + Municipio) utilizando:
 * - GEOAPI (provincias y municipios)
 * - Geocoder (coordenadas)
 * - Open-Meteo (predicción meteorológica)
 *
 *  @author Verónica Romero
 */
public class ComparativaActivity extends AppCompatActivity {


// ==============================
// SELECTORES (AUTOCOMPLETE)
// ==============================

    /** AutoCompleteTextView para seleccionar provincia y municipio del Punto A y del Punto B. */
    private AutoCompleteTextView spProvA, spMunA, spProvB, spMunB;


// ==============================
// RED (VOLLEY)
// ==============================

    /** Cola de peticiones Volley utilizada para realizar llamadas HTTP a la API. */
    private RequestQueue queue;


// ==============================
// DATOS DE PROVINCIAS
// ==============================

    /** Lista con los nombres de provincias obtenidos desde la API. */
    private final List<String> provinciaNombres = new ArrayList<>();

    /** Mapa que relaciona el nombre de la provincia con su código CPRO devuelto por la API. */
    private final Map<String, String> provinciaNombreToCpro = new HashMap<>();


// ==============================
// DATOS DE MUNICIPIOS
// ==============================

    /** Lista de municipios disponibles para el Punto A (según provincia seleccionada). */
    private final List<String> municipiosA = new ArrayList<>();

    /** Lista de municipios disponibles para el Punto B (según provincia seleccionada). */
    private final List<String> municipiosB = new ArrayList<>();


// ==============================
// ADAPTERS
// ==============================

    /** Adaptador para mostrar la lista de provincias en los AutoComplete. */
    private ArrayAdapter<String> adapterProv;

    /** Adaptador para mostrar los municipios del Punto A. */
    private ArrayAdapter<String> adapterMunA;

    /** Adaptador para mostrar los municipios del Punto B. */
    private ArrayAdapter<String> adapterMunB;


// ==============================
// CONFIGURACIÓN GEOAPI
// ==============================

    /** URL base del servicio GeoAPI utilizado para consultar provincias y municipios. */
    private static final String BASE = "https://apiv1.geoapi.es";

    /** Clave de acceso a GeoAPI. Se mantiene fuera del código en producción por seguridad. */
    private static final String KEY = "1fc368b23d25818cdd878134ec20df90e9e67a7a38481c4e395d0a6d56f99abc";

    /** Parámetros comunes para todas las peticiones (formato, tamaño de página y clave). */
    private static final String COMMON_PARAMS = "FORMAT=json&PAGE_SIZE=1000&KEY=" + KEY;


// ==============================
// ESTADO DE SELECCIÓN
// ==============================

    /** Última provincia seleccionada en el Punto A  */
    private String ultimaProvA = "";

    /** Última provincia seleccionada en el Punto B  */
    private String ultimaProvB = "";



    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        inicializarVolley();
        enlazarVistas();
        configurarAdapters();
        configurarAutoComplete();
        configurarEventosUI();
        cargarProvincias();
    }

    /** Inicializa la cola de peticiones Volley. */
    private void inicializarVolley() {
        queue = Volley.newRequestQueue(this);
    }

    /** Enlaza los componentes del layout con las variables Java. */
    private void enlazarVistas() {
        spProvA = findViewById(R.id.autoCompleteTextView);
        spMunA  = findViewById(R.id.autoCompleteTextView3);
        spProvB = findViewById(R.id.autoCompleteTextView2);
        spMunB  = findViewById(R.id.autoCompleteTextView4);
    }

    /** Configura los adapters de provincias y municipios en los AutoCompleteTextView. */
    private void configurarAdapters() {

        // Provincias (mismo adapter para A y B)
        adapterProv = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, provinciaNombres);
        spProvA.setAdapter(adapterProv);
        spProvB.setAdapter(adapterProv);

        // Municipios A
        adapterMunA = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, municipiosA);
        spMunA.setAdapter(adapterMunA);

        // Municipios B
        adapterMunB = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, municipiosB);
        spMunB.setAdapter(adapterMunB);
    }

    /** Configura el comportamiento de autocompletado y el vínculo provincia y municipios. */
    private void configurarAutoComplete() {

        // Mínimo de caracteres para que aparezcan sugerencias
        spProvA.setThreshold(1);
        spProvB.setThreshold(1);
        spMunA.setThreshold(1);
        spMunB.setThreshold(1);

        // Mostrar desplegable automáticamente
        autoShowDropdown(spProvA);
        autoShowDropdown(spProvB);
        autoShowDropdown(spMunA);
        autoShowDropdown(spMunB);

        // Vincula provincia con municipios
        vincularProvinciaConMunicipios(spProvA, spMunA, true);
        vincularProvinciaConMunicipios(spProvB, spMunB, false);
    }

    /** Configura listeners de botones y eventos de selección de provincias. */
    private void configurarEventosUI() {

        Button btnVolver = findViewById(R.id.btnVolvercom);
        Button btnBuscar = findViewById(R.id.btnBuscar);

        // Selección provincia A
        spProvA.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvA.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, true);
            spMunA.post(spMunA::showDropDown);
        });

        // Selección provincia B
        spProvB.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvB.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, false);
            spMunB.post(spMunB::showDropDown);
        });

        // Botón volver
        btnVolver.setOnClickListener(v -> finish());

        // Botón buscar
        btnBuscar.setOnClickListener(v -> buscarComparativa());
    }


    /**
     * Devuelve el email del usuario actual para registrar errores.
     */
    private String emailActual() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return (u != null && u.getEmail() != null) ? u.getEmail() : "desconocido";
    }


    /**
     * Configura  AutoCompleteTextView para que muestre automáticamente
     *
     * @param actv AutoCompleteTextView al que se le aplica el comportamiento
     */
    private void autoShowDropdown(AutoCompleteTextView actv) {

        // Al ganar foco muestra el desplegable
        actv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actv.post(actv::showDropDown);
        });

        // Al hacer click muestra el desplegable
        actv.setOnClickListener(v -> actv.showDropDown());

        // Mientras se escribe mantiene abierto el desplegable
        actv.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (actv.hasFocus() && s != null && s.length() > 0) {
                    actv.post(actv::showDropDown);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Vincula un AutoCompleteTextView de provincias con su correspondiente
     *
     * @param prov AutoCompleteTextView de provincias
     * @param mun AutoCompleteTextView de municipios asociado
     * @param esPuntoA indica si la vinculación corresponde al Punto A (true) o Punto B (false)
     */
    private void vincularProvinciaConMunicipios(AutoCompleteTextView prov,AutoCompleteTextView mun,boolean esPuntoA) {

        prov.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) return;

                String texto = s.toString().trim();

                // Evita recargas innecesarias comparando con la última provincia
                if (esPuntoA) {
                    if (texto.equals(ultimaProvA)) return;
                    ultimaProvA = texto;
                } else {
                    if (texto.equals(ultimaProvB)) return;
                    ultimaProvB = texto;
                }

                // Si la provincia es válida, carga los municipios asociados
                if (provinciaNombreToCpro.containsKey(texto)) {
                    mun.setText("");
                    cargarMunicipiosSiProvinciaValida(texto, esPuntoA);
                }
                // Si se borra la provincia, limpia la lista de municipios
                else if (texto.isEmpty()) {
                    limpiarMunicipios(esPuntoA);
                }
            }
        });
    }

    /**
     * Limpia la lista de municipios del punto indicado y actualiza su adapter.
     *
     * @param esPuntoA indica si se deben limpiar los municipios del Punto A o B
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
     * Carga los municipios de una provincia únicamente si el nombre de la provincia
     * es válido y existe en el mapa de provincias.
     *
     * @param nombreProvincia nombre de la provincia seleccionada
     * @param esPuntoA        indica si la carga corresponde al Punto A y B
     */
    private void cargarMunicipiosSiProvinciaValida(String nombreProvincia, boolean esPuntoA) {
        String cpro = provinciaNombreToCpro.get(nombreProvincia);
        if (cpro != null) {
            cargarMunicipios(cpro, esPuntoA);
        }
    }



    /**
     * Carga la lista de provincias desde GEOAPI.
     */
    private void cargarProvincias() {

        // Limpia la lista de nombres de provincias
        provinciaNombres.clear();

        // Limpia el mapa nombre de provincia
        provinciaNombreToCpro.clear();

        // Notifica al adapter que los datos han cambiado
        adapterProv.notifyDataSetChanged();

        // Construye la URL para obtener las provincias desde GEOAPI
        String url = BASE + "/provincias/?" + COMMON_PARAMS;

        // Crea una petición HTTP GET que devuelve un JSON
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,   // Tipo de petición (GET)
                url,                  // URL del servicio
                null,                 // No se envía cuerpo en la petición
                response -> {          // Respuesta correcta del servidor
                    try {
                        // Obtiene el array "data" del JSON recibido
                        JSONArray data = response.getJSONArray("data");

                        // Recorre todas las provincias devueltas por la API
                        for (int i = 0; i < data.length(); i++) {

                            // Obtiene el objeto JSON de una provincia
                            JSONObject item = data.getJSONObject(i);

                            // Lee el código y nombre  de la provincia
                            String cpro = item.optString("CPRO", "").trim();
                            String nombre = item.optString("PRO", "").trim();

                            // Comprueba que ambos valores existan
                            if (!cpro.isEmpty() && !nombre.isEmpty()) {

                                // Primera letra en mayuscula
                                String nombreFmt = Mayuscula(nombre);

                                // Añade el nombre a la lista de provincias
                                provinciaNombres.add(nombreFmt);

                                // Guarda la relación nombre -> código de provincia
                                provinciaNombreToCpro.put(nombreFmt, cpro);
                            }
                        }

                        // Actualiza el AutoCompleteTextView de provincias
                        adapterProv.notifyDataSetChanged();

                    } catch (Exception e) {
                        // Error al procesar el JSON
                        Toast.makeText(this, "Error parseando provincias", Toast.LENGTH_LONG).show();

                        // Registra el error para auditoría/logs
                        RegistroConexiones.error(
                                emailActual(),
                                "GEOAPI",
                                "Parse provincias: " + e.getMessage()
                        );
                    }
                },
                error -> { // Error de red
                    Toast.makeText(this, "Error de red al cargar provincias", Toast.LENGTH_LONG).show();

                    // Registra el error de red
                    RegistroConexiones.error(
                            emailActual(),
                            "GEOAPI",
                            "Red provincias: " + error.toString()
                    );
                }
        );

        // Añade la petición a la cola de Volley para que se ejecute
        queue.add(req);
    }

    /**
     * Carga la lista de municipios correspondientes a una provincia concreta
     *
     * @param cpro código de la provincia
     * @param esPuntoA indica si los municipios corresponden al Punto A  y B
     */
    private void cargarMunicipios(String cpro, boolean esPuntoA) {

        // Selecciona la lista de municipios destino según el punto A/B
        List<String> destino = esPuntoA ? municipiosA : municipiosB;

        // Selecciona el adapter correspondiente A/B
        ArrayAdapter<String> adapter = esPuntoA ? adapterMunA : adapterMunB;

        // Construye la URL para obtener municipios filtrados por provincia
        String url = BASE + "/municipios/?" + COMMON_PARAMS + "&CPRO=" + cpro;

        // Crea la petición HTTP GET
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Obtiene el array de municipios del JSON de respuesta
                        JSONArray data = response.getJSONArray("data");

                        // Limpia los datos anteriores
                        destino.clear();
                        adapter.clear();

                        // Recorre todos los municipios devueltos por la API
                        for (int i = 0; i < data.length(); i++) {
                            String nombre = data.getJSONObject(i)
                                    .optString("DMUN50", "")
                                    .trim();

                            // Añade solo nombres válidos formateados
                            if (!nombre.isEmpty()) {
                                destino.add(Mayuscula(nombre));
                            }
                        }

                        // Añade los municipios al adapter
                        adapter.addAll(destino);

                        // Refresca el AutoCompleteTextView
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        // Error al procesar el JSON de municipios
                        Toast.makeText(this, "Error parseando municipios", Toast.LENGTH_LONG).show();

                        // Registra el error indicando la provincia afectada
                        RegistroConexiones.error(
                                emailActual(),
                                "GEOAPI",
                                "Parse municipios CPRO=" + cpro + ": " + e.getMessage()
                        );
                    }
                },
                error -> { // Error de red
                    Toast.makeText(this, "Error de red al cargar municipios", Toast.LENGTH_LONG).show();

                    RegistroConexiones.error(
                            emailActual(),
                            "GEOAPI",
                            "Red municipios CPRO=" + cpro + ": " + error.toString()
                    );
                }
        );

        // Añade la petición a la cola de Volley para su ejecución
        queue.add(req);
    }


    /**
     * Convierte una cadena a formato capitalizado.
     *
     *@param s palabra a modificcar
     */
    private String Mayuscula(String s) {
        if (s == null || s.isEmpty()) return "";
        s = s.toLowerCase().trim();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    /**
     * Obtiene las coordenadas de un municipio usando Geocoder.
     *
     * @param municipio nombre del municipio
     * @param provincia nombre de la provincia
     * @param onOk acción a ejecutar si se obtienen correctamente las coordenadas
     * @param onFail acción a ejecutar si ocurre algún error
     */
    private void geocodeMunicipio(String municipio, String provincia,
                                  BiConsumer<Double, Double> onOk,
                                  Runnable onFail) {

        // Normaliza el nombre del municipio y provincia
        String mun = (municipio == null) ? "" : municipio.trim();
        String prov = (provincia == null) ? "" : provincia.trim();

        // Si alguno de los campos está vacío, no se puede geocodificar
        if (mun.isEmpty() || prov.isEmpty()) {

            // Registra el error
            RegistroConexiones.error(
                    emailActual(),
                    "GEOCODER",
                    "Municipio/provincia vacíos. mun=" + mun + " prov=" + prov
            );

            // Ejecuta la acción de error
            runOnUiThread(onFail);
            return; // Sale del método
        }

        // Ejecuta el geocoding en un hilo secundario
        new Thread(() -> {
            try {
                // Construye la cadena de búsqueda
                String query = mun + ", " + prov + ", España";

                // Crea el Geocoder
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                // Solicita como máximo 1 resultado de localización
                List<Address> res = geocoder.getFromLocationName(query, 1);

                // Comprueba que se ha obtenido una dirección válida con coordenadas
                if (res != null && !res.isEmpty()
                        && res.get(0).hasLatitude()
                        && res.get(0).hasLongitude()) {

                    // latitud
                    double lat = res.get(0).getLatitude();

                    // longitud
                    double lon = res.get(0).getLongitude();

                    // Devuelve las coordenadas al hilo principal usando el callback onOk
                    runOnUiThread(() -> onOk.accept(lat, lon));

                } else {
                    // Error
                    RegistroConexiones.error(
                            emailActual(),
                            "GEOCODER",
                            "Sin coordenadas para: " + query
                    );

                    // Ejecuta la acción de error
                    runOnUiThread(onFail);
                }

            } catch (Exception e) {
                RegistroConexiones.error(
                        emailActual(),
                        "GEOCODER",
                        "Excepción geocoding: " + e.getMessage()
                );
                runOnUiThread(onFail);
            }
        }).start(); // Inicia el hilo secundario
    }


    /**
     * Método que construye la URL de Open-Meteo para una localización.
     *
     * @param lat altitud
     * @param  lon longitud
     */
    private String buildOpenMeteoUrl(double lat, double lon) {
        return "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=temperature_2m_max,temperature_2m_min,"
                + "precipitation_probability_max,windspeed_10m_max,weathercode"
                + "&forecast_days=3"
                + "&timezone=auto";
    }


    /**
     * Método que Inicia el proceso de búsqueda y comparación del tiempo.
     */
    private void buscarComparativa() {

        String provA = spProvA.getText().toString().trim();
        String munA  = spMunA.getText().toString().trim();
        String provB = spProvB.getText().toString().trim();
        String munB  = spMunB.getText().toString().trim();

        // Validaciones
        if (!validarSeleccion(provA, munA, true)) return;
        if (!validarSeleccion(provB, munB, false)) return;

        String puntoA = munA + " (" + provA + ")";
        String puntoB = munB + " (" + provB + ")";

        String fechaInicio = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        Toast.makeText(this, "Buscando coordenadas y tiempo real...", Toast.LENGTH_SHORT).show();

        // Geocoding Punto A → Punto B → Carga tiempo
        geocodeMunicipio(munA, provA, (latA, lonA) -> {

            geocodeMunicipio(munB, provB, (latB, lonB) -> {

                cargarTiempoDosPuntos(latA, lonA, latB, lonB,
                        puntoA, puntoB, fechaInicio);

            }, () -> {
                Toast.makeText(this, "No se han encontrado coordenadas para Punto B", Toast.LENGTH_LONG).show();
                RegistroConexiones.error(emailActual(), "GEOCODER", "No coords Punto B");
            });

        }, () -> {
            Toast.makeText(this, "No se han encontrado coordenadas para Punto A", Toast.LENGTH_LONG).show();
            RegistroConexiones.error(emailActual(), "GEOCODER", "No coords Punto A");
        });
    }

    /**
     * Valida que la provincia y el municipio seleccionados por el usuario sean correctos.
     *
     * @param prov nombre de la provincia seleccionada
     * @param mun nombre del municipio seleccionado
     * @param esPuntoA indica si la validación corresponde al Punto A (true) o al Punto B (false)
     * @return {@code true} si la provincia y el municipio son válidos,
     *         {@code false} en caso contrario
     */
    private boolean validarSeleccion(String prov, String mun, boolean esPuntoA) {

        // Comprueba si la provincia introducida existe en el mapa nombre
        if (!provinciaNombreToCpro.containsKey(prov)) {

            // Muestra un mensaje de error distinto según sea Punto A o Punto B
            Toast.makeText(this,
                    esPuntoA ? "Selecciona una provincia válida en Punto A"
                            : "Selecciona una provincia válida en Punto B",
                    Toast.LENGTH_LONG).show();

            // Registra el error de validación
            RegistroConexiones.error(
                    emailActual(),
                    "VALIDACION",
                    "Provincia inválida: " + prov
            );

            // Devuelve el foco al AutoComplete de provincia correspondiente
            if (esPuntoA) {
                spProvA.requestFocus();
                spProvA.showDropDown();
            } else {
                spProvB.requestFocus();
                spProvB.showDropDown();
            }

            return false; // La validación falla
        }

        // Selecciona la lista de municipios según el punto A o B
        List<String> listaMun = esPuntoA ? municipiosA : municipiosB;

        // Comprueba si el municipio pertenece a la lista cargada
        if (!listaMun.contains(mun)) {

            // Muestra un mensaje de error según el punto
            Toast.makeText(this,
                    esPuntoA ? "Selecciona un municipio válido en Punto A"
                            : "Selecciona un municipio válido en Punto B",
                    Toast.LENGTH_LONG).show();

            // Registra el error de validación
            RegistroConexiones.error(
                    emailActual(),
                    "VALIDACION",
                    "Municipio inválido: " + mun
            );

            // Devuelve el foco al AutoComplete de municipio correspondiente
            if (esPuntoA) {
                spMunA.requestFocus();
                spMunA.showDropDown();
            } else {
                spMunB.requestFocus();
                spMunB.showDropDown();
            }

            return false; // La validación falla
        }

        // Si provincia y municipio son válidos, la validación es correcta
        return true;
    }


    /**
     *Método para cargar  la previsión meteorológica de dos ubicaciones distintas
     *
     * @param latA latitud del Punto A
     * @param lonA longitud del Punto A
     * @param latB latitud del Punto B
     * @param lonB longitud del Punto B
     * @param puntoA descripción del Punto A (municipio y provincia)
     * @param puntoB descripción del Punto B (municipio y provincia)
     * @param fechaInicio fecha de inicio de la previsión
     */
    private void cargarTiempoDosPuntos(double latA, double lonA,double latB, double lonB,String puntoA, String puntoB,String fechaInicio) {

        final JSONObject[] jsonA = new JSONObject[1];
        final JSONObject[] jsonB = new JSONObject[1];

        cargarTiempoOpenMeteo(latA, lonA,
                respA -> {
                    jsonA[0] = respA;
                    if (jsonB[0] != null) {
                        RegistroConexiones.ok(emailActual(), "COMPARATIVA");
                        abrirResultados(puntoA, puntoB, fechaInicio, jsonA[0], jsonB[0]);
                    }
                },
                err -> {
                    Toast.makeText(this, "Error tiempo Punto A", Toast.LENGTH_LONG).show();
                    RegistroConexiones.error(emailActual(), "OPEN_METEO", err.toString());
                }
        );

        cargarTiempoOpenMeteo(latB, lonB,
                respB -> {
                    jsonB[0] = respB;
                    if (jsonA[0] != null) {
                        RegistroConexiones.ok(emailActual(), "COMPARATIVA");
                        abrirResultados(puntoA, puntoB, fechaInicio, jsonA[0], jsonB[0]);
                    }
                },
                err -> {
                    Toast.makeText(this, "Error tiempo Punto B", Toast.LENGTH_LONG).show();
                    RegistroConexiones.error(emailActual(), "OPEN_METEO", err.toString());
                }
        );
    }

    /**
     * Método que abre  la actividad de resultados mostrando la comparativa meteorológica.
     *
     * @param puntoA descripción del Punto A
     * @param puntoB descripción del Punto B
     * @param fechaInicio fecha de inicio de la comparativa
     * @param jsonA datos meteorológicos del Punto A en formato JSON
     * @param jsonB datos meteorológicos del Punto B en formato JSON
     */
    private void abrirResultados(String puntoA, String puntoB,String fechaInicio,JSONObject jsonA, JSONObject jsonB) {

        Intent i = new Intent(this, ResultadosActivity.class);
        i.putExtra("puntoA", puntoA);
        i.putExtra("puntoB", puntoB);
        i.putExtra("fechaInicio", fechaInicio);
        i.putExtra("jsonA", jsonA.toString());
        i.putExtra("jsonB", jsonB.toString());
        startActivity(i);
    }

    /**
     * Método que realiza una petición HTTP a la API Open-Meteo para obtener la previsión
     * meteorológica de una localización concreta.
     *
     * @param lat  latitud de la localización
     * @param lon  longitud de la localización
     * @param ok   callback ejecutado cuando la petición se completa correctamente
     * @param fail callback ejecutado cuando ocurre un error en la petición
     */
    private void cargarTiempoOpenMeteo(double lat, double lon,Response.Listener<JSONObject> ok,Response.ErrorListener fail) {

        String url = buildOpenMeteoUrl(lat, lon);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                ok,
                fail
        );

        queue.add(req);
    }

}
