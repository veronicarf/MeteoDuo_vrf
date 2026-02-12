package com.romerofernandez.meteoduo;

public class AppLog {
    public String id;
    public String dia;
    public String hora;
    public String usuario;
    public String tipo;         // LOGIN, GEOAPI, GEOCODER, OPEN_METEO, FIRESTORE...
    public String detalleError; // vacío => correcto

    public AppLog(String id, String dia, String hora, String usuario, String tipo, String detalleError) {
        this.id = id;
        this.dia = dia;
        this.hora = hora;
        this.usuario = usuario;
        this.tipo = tipo;
        this.detalleError = detalleError;
    }

    public boolean esError() {
        return detalleError != null && !detalleError.trim().isEmpty();
    }
}
