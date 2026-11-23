package org.example.ui;

import org.example.config.ConfiguracionBD;
import org.example.repository.jdbc.EmpresaJdbcDAO;
import org.example.repository.jdbc.OfertaJdbcDAO;
import org.example.service.EmpresaService;
import org.example.service.OfertaService;
import org.example.service.impl.EmpresaServiceImpl;
import org.example.service.impl.OfertaServiceImpl;

/**
 * Punto único donde la interfaz obtiene los servicios.
 *
 * <p>La creación es perezosa para que un fallo de configuración o de conexión
 * se pueda mostrar como un mensaje amigable al arrancar la ventana.</p>
 */
public final class Servicios {

    private static ConfiguracionBD configuracion;
    private static OfertaService ofertas;
    private static EmpresaService empresas;

    private Servicios() {
    }

    /**
     * @return servicio de ofertas usado por la interfaz
     */
    public static synchronized OfertaService ofertas() {
        if (ofertas == null) {
            ConfiguracionBD config = configuracion();
            ofertas = new OfertaServiceImpl(new OfertaJdbcDAO(config), new EmpresaJdbcDAO(config));
        }
        return ofertas;
    }

    /**
     * @return servicio de empresas usado por la interfaz
     */
    public static synchronized EmpresaService empresas() {
        if (empresas == null) {
            empresas = new EmpresaServiceImpl(new EmpresaJdbcDAO(configuracion()));
        }
        return empresas;
    }

    private static ConfiguracionBD configuracion() {
        if (configuracion == null) {
            configuracion = new ConfiguracionBD();
        }
        return configuracion;
    }
}
