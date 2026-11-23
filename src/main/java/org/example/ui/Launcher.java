package org.example.ui;

import javafx.application.Application;

/**

 * Punto de entrada de la aplicación. No extiende Application para que JavaFX

 * pueda arrancar desde un JAR sin módulos.

 */

public final class Launcher {

    private Launcher() {

    }

    /**

     * @param args argumentos de la línea de comandos (no se usan)

     */

    public static void main(String[] args) {

        Application.launch(JobTrackrApp.class, args);

    }

}
