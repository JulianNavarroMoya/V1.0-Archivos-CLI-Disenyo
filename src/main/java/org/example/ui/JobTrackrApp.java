package org.example.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Ventana principal de JobTrackr. Carga la primera pantalla con {@link Navegador}.
 */
public class JobTrackrApp extends Application {

    @Override
    public void start(Stage escenario) {
        try {
            Navegador.inicializar(escenario);
            Navegador.ir("listado-ofertas");
            escenario.setTitle("JobTrackr");
            ajustarAlTamanoPantalla(escenario);
            escenario.show();
        } catch (RuntimeException | LinkageError e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje(e));
            alerta.setTitle("Error de inicio");
            alerta.setHeaderText("No se pudo iniciar JobTrackr");
            alerta.setResizable(true);
            alerta.showAndWait();
            Platform.exit();
        }
    }

    /**
     * Reduce la ventana si su tamaño preferido no cabe en la pantalla.
     */
    private static void ajustarAlTamanoPantalla(Stage escenario) {
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        if (escenario.getScene() != null) {
            escenario.setWidth(Math.min(escenario.getScene().getWidth(), pantalla.getWidth() * 0.95));
            escenario.setHeight(Math.min(escenario.getScene().getHeight(), pantalla.getHeight() * 0.95));
        }
    }

    private static String mensaje(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        String texto = causa.getMessage();
        return texto == null || texto.isBlank() ? causa.getClass().getSimpleName() : texto;
    }
}
