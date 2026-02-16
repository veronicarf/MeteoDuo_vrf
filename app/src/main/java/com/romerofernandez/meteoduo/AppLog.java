package com.romerofernandez.meteoduo;

/**
 * Clase AppLog para guardar datos de los errores obtenidos
 *
 *  @author Verónica Romero
 */
public class AppLog {

    /** Identificador único del documento almacenado en Firestore. */
    public String id;

    /** Día en el que se registró el evento. */
    public String dia;

    /** Hora exacta en la que ocurrió el evento. */
    public String hora;

    /** Usuario asociado al evento registrado. */
    public String usuario;

    /** Tipo de evento registrado (ej. consulta, error, acción del sistema). */
    public String tipo;

    /** Descripción detallada del error en caso de que el evento sea de tipo error. */
    public String detalleError;


    /**
     * Constructor de la clase AppLog.
     *
     * @param id identificador del documento en Firestore
     * @param dia día en el que ocurrió el evento
     * @param hora hora en la que ocurrió el evento
     * @param usuario usuario asociado al evento
     * @param tipo tipo de evento registrado
     * @param detalleError mensaje de error (vacío si no hubo error)
     */
    public AppLog(String id, String dia, String hora, String usuario, String tipo, String detalleError) {
        this.id = id;                   // Asigna el id del documento
        this.dia = dia;                 // Asigna el día del evento
        this.hora = hora;               // Asigna la hora del evento
        this.usuario = usuario;         // Asigna el usuario
        this.tipo = tipo;               // Asigna el tipo de evento
        this.detalleError = detalleError; // Asigna el detalle del error (si existe)
    }

    /**
     * Método que indica si el registro representa un error.
     *
     * @return true si hay un mensaje de error, false si el evento es correcto
     */
    public boolean esError() {
        // Devuelve true si detalleError no es null y no está vacío
        return detalleError != null && !detalleError.trim().isEmpty();
    }
}
