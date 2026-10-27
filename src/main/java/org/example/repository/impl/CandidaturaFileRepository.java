package org.example.repository.impl;

import org.example.exception.TrackerException;
import org.example.model.Candidatura;
import org.example.repository.CandidaturaRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementación del repositorio de candidaturas sobre un fichero de texto
 * plano.
 *
 * <p>El fichero por defecto es {@code candidaturas.txt}, ubicado en el
 * directorio de ejecución, y cada línea respeta el formato:</p>
 *
 * <pre>id;titulo;empresa;estado;dd/MM/yyyy</pre>
 *
 * <p>Toda la entrada/salida se realiza con {@link Files}, {@link Path} y
 * {@link StandardCharsets#UTF_8}. Si el fichero no existe, se crea
 * automáticamente.</p>
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 1.0
 */
public class CandidaturaFileRepository implements CandidaturaRepository {

    /** Nombre del fichero de datos por defecto. */
    public static final String DEFAULT_FILE_NAME = "candidaturas.txt";

    private static final String BOM = "\uFEFF";

    private final Path filePath;

    /**
     * Crea el repositorio usando {@code candidaturas.txt} en el directorio
     * actual de ejecución.
     */
    public CandidaturaFileRepository() {
        this(Path.of(DEFAULT_FILE_NAME));
    }

    /**
     * Crea el repositorio sobre una ruta concreta. Si el fichero no existe se
     * crea vacío en este momento.
     *
     * @param filePath ruta del fichero de persistencia
     */
    public CandidaturaFileRepository(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "La ruta del fichero no puede ser nula");
        asegurarFichero();
    }

    /**
     * @return ruta del fichero de datos utilizado por el repositorio
     */
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public List<Candidatura> findAll() {
        return leerTodas();
    }

    @Override
    public Optional<Candidatura> findById(long id) {
        return leerTodas().stream()
                .filter(candidatura -> candidatura.getId() != null && candidatura.getId() == id)
                .findFirst();
    }

    @Override
    public Candidatura save(Candidatura candidatura) {
        if (candidatura == null) {
            throw new TrackerException("No se puede guardar una candidatura nula.");
        }

        List<Candidatura> candidaturas = leerTodas();

        if (candidatura.getId() == null) {
            candidatura.setId(calcularSiguienteId(candidaturas));
            candidaturas.add(candidatura);
        } else {
            boolean actualizada = false;
            for (int i = 0; i < candidaturas.size(); i++) {
                if (Objects.equals(candidaturas.get(i).getId(), candidatura.getId())) {
                    candidaturas.set(i, candidatura);
                    actualizada = true;
                    break;
                }
            }
            if (!actualizada) {
                candidaturas.add(candidatura);
            }
        }

        escribirTodas(candidaturas);
        return candidatura;
    }

    @Override
    public boolean deleteById(long id) {
        List<Candidatura> candidaturas = leerTodas();
        boolean eliminada = candidaturas.removeIf(candidatura ->
                candidatura.getId() != null && candidatura.getId() == id);

        if (eliminada) {
            escribirTodas(candidaturas);
        }
        return eliminada;
    }

    @Override
    public int importFromCsv(Path externalCsvPath) {
        if (externalCsvPath == null) {
            throw new TrackerException("Debes indicar la ruta del fichero CSV a importar. "
                    + "Ejemplo: importar candidaturas.csv");
        }
        if (!Files.exists(externalCsvPath)) {
            throw new TrackerException("El fichero CSV indicado no existe: '" + externalCsvPath + "'. "
                    + "Comprueba la ruta. Ejemplo: importar candidaturas.csv");
        }
        if (!Files.isRegularFile(externalCsvPath)) {
            throw new TrackerException("La ruta indicada no es un fichero CSV válido: '"
                    + externalCsvPath + "'.");
        }

        List<String> lineas;
        try {
            lineas = Files.readAllLines(externalCsvPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TrackerException("No se pudo leer el fichero CSV '" + externalCsvPath
                    + "': " + e.getMessage(), e);
        }

        List<Candidatura> existentes = leerTodas();
        long siguienteId = calcularSiguienteId(existentes);
        List<Candidatura> importadas = new ArrayList<>();

        for (String lineaOriginal : lineas) {
            String linea = quitarBom(lineaOriginal).trim();
            if (linea.isEmpty()) {
                continue;
            }
            if (esCabecera(linea)) {
                continue;
            }

            Candidatura candidatura = parsearLineaImportacion(linea, siguienteId);
            if (candidatura != null) {
                importadas.add(candidatura);
                siguienteId++;
            }
        }

        if (!importadas.isEmpty()) {
            existentes.addAll(importadas);
            escribirTodas(existentes);
        }

        return importadas.size();
    }

    /**
     * Parsea una línea de un CSV externo admitiendo 4 columnas
     * ({@code titulo;empresa;estado;fecha}) o 5 columnas
     * ({@code id;titulo;empresa;estado;fecha}). El ID externo se ignora: la
     * candidatura recibe siempre un ID nuevo y seguro.
     *
     * @param linea       linea ya limpia de BOM y espacios
     * @param idAsignado  identificador nuevo que se asignará al registro
     * @return candidatura lista para importar, o {@code null} si la linea no
     *         es valida
     */
    private Candidatura parsearLineaImportacion(String linea, long idAsignado) {
        String[] partes = linea.split(";", -1);
        String titulo;
        String empresa;
        String estado;
        String fechaTexto;

        if (partes.length == 5) {
            titulo = partes[1].trim();
            empresa = partes[2].trim();
            estado = partes[3].trim();
            fechaTexto = partes[4].trim();
        } else if (partes.length == 4) {
            titulo = partes[0].trim();
            empresa = partes[1].trim();
            estado = partes[2].trim();
            fechaTexto = partes[3].trim();
        } else {
            return null;
        }

        if (titulo.isEmpty() || empresa.isEmpty() || estado.isEmpty() || fechaTexto.isEmpty()) {
            return null;
        }

        try {
            LocalDate fecha = Candidatura.parsearFecha(fechaTexto);
            return new Candidatura(idAsignado, titulo, empresa,
                    estado.toUpperCase(Locale.ROOT), fecha);
        } catch (TrackerException e) {
            return null;
        }
    }

    /**
     * Detecta si una línea es la cabecera del CSV. Se consideran cabeceras
     * aquellas cuyo primer campo es {@code id} o {@code titulo} (también con
     * tilde), sin distinguir mayúsculas ni espacios.
     *
     * @param linea línea a comprobar
     * @return {@code true} si es una cabecera
     */
    private boolean esCabecera(String linea) {
        String normalizada = quitarBom(linea).trim().toLowerCase(Locale.ROOT);
        if (normalizada.startsWith("\"")) {
            normalizada = normalizada.substring(1);
        }
        return normalizada.startsWith("id;")
                || normalizada.startsWith("id,")
                || normalizada.startsWith("titulo;")
                || normalizada.startsWith("titulo,")
                || normalizada.startsWith("título;")
                || normalizada.startsWith("título,");
    }

    /**
     * Lee y valida todas las líneas del fichero de datos.
     *
     * @return candidaturas almacenadas
     * @throws TrackerException si el fichero no se puede leer o contiene una
     *                          línea con formato inválido
     */
    private List<Candidatura> leerTodas() {
        asegurarFichero();

        List<String> lineas;
        try {
            lineas = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TrackerException("No se pudo leer el fichero de datos '" + filePath
                    + "': " + e.getMessage(), e);
        }

        List<Candidatura> candidaturas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = quitarBom(lineas.get(i)).trim();
            if (linea.isEmpty()) {
                continue;
            }
            try {
                candidaturas.add(Candidatura.fromCsvLine(linea));
            } catch (TrackerException e) {
                throw new TrackerException("El fichero '" + filePath.getFileName()
                        + "' contiene una línea inválida (línea " + (i + 1) + "): "
                        + e.getMessage(), e);
            }
        }
        return candidaturas;
    }

    /**
     * Reescribe el fichero completo con las candidaturas indicadas.
     *
     * @param candidaturas contenido definitivo del fichero
     */
    private void escribirTodas(List<Candidatura> candidaturas) {
        asegurarFichero();

        List<String> lineas = candidaturas.stream()
                .map(Candidatura::toCsvLine)
                .toList();

        try {
            Files.write(filePath, lineas, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new TrackerException("No se pudo escribir en el fichero de datos '" + filePath
                    + "': " + e.getMessage(), e);
        }
    }

    /**
     * Garantiza que el fichero de datos existe. Si no existe, crea los
     * directorios necesarios y un fichero vacío.
     */
    private void asegurarFichero() {
        try {
            Path rutaAbsoluta = filePath.toAbsolutePath();
            Path directorioPadre = rutaAbsoluta.getParent();
            if (directorioPadre != null) {
                Files.createDirectories(directorioPadre);
            }
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new TrackerException("No se pudo crear el fichero de datos '" + filePath
                    + "': " + e.getMessage(), e);
        }
    }

    /**
     * Calcula el siguiente ID autoincremental como {@code max(id) + 1}.
     *
     * @param candidaturas registros actuales
     * @return siguiente identificador disponible, empezando en 1 si no hay
     *         registros
     */
    private long calcularSiguienteId(List<Candidatura> candidaturas) {
        return candidaturas.stream()
                .map(Candidatura::getId)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1L;
    }

    /**
     * Elimina el caracter BOM UTF-8 inicial si esta presente.
     *
     * @param texto texto original
     * @return texto sin BOM
     */
    private String quitarBom(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.startsWith(BOM) ? texto.substring(BOM.length()) : texto;
    }
}
