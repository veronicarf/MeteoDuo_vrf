package com.romerofernandez.meteoduo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



/**
 * Activity encargada de mostrar la comparativa meteorológica entre dos puntos (A y B)
  */
public class ResultadosActivity extends AppCompatActivity {


    //Valores por defecto
    private boolean mostrarViento = true;
    private boolean mostrarCielo = true;
    private boolean mostrarLluvia = true;
    private String unidadTemp = "C";

//Varible UI

    private TextView tvPuntos;
    private TextView tvRango;
    private LinearLayout contenedorDias;
    private Button btnGuardar;
    private Button btnVolver;
    private ProgressBar pbAjustes;



  //UID del usuario autenticado
    private String uid = "guest";

  //Número de días a mostrar en la comparativa.
    private static final int NUM_DIAS = 3;

    /**
     * Método de inicialización de la Activity.
     * Se ejecuta cuando la pantalla es creada por primera vez.
     *
     * @param savedInstanceState estado previo de la Activity si existiera
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        initViews();             // Enlaza los componentes del layout
        configurarBotones();     // Configura listeners de botones

        // Obtiene el UID del usuario actual (o "guest" si no hay sesión)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = (user != null) ? user.getUid() : "guest";

        // Primero carga ajustes y después pinta la pantalla con esos ajustes
        cargarAjustesYLuegoPintarResultados(this::pintarResultados);
    }

    /**
     * Enlaza los componentes del layout con las variables de la clase.
     */
    private void initViews() {
        tvPuntos = findViewById(R.id.tvPuntos);
        tvRango = findViewById(R.id.tvRango);
        contenedorDias = findViewById(R.id.contenedorDias);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVolver = findViewById(R.id.btnVolver);
        pbAjustes = findViewById(R.id.pbAjustes);
    }

    /**
     * Método que configura los listeners principales de la pantalla:
     */
    private void configurarBotones() {
        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarEnHistorial());
    }


    /**
     * Carga ajustes del usuario desde Firestore y, al finalizar muestra los resultados
     *
     * @param pintarResultados acción que pinta los resultados una vez listos los ajustes
     */
    private void cargarAjustesYLuegoPintarResultados(@NonNull Runnable pintarResultados) {

        // Muestra el ProgressBar mientras se cargan ajustes
        if (pbAjustes != null) pbAjustes.setVisibility(View.VISIBLE);

        // Si no hay usuario, no se consultan ajustes
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (pbAjustes != null) pbAjustes.setVisibility(View.GONE);
            pintarResultados.run();
            return;
        }

        // Consulta ajustes del usuario en Firestore
        FirebaseFirestore.getInstance()
                .collection("ajustes")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    // Si existe el documento, aplica ajustes
                    if (doc != null && doc.exists()) {

                        Boolean v = doc.getBoolean("mostrarViento");
                        Boolean c = doc.getBoolean("mostrarCielo");
                        Boolean l = doc.getBoolean("mostrarLluvia");
                        String unidad = doc.getString("unidadTemp");

                        // Aplica booleanos  true
                        mostrarViento = (v == null) ? true : v;
                        mostrarCielo  = (c == null) ? true : c;
                        mostrarLluvia = (l == null) ? true : l;

                        // Aplica unidad de temperatura
                        if (unidad != null && unidad.equalsIgnoreCase("F")) {
                            unidadTemp = "F";
                        } else {
                            unidadTemp = "C";
                        }
                    }

                    //pinta resultados
                    if (pbAjustes != null) pbAjustes.setVisibility(View.GONE);
                    pintarResultados.run();
                })
                .addOnFailureListener(e -> {
                    // Si falla Firestore, se pinta con defaults
                    if (pbAjustes != null) pbAjustes.setVisibility(View.GONE);
                    pintarResultados.run();
                });
    }


    /**
     * Pinta en pantalla la comparativa meteorológica
     */
    private void pintarResultados() {

        // 1) Puntos
        String puntoA = getIntent().getStringExtra("puntoA");
        String puntoB = getIntent().getStringExtra("puntoB");
        if (puntoA == null) puntoA = "Punto A";
        if (puntoB == null) puntoB = "Punto B";
        tvPuntos.setText(puntoA + " - " + puntoB);

        // 2) Fechas
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calInicio = getCalendarInicio(sdf);
        setRangoEnPantalla(sdf, calInicio);

        // 3) JSONs
        String jsonAStr = getIntent().getStringExtra("jsonA");
        String jsonBStr = getIntent().getStringExtra("jsonB");

        if (jsonAStr == null || jsonBStr == null) {
            Toast.makeText(this, "No han llegado datos (jsonA/jsonB).", Toast.LENGTH_LONG).show();
            return;
        }

        DailyData dataA;
        DailyData dataB;

        // Parsea la respuesta de Open-Meteo
        try {
            dataA = parseOpenMeteoDaily(new JSONObject(jsonAStr), NUM_DIAS);
            dataB = parseOpenMeteoDaily(new JSONObject(jsonBStr), NUM_DIAS);
        } catch (Exception e) {
            Toast.makeText(this, "Error leyendo datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // 4) Pinta días
        contenedorDias.removeAllViews();
        Calendar calIter = (Calendar) calInicio.clone();

        for (int i = 0; i < NUM_DIAS; i++) {

            String fecha = sdf.format(calIter.getTime());

            // Infla el bloque de un día y lo añade al contenedor
            View bloqueDia = getLayoutInflater()
                    .inflate(R.layout.item_resultados_dia, contenedorDias, false);

            // Pinta fecha
            TextView tvFecha = bloqueDia.findViewById(R.id.tvFecha);
            tvFecha.setText(fecha);

            // Pinta cabeceras con nombres de puntos
            TextView tvHeadA = bloqueDia.findViewById(R.id.tvHeadA);
            TextView tvHeadB = bloqueDia.findViewById(R.id.tvHeadB);
            tvHeadA.setText(puntoA);
            tvHeadB.setText(puntoB);

            // Filas opcionales según ajustes
            View rowViento = bloqueDia.findViewById(R.id.rowViento);
            View rowCielo  = bloqueDia.findViewById(R.id.rowCielo);
            View rowPrec   = bloqueDia.findViewById(R.id.rowPrecipitacion);

            if (rowViento != null) rowViento.setVisibility(mostrarViento ? View.VISIBLE : View.GONE);
            if (rowCielo != null)  rowCielo.setVisibility(mostrarCielo ? View.VISIBLE : View.GONE);
            if (rowPrec != null)   rowPrec.setVisibility(mostrarLluvia ? View.VISIBLE : View.GONE);

            // TextViews de datos
            TextView aMax  = bloqueDia.findViewById(R.id.tvA_TempMax);
            TextView bMax  = bloqueDia.findViewById(R.id.tvB_TempMax);
            TextView aMin  = bloqueDia.findViewById(R.id.tvA_TempMin);
            TextView bMin  = bloqueDia.findViewById(R.id.tvB_TempMin);

            TextView aWind = bloqueDia.findViewById(R.id.tvA_Viento);
            TextView bWind = bloqueDia.findViewById(R.id.tvB_Viento);

            TextView aPrec = bloqueDia.findViewById(R.id.tvA_Prec);
            TextView bPrec = bloqueDia.findViewById(R.id.tvB_Prec);

            TextView aSky  = bloqueDia.findViewById(R.id.tvA_Cielo);
            TextView bSky  = bloqueDia.findViewById(R.id.tvB_Cielo);

            // Obtiene valores
            double aMaxVal = safeDouble(dataA.tempMax, i, Double.NaN);
            double aMinVal = safeDouble(dataA.tempMin, i, Double.NaN);
            double bMaxVal = safeDouble(dataB.tempMax, i, Double.NaN);
            double bMinVal = safeDouble(dataB.tempMin, i, Double.NaN);

            double aWindVal = safeDouble(dataA.windMax, i, Double.NaN);
            double bWindVal = safeDouble(dataB.windMax, i, Double.NaN);

            double aPrecVal = safeDouble(dataA.precipProbMax, i, Double.NaN);
            double bPrecVal = safeDouble(dataB.precipProbMax, i, Double.NaN);

            int aCodeVal = safeInt(dataA.weatherCode, i, -1);
            int bCodeVal = safeInt(dataB.weatherCode, i, -1);

            // Pinta valores en pantalla
            aMax.setText(Double.isNaN(aMaxVal) ? "—" : formatoTemp(aMaxVal, unidadTemp));
            aMin.setText(Double.isNaN(aMinVal) ? "—" : formatoTemp(aMinVal, unidadTemp));
            bMax.setText(Double.isNaN(bMaxVal) ? "—" : formatoTemp(bMaxVal, unidadTemp));
            bMin.setText(Double.isNaN(bMinVal) ? "—" : formatoTemp(bMinVal, unidadTemp));

            aWind.setText(Double.isNaN(aWindVal) ? "—" : String.format(Locale.getDefault(), "%.0f km/h", aWindVal));
            bWind.setText(Double.isNaN(bWindVal) ? "—" : String.format(Locale.getDefault(), "%.0f km/h", bWindVal));

            aPrec.setText(Double.isNaN(aPrecVal) ? "—" : String.format(Locale.getDefault(), "%.0f %%", aPrecVal));
            bPrec.setText(Double.isNaN(bPrecVal) ? "—" : String.format(Locale.getDefault(), "%.0f %%", bPrecVal));

            aSky.setText(aCodeVal == -1 ? "—" : skyFromCode(aCodeVal));
            bSky.setText(bCodeVal == -1 ? "—" : skyFromCode(bCodeVal));

            // Añade el bloque del día al contenedor y avanza fecha
            contenedorDias.addView(bloqueDia);
            calIter.add(Calendar.DAY_OF_MONTH, 1);
        }
    }


    /**
     * Obtiene el calendario de inicio
     *
     * @param sdf formato usado para parsear la fecha (dd/MM/yyyy)
     * @return Calendar con la fecha de inicio
     */
    private Calendar getCalendarInicio(SimpleDateFormat sdf) {
        String fechaInicio = getIntent().getStringExtra("fechaInicio");
        Calendar cal = Calendar.getInstance();

        try {
            if (fechaInicio != null && !fechaInicio.trim().isEmpty()) {
                Date d = sdf.parse(fechaInicio.trim());
                if (d != null) cal.setTime(d);
            }
        } catch (Exception ignored) {
            // Si falla el parseo, se mantiene la fecha actual
        }

        return cal;
    }

    /**
     * Método para mostrar  el rango de fechas en pantalla en función del inicio
     *
     * @param sdf formato para mostrar fechas
     * @param calInicio calendario con la fecha de inicio
     */
    private void setRangoEnPantalla(SimpleDateFormat sdf, Calendar calInicio) {
        String inicioStr = sdf.format(calInicio.getTime());
        Calendar calFin = (Calendar) calInicio.clone();
        calFin.add(Calendar.DAY_OF_MONTH, NUM_DIAS - 1);
        String finStr = sdf.format(calFin.getTime());
        tvRango.setText("días: " + inicioStr + " a " + finStr);
    }

    /**
     * Método que formatea una temperatura en Celsius o Fahrenheit según la unidad indicada.
     *
     * @param celsius temperatura
     *
     * @return texto formateado
     */
    private String formatoTemp(double celsius, String unit) {
        if ("F".equalsIgnoreCase(unit)) {
            double f = (celsius * 9.0 / 5.0) + 32.0;
            return String.format(Locale.getDefault(), "%.0f °F", f);
        }
        return String.format(Locale.getDefault(), "%.0f °C", celsius);
    }

    /**
     * Traduce el código meteorológico de Open-Meteo
     *
     * @param code código meteorológico
     * @return descripción textual del estado del cielo
     */
    private String skyFromCode(int code) {
        if (code == 0) return "Despejado";
        if (code == 1 || code == 2) return "Poco nuboso";
        if (code == 3) return "Nublado";
        if (code >= 45 && code <= 48) return "Niebla";
        if (code >= 51 && code <= 67) return "Lluvia";
        if (code >= 71 && code <= 77) return "Nieve";
        if (code >= 80 && code <= 82) return "Chubascos";
        if (code >= 95) return "Tormenta";
        return "Variable";
    }

    /**
     * Obtiene un valor entero de un JSONArray de forma segura.
     *
     * @param arr array JSON
     * @param index posición a leer
     * @param fallback valor por defecto si falla la lectura
     * @return valor leído o {@code fallback} si hay error
     */
    private double safeDouble(JSONArray arr, int index, double fallback) {
        try {
            if (arr == null || index < 0 || index >= arr.length()) return fallback;
            return arr.getDouble(index);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Obtiene un valor entero de un JSONArray de forma segura.
     *
     * @param arr array JSON
     * @param index posición a leer
     * @param fallback valor por defecto si falla la lectura
     * @return valor leído o {@code fallback} si hay error
     */
    private int safeInt(JSONArray arr, int index, int fallback) {
        try {
            if (arr == null || index < 0 || index >= arr.length()) return fallback;
            return arr.getInt(index);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Estructura auxiliar para almacenar arrays diarios
     */
    private static class DailyData {
        JSONArray tempMax;
        JSONArray tempMin;
        JSONArray windMax;
        JSONArray precipProbMax;
        JSONArray weatherCode;
    }

    /**
     * Lee la parte "daily" del JSON que devuelve Open-Meteo y saca los datos necesarios
     *
     * @param root    JSON raíz devuelto por Open-Meteo
     * @param numDias número mínimo de días requeridos
     * @return objeto {@link DailyData} con los arrays diarios
     * @throws Exception si faltan campos obligatorios o no hay suficientes días
     */
    private DailyData parseOpenMeteoDaily(JSONObject root, int numDias) throws Exception {

        JSONObject daily = root.getJSONObject("daily");
        DailyData out = new DailyData();

        out.tempMax = daily.getJSONArray("temperature_2m_max");
        out.tempMin = daily.getJSONArray("temperature_2m_min");
        out.precipProbMax = daily.getJSONArray("precipitation_probability_max");

        // Viento
        if (daily.has("windspeed_10m_max")) {
            out.windMax = daily.getJSONArray("windspeed_10m_max");
        } else if (daily.has("wind_speed_10m_max")) {
            out.windMax = daily.getJSONArray("wind_speed_10m_max");
        } else {
            throw new Exception("No viene el viento en el JSON");
        }

        // Weathercode
        if (daily.has("weathercode")) {
            out.weatherCode = daily.getJSONArray("weathercode");
        } else if (daily.has("weather_code")) {
            out.weatherCode = daily.getJSONArray("weather_code");
        } else {
            throw new Exception("No viene weathercode en el JSON");
        }

        // Comprueba que la API haya devuelto suficientes días
        if (out.tempMax.length() < numDias || out.tempMin.length() < numDias) {
            throw new Exception("La API no devolvió suficientes días");
        }

        return out;
    }


    /**
     * Guarda la consulta actual en el historial local del usuario.
     */
    private void guardarEnHistorial() {
        try {
            String puntoA = getIntent().getStringExtra("puntoA");
            String puntoB = getIntent().getStringExtra("puntoB");
            String fecha  = getIntent().getStringExtra("fechaInicio");
            String jsonA  = getIntent().getStringExtra("jsonA");
            String jsonB  = getIntent().getStringExtra("jsonB");

            if (puntoA == null) puntoA = "Punto A";
            if (puntoB == null) puntoB = "Punto B";
            if (fecha == null)  fecha = "";

            // Sin datos reales no se puede guardar la consulta
            if (jsonA == null || jsonB == null) {
                Toast.makeText(this, "No hay datos reales para guardar.", Toast.LENGTH_LONG).show();
                return;
            }

            // Construye el objeto a guardar en historial
            JSONObject obj = new JSONObject();
            obj.put("puntoA", puntoA);
            obj.put("puntoB", puntoB);
            obj.put("fecha", fecha);
            obj.put("jsonA", jsonA);
            obj.put("jsonB", jsonB);

            // Guarda el historial local asociado al UID
            HistorialStorage.add(this, uid, obj);

            Toast.makeText(this, "Consulta guardada", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error guardando historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
