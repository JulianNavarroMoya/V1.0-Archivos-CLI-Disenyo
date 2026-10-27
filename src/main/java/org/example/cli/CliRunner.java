package org.example.cli;

import org.example.exception.TrackerException;
import org.example.model.Candidatura;
import org.example.service.CandidaturaService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Procesador de la interfaz de linea de comandos.
 *
 * <p>Recibe los argumentos de {@code main}, normaliza el comando (con o sin
 * guion inicial), interpreta las opciones con nombre ({@code --titulo},
 * {@code --empresa}, {@code --estado}, {@code --fecha}) y presenta los
 * resultados con un formato de tabla o ficha cuidado por consola.</p>
 *
 * <p>La clase no abre ningún menú interactivo: cada ejecución procesa una
 * única orden y finaliza.</p>
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 1.0
 */
public class CliRunner {

    private static final String EJEMPLO_LISTAR =
            "listar --estado pendiente --empresa Indra";
    private static final String EJEMPLO_ANADIR =
            "anadir --titulo \"Desarrollador Backend Java\" --empresa \"Indra\" "
                    + "--estado pendiente --fecha 26/10/2026";

    private static final int ANCHO_MAX_TITULO = 38;
    private static final int ANCHO_MAX_EMPRESA = 26;
    private static final int ANCHO_MAX_ESTADO = 12;

    private final CandidaturaService service;

    /**
     * Crea el procesador CLI con el servicio de candidaturas.
     *
     * @param service servicio de negocio
     */
    public CliRunner(CandidaturaService service) {
        this.service = Objects.requireNonNull(service,
                "El servicio de candidaturas no puede ser nulo");
    }

    /**
     * Ejecuta la orden solicitada.
     *
     * @param args argumentos recibidos por {@code main}
     * @return {@code 0} si la operación terminó correctamente; {@code 1} si se
     *         produjo un error controlado
     */
    public int run(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("[ERROR] No se ha indicado ningún comando.");
            System.out.println("        Ejemplo: java -jar tracker.jar listar");
            System.out.println();
            imprimirAyuda();
            return 1;
        }

        String comando = normalizarComando(args[0]);

        try {
            switch (comando) {
                case "listar" -> ejecutarListar(args);
                case "anadir", "añadir" -> ejecutarAnadir(args);
                case "ver" -> ejecutarVer(args);
                case "borrar" -> ejecutarBorrar(args);
                case "importar" -> ejecutarImportar(args);
                case "ayuda", "help", "h", "?" -> imprimirAyuda();
                default -> {
                    System.out.println("[ERROR] Comando desconocido: '" + args[0] + "'.");
                    System.out.println("        Ejemplo: java -jar tracker.jar listar");
                    System.out.println();
                    imprimirAyuda();
                    return 1;
                }
            }
            return 0;
        } catch (TrackerException e) {
            System.out.println("[ERROR] " + e.getMessage());
            return 1;
        }
    }

    /**
     * HU-02 / HU-03: lista todas las candidaturas o aplica los filtros
     * indicados.
     *
     * @param args argumentos completos de la orden
     */
    private void ejecutarListar(String[] args) {
        Map<String, String> opciones = parsearOpciones(args, 1,
                Set.of("estado", "empresa"), EJEMPLO_LISTAR);

        boolean filtrada = !opciones.isEmpty();
        List<Candidatura> candidaturas = filtrada
                ? service.listarFiltradas(opciones.get("estado"), opciones.get("empresa"))
                : service.listarTodas();

        imprimirTabla(candidaturas, filtrada);
    }

    /**
     * HU-04: añade una candidatura a partir de las opciones de linea de
     * comandos.
     *
     * @param args argumentos completos de la orden
     */
    private void ejecutarAnadir(String[] args) {
        Map<String, String> opciones = parsearOpciones(args, 1,
                Set.of("titulo", "empresa", "estado", "fecha"), EJEMPLO_ANADIR);

        String titulo = opcionObligatoria(opciones, "titulo", EJEMPLO_ANADIR);
        String empresa = opcionObligatoria(opciones, "empresa", EJEMPLO_ANADIR);
        String estado = opcionObligatoria(opciones, "estado", EJEMPLO_ANADIR);
        String fecha = opciones.get("fecha");

        Candidatura creada = service.anadir(titulo, empresa, estado, fecha);

        System.out.println("[OK] Candidatura añadida correctamente con ID " + creada.getId() + ".");
        System.out.println();
        imprimirFicha(creada, "CANDIDATURA REGISTRADA");
    }

    /**
     * HU-05: muestra la ficha detallada de una candidatura.
     *
     * @param args argumentos completos de la orden
     */
    private void ejecutarVer(String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            throw new TrackerException("Debes indicar el ID de la candidatura que quieres ver. "
                    + "Ejemplo: ver 13");
        }

        Candidatura candidatura = service.verDetalle(args[1]);
        imprimirFicha(candidatura, "DETALLE DE LA CANDIDATURA #" + candidatura.getId());
    }

    /**
     * HU-06: elimina una candidatura y confirma la operacion.
     *
     * @param args argumentos completos de la orden
     */
    private void ejecutarBorrar(String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            throw new TrackerException("Debes indicar el ID de la candidatura que quieres borrar. "
                    + "Ejemplo: borrar 13");
        }

        long idEliminado = service.borrar(args[1]);
        System.out.println("[OK] Candidatura con ID " + idEliminado + " eliminada correctamente.");
    }

    /**
     * HU-01: importa un fichero CSV externo y muestra el resumen.
     *
     * @param args argumentos completos de la orden
     */
    private void ejecutarImportar(String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            throw new TrackerException("Debes indicar la ruta del fichero CSV a importar. "
                    + "Ejemplo: importar candidaturas.csv");
        }

        String ruta = args[1].trim();
        int importadas = service.importar(ruta);
        System.out.println("[OK] Se han importado " + importadas
                + " candidaturas correctamente desde " + ruta);
    }

    /**
     * Interpreta opciones con nombre, admitiendo tanto {@code --clave valor}
     * como {@code --clave=valor} y una o dos barras iniciales.
     *
     * @param args       argumentos completos
     * @param inicio     índice donde comienzan las opciones
     * @param permitidas claves admitidas
     * @param ejemplo    ejemplo de uso para los mensajes de error
     * @return mapa ordenado de opciones
     */
    private Map<String, String> parsearOpciones(String[] args, int inicio,
                                                Set<String> permitidas, String ejemplo) {
        Map<String, String> opciones = new LinkedHashMap<>();

        for (int i = inicio; i < args.length; i++) {
            String token = args[i];
            if (token == null || token.isBlank()) {
                continue;
            }
            if (!token.startsWith("-")) {
                throw new TrackerException("Argumento no reconocido: '" + token + "'. "
                        + "Ejemplo: " + ejemplo);
            }

            String sinGuiones = token.replaceFirst("^-+", "");
            if (sinGuiones.isEmpty()) {
                throw new TrackerException("Opción no válida: '" + token + "'. Ejemplo: " + ejemplo);
            }

            String clave;
            String valor;
            int posicionIgual = sinGuiones.indexOf('=');

            if (posicionIgual >= 0) {
                clave = sinGuiones.substring(0, posicionIgual).toLowerCase(Locale.ROOT);
                valor = sinGuiones.substring(posicionIgual + 1);
            } else {
                clave = sinGuiones.toLowerCase(Locale.ROOT);
                if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                    throw new TrackerException("La opción --" + clave
                            + " requiere un valor. Ejemplo: " + ejemplo);
                }
                valor = args[++i];
            }

            if (!permitidas.contains(clave)) {
                throw new TrackerException("Opción desconocida '--" + clave + "'. "
                        + "Opciones válidas: " + formatearOpciones(permitidas)
                        + ". Ejemplo: " + ejemplo);
            }
            if (valor == null || valor.isBlank()) {
                throw new TrackerException("La opción --" + clave
                        + " no puede estar vacía. Ejemplo: " + ejemplo);
            }

            opciones.put(clave, valor.trim());
        }

        return opciones;
    }

    /**
     * Comprueba que una opcion obligatoria este presente y tenga valor.
     *
     * @param opciones mapa de opciones
     * @param clave    nombre de la opcion
     * @param ejemplo  ejemplo de uso
     * @return valor de la opcion
     */
    private String opcionObligatoria(Map<String, String> opciones, String clave, String ejemplo) {
        String valor = opciones.get(clave);
        if (valor == null || valor.isBlank()) {
            throw new TrackerException("Falta el parámetro obligatorio --" + clave
                    + ". Ejemplo: " + ejemplo);
        }
        return valor;
    }

    /**
     * Imprime una tabla ASCII alineada con todas las candidaturas.
     *
     * @param candidaturas registros a mostrar
     * @param filtrada     indica si se han aplicado filtros
     */
    private void imprimirTabla(List<Candidatura> candidaturas, boolean filtrada) {
        if (candidaturas.isEmpty()) {
            if (filtrada) {
                System.out.println("[INFO] No se han encontrado candidaturas con los filtros indicados.");
            } else {
                System.out.println("[INFO] No hay candidaturas registradas.");
            }
            return;
        }

        String[] cabeceras = {"ID", "TÍTULO", "EMPRESA", "ESTADO", "FECHA"};
        List<String[]> filas = new ArrayList<>();

        for (Candidatura candidatura : candidaturas) {
            filas.add(new String[]{
                    String.valueOf(candidatura.getId()),
                    truncar(limpiar(candidatura.getTitulo()), ANCHO_MAX_TITULO),
                    truncar(limpiar(candidatura.getEmpresa()), ANCHO_MAX_EMPRESA),
                    truncar(limpiar(candidatura.getEstado()), ANCHO_MAX_ESTADO),
                    Candidatura.formatearFecha(candidatura.getFecha())
            });
        }

        int[] anchos = new int[cabeceras.length];
        for (int columna = 0; columna < cabeceras.length; columna++) {
            int ancho = cabeceras[columna].length();
            for (String[] fila : filas) {
                ancho = Math.max(ancho, fila[columna].length());
            }
            anchos[columna] = ancho;
        }

        String separador = construirSeparador(anchos);
        System.out.println(separador);
        System.out.println(construirFila(cabeceras, anchos));
        System.out.println(separador);
        for (String[] fila : filas) {
            System.out.println(construirFila(fila, anchos));
        }
        System.out.println(separador);
        System.out.println("Total de registros: " + candidaturas.size());
    }

    /**
     * Construye la línea separadora de la tabla.
     *
     * @param anchos anchuras de cada columna
     * @return línea con formato {@code +----+----+}
     */
    private String construirSeparador(int[] anchos) {
        StringBuilder separador = new StringBuilder("+");
        for (int ancho : anchos) {
            separador.append("-".repeat(ancho + 2)).append('+');
        }
        return separador.toString();
    }

    /**
     * Construye una fila de la tabla con todas las columnas alineadas.
     *
     * @param valores valores de la fila
     * @param anchos  anchuras de cada columna
     * @return fila formateada
     */
    private String construirFila(String[] valores, int[] anchos) {
        StringBuilder fila = new StringBuilder("|");
        for (int i = 0; i < valores.length; i++) {
            fila.append(' ').append(ajustarDerecha(valores[i], anchos[i])).append(" |");
        }
        return fila.toString();
    }

    /**
     * Imprime una ficha detallada dentro de un recuadro.
     *
     * @param candidatura candidatura a mostrar
     * @param cabecera    titulo de la ficha
     */
    private void imprimirFicha(Candidatura candidatura, String cabecera) {
        List<String> lineas = new ArrayList<>();
        lineas.add("ID       : " + candidatura.getId());
        lineas.add("Título   : " + limpiar(candidatura.getTitulo()));
        lineas.add("Empresa  : " + limpiar(candidatura.getEmpresa()));
        lineas.add("Estado   : " + limpiar(candidatura.getEstado()));
        lineas.add("Fecha    : " + Candidatura.formatearFecha(candidatura.getFecha()));

        int anchoContenido = cabecera.length();
        for (String linea : lineas) {
            anchoContenido = Math.max(anchoContenido, linea.length());
        }

        String borde = "+" + "-".repeat(anchoContenido + 2) + "+";

        System.out.println(borde);
        System.out.println("| " + ajustarDerecha(cabecera, anchoContenido) + " |");
        System.out.println(borde);
        for (String linea : lineas) {
            System.out.println("| " + ajustarDerecha(linea, anchoContenido) + " |");
        }
        System.out.println(borde);
    }

    /**
     * Muestra la guia rapida de comandos y ejemplos de uso.
     */
    private void imprimirAyuda() {
        System.out.println("============================================================");
        System.out.println(" TRACKER DE CANDIDATURAS LABORALES v1.0");
        System.out.println("============================================================");
        System.out.println(" Uso: java -jar tracker.jar <comando> [opciones]");
        System.out.println();
        System.out.println(" COMANDOS DISPONIBLES");
        System.out.println("   listar [--estado ESTADO] [--empresa TEXTO]");
        System.out.println("   anadir --titulo \"...\" --empresa \"...\" --estado ESTADO [--fecha dd/MM/yyyy]");
        System.out.println("   ver <id>");
        System.out.println("   borrar <id>");
        System.out.println("   importar <archivo.csv>");
        System.out.println("   ayuda | --help");
        System.out.println();
        System.out.println(" EJEMPLOS");
        System.out.println("   java -jar tracker.jar listar || (Listar todas las candidaturas)");
        System.out.println("   java -jar tracker.jar listar --estado pendiente --empresa Indra || (Filtrar por estado y empresa)");
        System.out.println("   java -jar tracker.jar anadir --titulo \"Desarrollador Backend Java\" "
                + "--empresa \"Indra\" --estado pendiente --fecha 26/10/2026 || (Añadir una nueva candidatura)");
        System.out.println("   java -jar tracker.jar ver 13 || (Ver detalle de la candidatura 13)");
        System.out.println("   java -jar tracker.jar borrar 13 || (Borrar la candidatura 13)");
        System.out.println("   java -jar tracker.jar importar candidaturas.csv || (creará candidaturas.txt automáticamente)");
        System.out.println("   java -jar target/tracker.jar ver 9999 || (Prueba de control de errores)");
        System.out.println();
        System.out.println(" NOTAS");
        System.out.println("   - Los comandos funcionan con o sin guion inicial: listar / -listar.");
        System.out.println("   - También se acepta 'anadir' sin eñe por compatibilidad de terminal.");
        System.out.println("   - Datos en candidaturas.txt: id;titulo;empresa;estado;dd/MM/yyyy");
        System.out.println("============================================================");
    }

    /**
     * Normaliza el nombre del comando quitando guiones iniciales y pasando a
     * minusculas.
     *
     * @param argumento primer argumento recibido
     * @return comando normalizado
     */
    private String normalizarComando(String argumento) {
        if (argumento == null) {
            return "";
        }
        return argumento.trim().replaceFirst("^-+", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Da formato a las opciones validas para un mensaje de error.
     *
     * @param opciones conjunto de claves
     * @return texto con las opciones separadas por comas
     */
    private String formatearOpciones(Set<String> opciones) {
        return opciones.stream()
                .sorted()
                .map(opcion -> "--" + opcion)
                .reduce((a, b) -> a + ", " + b)
                .orElse("ninguna");
    }

    /**
     * Recorta un texto si supera el ancho maximo.
     *
     * @param texto texto original
     * @param ancho ancho maximo
     * @return texto recortado con puntos suspensivos si procede
     */
    private String truncar(String texto, int ancho) {
        if (texto.length() <= ancho) {
            return texto;
        }
        if (ancho <= 3) {
            return texto.substring(0, ancho);
        }
        return texto.substring(0, ancho - 3) + "...";
    }

    /**
     * Rellena un texto por la derecha hasta alcanzar el ancho indicado.
     *
     * @param texto texto original
     * @param ancho ancho deseado
     * @return texto alineado a la izquierda
     */
    private String ajustarDerecha(String texto, int ancho) {
        String valor = texto == null ? "" : texto;
        if (valor.length() >= ancho) {
            return valor;
        }
        return valor + " ".repeat(ancho - valor.length());
    }

    /**
     * Sustituye saltos de linea para evitar romper el formato de consola.
     *
     * @param texto texto original
     * @return texto en una sola linea
     */
    private String limpiar(String texto) {
        return texto == null ? "" : texto.replace("\r", " ").replace("\n", " ").trim();
    }
}
