package org.example.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
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
            escenario.show();
        } catch (RuntimeException | LinkageError e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje(e));
            alerta.setTitle("Error de inicio");
            alerta.setHeaderText("No se pudo iniciar JobTrackr");
            alerta.showAndWait();
            Platform.exit();
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
