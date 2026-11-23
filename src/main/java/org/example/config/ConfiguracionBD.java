package org.example.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Datos de conexión a MySQL. Por defecto los lee de config.properties en la raíz del proyecto.
 */
public class ConfiguracionBD {

    private static final Path FICHERO = Path.of("config.properties");

    private final String url;
    private final String usuario;
    private final String password;

    /**
     * Lee la configuración de config.properties.
     *
     * @throws IllegalStateException si el fichero no existe o le faltan claves
     */
    public ConfiguracionBD() {
        if (!Files.isRegularFile(FICHERO)) {
            throw new IllegalStateException("No se encuentra config.properties en la raíz del proyecto. "
                    + "Copia config.example.properties y rellena tus datos.");
        }
        Properties props = new Properties();
        // UTF-8 para que las contraseñas con tildes o eñes se lean bien
        try (BufferedReader lector = Files.newBufferedReader(FICHERO, StandardCharsets.UTF_8)) {
            props.load(lector);
        } catch (IOException e) {
            throw new IllegalStateException("No se puede leer config.properties.", e);
        }
        this.url = obligatoria(props, "db.url");
        this.usuario = obligatoria(props, "db.user");
        this.password = obligatoria(props, "db.password");
    }

    /**
     * Configuración explícita, útil para las pruebas con una base de datos H2.
     *
     * @param url     URL JDBC
     * @param usuario usuario de la base de datos
     * @param password contraseña
     */
    public ConfiguracionBD(String url, String usuario, String password) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    /**
     * @return nueva conexión a la base de datos; quien la pide debe cerrarla
     * @throws SQLException si no se puede conectar
     */
    public Connection abrirConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    private static String obligatoria(Properties props, String clave) {
        String valor = props.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la clave '" + clave + "' en config.properties.");
        }
        return valor.trim();
    }
}