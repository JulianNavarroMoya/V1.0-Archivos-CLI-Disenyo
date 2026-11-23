package org.example.service.impl;
import org.example.exception.TrackerException;
/**
 * Comprobaciones reutilizadas por los servicios.
 */
final class Validaciones {
    private Validaciones() {
    }
    /**
     * @return el valor sin espacios; lanza error si está vacío
     */
    static String obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new TrackerException(mensaje);
        }
        return valor.trim();
    }
    /**
     * @return el valor sin espacios, o {@code null} si está vacío
     */
    static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
    /**
     * Comprueba que la URL sea http o https, si se ha indicado.
     */
    static void urlValida(String url, String mensaje) {
        if (url != null && !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new TrackerException(mensaje);
        }
    }
}
