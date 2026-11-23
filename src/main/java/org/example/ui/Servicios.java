package org.example.ui;

import org.example.config.ConfiguracionBD;
import org.example.repository.jdbc.EmpresaJdbcDAO;
import org.example.repository.jdbc.OfertaJdbcDAO;
import org.example.service.OfertaService;
import org.example.service.impl.OfertaServiceImpl;

/**
 * Punto único donde la interfaz obtiene los servicios.
 */
public final class Servicios {

    private static final ConfiguracionBD CONFIG = new ConfiguracionBD();
    private static final OfertaService OFERTAS = new OfertaServiceImpl(
            new OfertaJdbcDAO(CONFIG), new EmpresaJdbcDAO(CONFIG));

    private Servicios() {
    }

    /**
     * @return servicio de ofertas usado por la interfaz
     */
    public static OfertaService ofertas() {
        return OFERTAS;
    }
}