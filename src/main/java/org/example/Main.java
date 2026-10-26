package org.example;

import org.example.cli.CliRunner;
import org.example.exception.TrackerException;
import org.example.repository.CandidaturaRepository;
import org.example.repository.impl.CandidaturaFileRepository;
import org.example.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de entrada de la aplicación Tracker de Candidaturas Laborales (v1.0).
 *
 * <p>Instancia las capas de repositorio, servicio y CLI, delega en
 * {@link CliRunner} los argumentos recibidos y garantiza que cualquier error
 * se muestre de forma amigable, sin trazas de excepción para el usuario.</p>
 *
 * @author Julian Navarro Moya
 * @version 1.0
 */
public class Main {

    /**
     * Constructor privado: la clase solo expone el método {@code main}.
     */
    private Main() {
    }

    /**
     * Ejecuta la aplicación de línea de comandos.
     *
     * @param args comando y opciones introducidos por el usuario
     */
    public static void main(String[] args) {
        configurarSalidaUtf8();
        try {
            CandidaturaRepository repository = new CandidaturaFileRepository();
            CandidaturaService service = new CandidaturaService(repository);
            CliRunner cliRunner = new CliRunner(service);

            int codigoSalida = cliRunner.run(args);
            if (codigoSalida != 0) {
                System.exit(codigoSalida);
            }
        } catch (TrackerException e) {
            System.out.println("[ERROR] " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            String detalle = (e.getMessage() == null || e.getMessage().isBlank())
                    ? e.getClass().getSimpleName()
                    : e.getMessage();
            System.out.println("[ERROR] Ha ocurrido un error inesperado: " + detalle);
            System.exit(1);
        }
    }

    /**
     * Fuerza la salida de consola en UTF-8 para que las tildes y la eñe se
     * muestren correctamente.
     */
    private static void configurarSalidaUtf8() {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }
}
