package com.romerofernandez.meteoduo;



/**
 * Clase  que representa una consulta guardada en el historial.
 *
 *  @author Verónica Romero
 */
public class HistorialItem {

    // ==============================
// DATOS DE LA CONSULTA
// ==============================

    /** Nombre del municipio seleccionado como Punto A en la comparativa. */
    public String puntoA;

    /** Nombre del municipio seleccionado como Punto B en la comparativa. */
    public String puntoB;

    /** Fecha en la que se realizó la consulta comparativa. */
    public String fecha;

    /** Respuesta JSON completa obtenida de Open-Meteo para el Punto A. */
    public String jsonA;

    /** Respuesta JSON completa obtenida de Open-Meteo para el Punto B. */
    public String jsonB;

    /**
     * Constructor de la clase HistorialItem.
     *
     * @param puntoA Texto del Punto A (municipio y provincia)
     * @param puntoB Texto del Punto B (municipio y provincia)
     * @param fecha  Fecha de la consulta
     * @param jsonA  JSON con los datos meteorológicos del Punto A
     * @param jsonB  JSON con los datos meteorológicos del Punto B
     */
    public HistorialItem(String puntoA, String puntoB, String fecha,
                         String jsonA, String jsonB) {
        this.puntoA = puntoA;
        this.puntoB = puntoB;
        this.fecha = fecha;
        this.jsonA = jsonA;
        this.jsonB = jsonB;
    }
}
