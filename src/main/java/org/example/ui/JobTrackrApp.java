package org.example.ui;
import javafx.application.Application;
import javafx.stage.Stage;
/**
 * Ventana principal de JobTrackr. Carga la primera pantalla con {@link Navegador}.
 */
public class JobTrackrApp extends Application {
    @Override
    public void start(Stage escenario) {
        Navegador.inicializar(escenario);
        Navegador.ir("listado-ofertas");
        escenario.setTitle("JobTrackr");
        escenario.show();
    }
}
