package org.example.model;

import org.example.exception.TrackerException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Entidad que representa una candidatura laboral.
 *
 * <p>El modelo se serializa en texto plano con el formato exacto:</p>
 *
 * <pre>id;titulo;empresa;estado;dd/MM/yyyy</pre>
 *
 * <p>Ejemplo: {@code 13;Desarrollador Backend Java;Indra;PENDIENTE;26/10/2026}</p>
 *
 * @author Julian Navarro Moya
 * @version 1.0
 */
public class Candidatura {

    /**
     * Formato oficial de fecha utilizado en toda la aplicación y en los
     * ficheros de persistencia.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Long id;
    private String titulo;
    private String empresa;
    private String estado;
    private LocalDate fecha;

    /**
     * Constructor vacío requerido por frameworks de persistencia futuros
     * (JDBC / Hibernate).
     */
    public Candidatura() {
    }

    /**
     * Crea una candidatura sin identificador. El repositorio asignará el ID
     * autoincremental al guardarla.
     *
     * @param titulo  puesto al que se opta
     * @param empresa empresa que publica la oferta
     * @param estado  estado actual del proceso
     * @param fecha   fecha de la candidatura
     */
    public Candidatura(String titulo, String empresa, String estado, LocalDate fecha) {
        this(null, titulo, empresa, estado, fecha);
    }

    /**
     * Crea una candidatura con todos sus atributos.
     *
     * @param id      identificador único de la candidatura
     * @param titulo  puesto al que se opta
     * @param empresa empresa que publica la oferta
     * @param estado  estado actual del proceso
     * @param fecha   fecha de la candidatura
     */
    public Candidatura(Long id, String titulo, String empresa, String estado, LocalDate fecha) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estado = estado;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Serializa la candidatura como una línea CSV separada por punto y coma.
     *
     * <p>Los caracteres {@code ;} y los saltos de línea incluidos en los
     * campos de texto se sustituyen para no corromper la estructura del
     * fichero.</p>
     *
     * @return línea lista para escribirse en {@code candidaturas.txt}
     * @throws TrackerException si la candidatura no tiene ID o fecha
     */
    public String toCsvLine() {
        if (id == null) {
            throw new TrackerException("No se puede guardar una candidatura sin ID.");
        }
        if (fecha == null) {
            throw new TrackerException("No se puede guardar una candidatura sin fecha.");
        }
        return id + ";"
                + sanitizar(titulo) + ";"
                + sanitizar(empresa) + ";"
                + sanitizar(estado) + ";"
                + DATE_FORMATTER.format(fecha);
    }

    /**
     * Reconstruye una candidatura a partir de una línea de texto con el
     * formato {@code id;titulo;empresa;estado;dd/MM/yyyy}.
     *
     * @param line línea leída del fichero
     * @return candidatura correctamente parseada
     * @throws TrackerException si la línea está vacía, no tiene cinco campos,
     *                          el ID no es numérico o la fecha es invalida
     */
    public static Candidatura fromCsvLine(String line) {
        if (line == null || line.isBlank()) {
            throw new TrackerException("La línea del fichero está vacía o no contiene datos.");
        }

        String lineaLimpia = line.startsWith("\uFEFF") ? line.substring(1) : line;
        String[] partes = lineaLimpia.split(";", -1);

        if (partes.length != 5) {
            throw new TrackerException("Línea con formato incorrecto: se esperaban 5 campos separados por ';' "
                    + "pero se encontraron " + partes.length + ". Línea: " + lineaLimpia);
        }

        long id;
        try {
            id = Long.parseLong(partes[0].trim());
        } catch (NumberFormatException e) {
            throw new TrackerException("El ID de la línea no es numérico: '" + partes[0].trim()
                    + "'. Línea: " + lineaLimpia, e);
        }

        String titulo = partes[1].trim();
        String empresa = partes[2].trim();
        String estado = partes[3].trim();
        String fechaTexto = partes[4].trim();

        if (titulo.isEmpty() || empresa.isEmpty() || estado.isEmpty() || fechaTexto.isEmpty()) {
            throw new TrackerException("La línea contiene campos vacíos. Línea: " + lineaLimpia);
        }

        LocalDate fecha = parsearFecha(fechaTexto);
        return new Candidatura(id, titulo, empresa, estado, fecha);
    }

    /**
     * Valida y convierte un texto con formato {@code dd/MM/yyyy} en una fecha.
     *
     * @param fechaTexto fecha introducida por el usuario o leída del fichero
     * @return fecha parseada
     * @throws TrackerException si el texto no cumple el formato esperado
     */
    public static LocalDate parsearFecha(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.isBlank()) {
            throw new TrackerException("La fecha no puede estar vacía. "
                    + "Formato esperado: dd/MM/yyyy (ejemplo: 26/10/2026).");
        }

        String limpia = fechaTexto.trim();
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(limpia, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new TrackerException("La fecha '" + limpia + "' no es válida. "
                    + "Formato esperado: dd/MM/yyyy (ejemplo: 26/10/2026).", e);
        }

        // Verificación de ida y vuelta: evita que el parser SMART transforme
        // fechas imposibles (por ejemplo, 31/02/2026 en 28/02/2026).
        if (!DATE_FORMATTER.format(fecha).equals(limpia)) {
            throw new TrackerException("La fecha '" + limpia + "' no es válida. "
                    + "Formato esperado: dd/MM/yyyy (ejemplo: 26/10/2026).");
        }
        return fecha;
    }

    /**
     * Devuelve una fecha ya formateada con el patrón oficial de la aplicación.
     *
     * @param fecha fecha a formatear
     * @return texto con formato {@code dd/MM/yyyy} o cadena vacía si es nula
     */
    public static String formatearFecha(LocalDate fecha) {
        return fecha == null ? "" : DATE_FORMATTER.format(fecha);
    }

    /**
     * Elimina caracteres que romperían la estructura del fichero CSV.
     *
     * @param valor texto original
     * @return texto saneado
     */
    private static String sanitizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(";", ",")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    @Override
    public String toString() {
        return "Candidatura{"
                + "id=" + id
                + ", titulo='" + titulo + '\''
                + ", empresa='" + empresa + '\''
                + ", estado='" + estado + '\''
                + ", fecha=" + formatearFecha(fecha)
                + '}';
    }
}
