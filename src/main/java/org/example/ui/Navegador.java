package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;

/**
 * Cambia la pantalla mostrada cargando el FXML correspondiente con FXMLLoader.
 */
public final class Navegador {

    private static final String RUTA_FXML = "/org/example/ui/fxml/";
    private static Stage escenario;

    private Navegador() {
    }

    /**
     * @param stage ventana principal de la aplicación
     */
    public static void inicializar(Stage stage) {
        escenario = Objects.requireNonNull(stage, "La ventana principal no puede ser nula");
        escenario.setMinWidth(780);
        escenario.setMinHeight(480);
    }

    /**
     * Muestra una pantalla y devuelve su controlador.
     *
     * @param vista nombre del FXML sin extensión
     * @param <C>   tipo del controlador
     * @return controlador de la pantalla cargada
     * @throws IllegalStateException si no existe el FXML
     */
    public static <C> C ir(String vista) {
        URL recurso = Navegador.class.getResource(RUTA_FXML + vista + ".fxml");
        if (recurso == null) {
            throw new IllegalStateException("No se encuentra la pantalla " + vista + ".fxml");
        }
        try {
            FXMLLoader cargador = new FXMLLoader(recurso);
            Parent raiz = cargador.load();
            escenario.setScene(new Scene(raiz));
            return cargador.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la pantalla " + vista, e);
        }
    }
}