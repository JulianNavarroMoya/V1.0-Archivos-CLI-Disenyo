package org.example.service;

import org.example.exception.TrackerException;
import org.example.model.Candidatura;
import org.example.repository.CandidaturaRepository;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Servicio de aplicación que concentra las reglas de negocio y las
 * validaciones de las historias de usuario HU-01 a HU-06.
 *
 * <p>Depende únicamente de la interfaz {@link CandidaturaRepository}, lo que
 * permite cambiar la persistencia en futuras versiones sin alterar esta
 * clase.</p>
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 1.0
 */
public class CandidaturaService {

    private static final String EJEMPLO_ANADIR =
            "anadir --titulo \"Desarrollador Backend Java\" --empresa \"Indra\" "
                    + "--estado pendiente --fecha 26/10/2026";

    private final CandidaturaRepository repository;

    /**
     * Crea el servicio inyectando el repositorio de candidaturas.
     *
     * @param repository implementación de persistencia a utilizar
     */
    public CandidaturaService(CandidaturaRepository repository) {
        this.repository = Objects.requireNonNull(repository,
                "El repositorio de candidaturas no puede ser nulo");
    }

    /**
     * HU-01: importa candidaturas desde un fichero CSV externo.
     *
     * @param rutaCsv ruta del CSV de origen
     * @return número de candidaturas importadas
     * @throws TrackerException si la ruta es inválida o el fichero no existe
     */
    public int importar(String rutaCsv) {
        if (rutaCsv == null || rutaCsv.isBlank()) {
            throw new TrackerException("Debes indicar la ruta del fichero CSV a importar. "
                    + "Ejemplo: importar candidaturas.csv");
        }

        try {
            return repository.importFromCsv(Path.of(rutaCsv.trim()));
        } catch (InvalidPathException e) {
            throw new TrackerException("La ruta del fichero CSV no es válida: '" + rutaCsv.trim()
                    + "'. Ejemplo: importar candidaturas.csv", e);
        }
    }

    /**
     * HU-02: obtiene todas las candidaturas registradas.
     *
     * @return lista completa de candidaturas
     */
    public List<Candidatura> listarTodas() {
        return repository.findAll();
    }

    /**
     * HU-03: filtra candidaturas por estado y/o empresa. Ambos filtros son
     * opcionales y combinables; la comparación no distingue mayusculas ni
     * minusculas y el filtro de empresa admite coincidencia parcial.
     *
     * @param estado  estado exacto a buscar (puede ser {@code null})
     * @param empresa texto contenido en el nombre de la empresa (puede ser
     *                {@code null})
     * @return candidaturas que cumplen todos los filtros indicados
     */
    public List<Candidatura> listarFiltradas(String estado, String empresa) {
        String estadoFiltro = normalizar(estado);
        String empresaFiltro = normalizar(empresa);

        return repository.findAll().stream()
                .filter(candidatura -> estadoFiltro == null
                        || coincideEstado(candidatura.getEstado(), estadoFiltro))
                .filter(candidatura -> empresaFiltro == null
                        || coincideEmpresa(candidatura.getEmpresa(), empresaFiltro))
                .toList();
    }

    /**
     * HU-04: añade una nueva candidatura tras validar los campos
     * obligatorios. Si no se indica fecha se utiliza la fecha actual.
     *
     * @param titulo   puesto al que se opta (obligatorio)
     * @param empresa  empresa (obligatorio)
     * @param estado   estado del proceso (obligatorio)
     * @param fechaStr fecha en formato {@code dd/MM/yyyy} (opcional)
     * @return candidatura guardada con su ID autoincremental
     * @throws TrackerException si falta algún campo o la fecha es inválida
     */
    public Candidatura anadir(String titulo, String empresa, String estado, String fechaStr) {
        String tituloValidado = exigirTexto(titulo, "título");
        String empresaValidada = exigirTexto(empresa, "empresa");
        String estadoValidado = exigirTexto(estado, "estado").toUpperCase(Locale.ROOT);

        LocalDate fecha = (fechaStr == null || fechaStr.isBlank())
                ? LocalDate.now()
                : Candidatura.parsearFecha(fechaStr);

        Candidatura candidatura = new Candidatura(tituloValidado, empresaValidada,
                estadoValidado, fecha);
        return repository.save(candidatura);
    }

    /**
     * HU-05: obtiene el detalle de una candidatura por su ID.
     *
     * @param idStr identificador introducido por el usuario
     * @return candidatura encontrada
     * @throws TrackerException si el ID no es numérico o no existe
     */
    public Candidatura verDetalle(String idStr) {
        long id = parsearId(idStr, "ver");
        return repository.findById(id)
                .orElseThrow(() -> new TrackerException(
                        "No existe ninguna candidatura con el ID " + id + "."));
    }

    /**
     * HU-06: elimina una candidatura por su ID.
     *
     * @param idStr identificador introducido por el usuario
     * @return identificador realmente eliminado
     * @throws TrackerException si el ID no es numérico o no existe
     */
    public long borrar(String idStr) {
        long id = parsearId(idStr, "borrar");
        if (!repository.deleteById(id)) {
            throw new TrackerException("No existe ninguna candidatura con el ID " + id + ".");
        }
        return id;
    }

    /**
     * Convierte y valida el ID recibido como texto.
     *
     * @param idStr   texto del identificador
     * @param comando comando que se esta ejecutando (para el ejemplo de uso)
     * @return ID numérico positivo
     * @throws TrackerException si esta vacío, no es numérico o no es positivo
     */
    private long parsearId(String idStr, String comando) {
        if (idStr == null || idStr.isBlank()) {
            throw new TrackerException("Debes indicar un ID numérico. Ejemplo: " + comando + " 13");
        }

        try {
            long id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                throw new NumberFormatException("El ID debe ser mayor que cero");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new TrackerException("El ID '" + idStr.trim() + "' no es un número válido. "
                    + "Ejemplo: " + comando + " 13", e);
        }
    }

    /**
     * Comprueba que un campo de texto obligatorio tenga contenido.
     *
     * @param valor valor a validar
     * @param campo nombre del campo para el mensaje de error
     * @return valor saneado con {@code trim()}
     * @throws TrackerException si el valor es nulo o está en blanco
     */
    private String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new TrackerException("El campo '" + campo + "' no puede estar vacío. "
                    + "Ejemplo: " + EJEMPLO_ANADIR);
        }
        return valor.trim();
    }

    /**
     * Normaliza un filtro opcional. Devuelve {@code null} cuando no se ha
     * indicado.
     *
     * @param valor texto del filtro
     * @return texto sin espacios o {@code null}
     */
    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    /**
     * Compara dos estados sin distinguir mayúsculas.
     *
     * @param estadoCandidatura estado almacenado
     * @param estadoFiltro      estado solicitado
     * @return {@code true} si coinciden
     */
    private boolean coincideEstado(String estadoCandidatura, String estadoFiltro) {
        return estadoCandidatura != null
                && estadoCandidatura.trim().equalsIgnoreCase(estadoFiltro);
    }

    /**
     * Comprueba si el nombre de la empresa contiene el texto buscado, sin
     * distinguir mayusculas.
     *
     * @param empresaCandidatura nombre almacenado
     * @param empresaFiltro      texto solicitado
     * @return {@code true} si contiene el texto
     */
    private boolean coincideEmpresa(String empresaCandidatura, String empresaFiltro) {
        return empresaCandidatura != null
                && empresaCandidatura.toLowerCase(Locale.ROOT)
                .contains(empresaFiltro.toLowerCase(Locale.ROOT));
    }
}
