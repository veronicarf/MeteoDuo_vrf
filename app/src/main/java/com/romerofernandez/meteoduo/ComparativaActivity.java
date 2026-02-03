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
 * Activity encargada de la comparación de localizaciones.
 *
 * En esta pantalla se permite seleccionar dos puntos distintos (A y B),
 * eligiendo una provincia y posteriormente un municipio mediante campos
 * de autocompletado. Los datos se obtienen desde la API GeoAPI utilizando
 * peticiones HTTP gestionadas con la librería Volley.
 */
public class ComparativaActivity extends AppCompatActivity {

    // -----------------------------
    // 1) COMPONENTES DE INTERFAZ
    // -----------------------------

    /**
     * Campos de autocompletado para provincias y municipios.
     * Se utilizan dos  campos, uno para el punto A y otro para el punto B.
     */
    private AutoCompleteTextView spProvA, spMunA, spProvB, spMunB;

    // -----------------------------
    // 2) RED (VOLLEY)
    // -----------------------------

    /**
     * Cola de peticiones de red utilizada por Volley.
     * En ella se encolan todas las peticiones HTTP que realiza la aplicación.
     */
    private RequestQueue queue;

    // -----------------------------
    // 3) DATOS DE PROVINCIAS
    // -----------------------------

    /**
     * Lista con los nombres de las provincias obtenidas de la API.
     * Se utiliza como fuente de datos para el autocompletado.
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
     * Adapters utilizados por los AutoCompleteTextView
     * para mostrar provincias y municipios.
     */
    private ArrayAdapter<String> adapterProv;
    private ArrayAdapter<String> adapterMunA;
    private ArrayAdapter<String> adapterMunB;

    // -----------------------------
    // 6) CONFIGURACIÓN GEOAPI
    // -----------------------------

    /**
     * URL base de la API GeoAPI utilizada para realizar las peticiones.
     */
    private static final String BASE = "https://apiv1.geoapi.es";

    /**
     * Clave de autenticación necesaria para acceder a GeoAPI.
     */
    private static final String KEY ="1fc368b23d25818cdd878134ec20df90e9e67a7a38481c4e395d0a6d56f99abc";

    /**
     * Parámetros comunes a todas las peticiones:
     */
    private static final String COMMON_PARAMS ="FORMAT=json&PAGE_SIZE=1000&KEY=" + KEY;

    // -----------------------------
    // 7) OTROS
    // -----------------------------

    /**
     * Variables auxiliares utilizadas para evitar recargar municipios
     * si el texto de la provincia no ha cambiado.
     */
    private String ultimaProvA = "";
    private String ultimaProvB = "";



    /**
     * Método principal de inicialización de la Activity.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa);

        // Inicialización de la cola de peticiones de Volley
        queue = Volley.newRequestQueue(this);

        // Enlazado con los elementos definidos en el layout XML
        spProvA = findViewById(R.id.autoCompleteTextView);
        spMunA  = findViewById(R.id.autoCompleteTextView3);
        spProvB = findViewById(R.id.autoCompleteTextView2);
        spMunB  = findViewById(R.id.autoCompleteTextView4);

        //Botón buscar
        Button btnBuscar = findViewById(R.id.btnCrearCuenta2);


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

        // Configuración del autocompletado
        autoShowDropdown(spProvA);
        autoShowDropdown(spProvB);
        autoShowDropdown(spMunA);
        autoShowDropdown(spMunB);

        // Vincula provincias con municipios aunque no se pulse una sugerencia
        vincularProvinciaConMunicipios(spProvA, spMunA, true);
        vincularProvinciaConMunicipios(spProvB, spMunB, false);

        // Carga los municipios al seleccionar una provincia del desplegable
        spProvA.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvA.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, true);
        });

        spProvB.setOnItemClickListener((parent, view, position, id) -> {
            String provSeleccionada = spProvB.getText().toString().trim();
            cargarMunicipiosSiProvinciaValida(provSeleccionada, false);
        });

        //  BOTÓN BUSCAR: valida y abre ResultadosActivity
        btnBuscar.setOnClickListener(v -> {

            String provA = spProvA.getText().toString().trim();
            String munA  = spMunA.getText().toString().trim();
            String provB = spProvB.getText().toString().trim();
            String munB  = spMunB.getText().toString().trim();

            // Validación provincias

            if (!provinciaNombreToCpro.containsKey(provA)) {
                Toast.makeText(this, "Selecciona una provincia válida en Punto A", Toast.LENGTH_LONG).show();
                spProvA.requestFocus();
                spProvA.showDropDown();
                return;
            }
            if (!provinciaNombreToCpro.containsKey(provB)) {
                Toast.makeText(this, "Selecciona una provincia válida en Punto B", Toast.LENGTH_LONG).show();
                spProvB.requestFocus();
                spProvB.showDropDown();
                return;
            }

            // Validación municipios

            if (!municipiosA.contains(munA)) {
                Toast.makeText(this, "Selecciona un municipio válido en Punto A", Toast.LENGTH_LONG).show();
                spMunA.requestFocus();
                spMunA.showDropDown();
                return;
            }
            if (!municipiosB.contains(munB)) {
                Toast.makeText(this, "Selecciona un municipio válido en Punto B", Toast.LENGTH_LONG).show();
                spMunB.requestFocus();
                spMunB.showDropDown();
                return;
            }

            String puntoA = munA + " (" + provA + ")";
            String puntoB = munB + " (" + provB + ")";

            // Fecha inicio , es decir, hoy

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaInicio = sdf.format(Calendar.getInstance().getTime());

            Intent i = new Intent(ComparativaActivity.this, ResultadosActivity.class);
            i.putExtra("puntoA", puntoA);
            i.putExtra("puntoB", puntoB);
            i.putExtra("fechaInicio", fechaInicio);
            startActivity(i);
        });
        // Carga inicial de provincias al abrir la pantalla
        cargarProvincias();
    }

    /**
     * Configura un AutoCompleteTextView para que muestre el desplegable
     * automáticamente al obtener foco, hacer click o escribir.
     *
     * @param actv campo de autocompletado
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
     * Vincula el campo de provincia con su correspondiente campo de municipios.
     * Cuando el texto coincide exactamente con una provincia ,
     * se cargan automáticamente sus municipios.
     *
     * @param prov campo de provincia
     * @param mun campo de municipio asociado
     * @param esPuntoA indica si se trata del punto A o del punto B
     */
    private void vincularProvinciaConMunicipios(AutoCompleteTextView prov,AutoCompleteTextView mun,boolean esPuntoA) {

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
     * Limpia la lista de municipios del punto indicado.
     *
     * @param esPuntoA true si corresponde al punto A, false para el punto B
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
     * Comprueba si la provincia existe en el mapa y, en ese caso,
     * obtiene su código y carga los municipios correspondientes.
     *
     * @param nombreProvincia nombre de la provincia
     * @param esPuntoA indica si se trata del punto A o del punto B
     */
    private void cargarMunicipiosSiProvinciaValida(String nombreProvincia, boolean esPuntoA) {
        String cpro = provinciaNombreToCpro.get(nombreProvincia);
        if (cpro != null) cargarMunicipios(cpro, esPuntoA);
    }

    /**
     * Realiza una petición a GeoAPI para obtener la lista de provincias
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
     * Realiza una petición a GeoAPI para obtener los municipios de una provincia
     * concreta a partir de su código (CPRO).
     *
     * @param cpro código de la provincia
     * @param esPuntoA indica si los datos son para el punto A o B
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
     * El texto se mostrará con la primera letra en mayúscula
     *
     * @param s texto original
     * @return texto capitalizado
     */
    private String Mayuscula(String s) {
        if (s == null || s.isEmpty()) return "";
        s = s.toLowerCase().trim();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
