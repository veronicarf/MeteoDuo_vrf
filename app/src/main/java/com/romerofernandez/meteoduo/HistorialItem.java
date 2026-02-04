package com.romerofernandez.meteoduo;



/**
 * Clase  que representa una consulta guardada en el historial.
 */
public class HistorialItem {

    /** Texto  del Punto A  */
    public String puntoA;

    /** Texto  del Punto B  */
    public String puntoB;

    /** Fecha en la que se realizó la consulta  */
    public String fecha;

    /** Respuesta JSON completa de Open-Meteo para el Punto A */
    public String jsonA;

    /** Respuesta JSON completa de Open-Meteo para el Punto B */
    public String jsonB;

    /**
     * Constructor de la clase HistorialItem.
     *
     * Inicia una entrada del historial con todos los datos
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
