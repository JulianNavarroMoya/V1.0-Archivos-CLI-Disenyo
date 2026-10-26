package org.example.exception;

/**
 * Excepción de dominio utilizada para comunicar errores controlados al usuario
 * de la aplicación.
 *
 * <p>Se lanza ante situaciones como argumentos incompletos, identificadores no
 * numéricos, fechas con formato incorrecto, ficheros de datos corruptos o
 * recursos inexistentes. El mensaje transportado está pensado para mostrarse
 * directamente por consola, en español y sin trazas técnicas.</p>
 *
 * @author Julian Navarro Moya
 * @version 1.0
 */
public class TrackerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea una excepción con un mensaje amigable para el usuario.
     *
     * @param mensaje descripción del error en español
     */
    public TrackerException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepción con un mensaje amigable y conserva la causa técnica
     * original para facilitar las labores de depuración.
     *
     * @param mensaje descripción del error en español
     * @param causa   excepción original que provocó el fallo
     */
    public TrackerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
