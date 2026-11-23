package org.example.ui;

import org.example.service.OfertaService;

/**
 * Punto único donde la interfaz obtiene los servicios. Aquí se cambia el servicio de
 * prueba por la implementación real cuando esté integrada.
 */
public final class Servicios {

    private static final OfertaService OFERTAS = new OfertaServicioFalso();

    private Servicios() {
    }

    /**
     * @return servicio de ofertas usado por la interfaz
     */
    public static OfertaService ofertas() {
        return OFERTAS;
    }
}