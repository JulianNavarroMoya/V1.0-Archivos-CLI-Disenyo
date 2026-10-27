package org.example.repository;

import org.example.model.Candidatura;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de candidaturas.
 *
 * <p>El servicio de negocio depende exclusivamente de esta interfaz, de modo
 * que en la v2.0 (JDBC) y la v3.0 (Hibernate) bastará con proporcionar nuevas
 * implementaciónes sin modificar la lógica de aplicación.</p>
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 1.0
 */
public interface CandidaturaRepository {

    /**
     * Obtiene todas las candidaturas almacenadas.
     *
     * @return lista de candidaturas (vacía si no hay registros)
     */
    List<Candidatura> findAll();

    /**
     * Busca una candidatura por su identificador.
     *
     * @param id identificador de la candidatura
     * @return candidatura encontrada o {@link Optional#empty()} si no existe
     */
    Optional<Candidatura> findById(long id);

    /**
     * Guarda una candidatura. Si no tiene ID se le asigna el siguiente
     * autoincremental; si ya existe se actualiza.
     *
     * @param candidatura candidatura a persistir
     * @return candidatura guardada con su ID definitivo
     */
    Candidatura save(Candidatura candidatura);

    /**
     * Elimina la candidatura cuyo identificador coincida con el indicado.
     *
     * @param id identificador de la candidatura
     * @return {@code true} si se eliminó algún registro, {@code false} si no
     *         existía
     */
    boolean deleteById(long id);

    /**
     * Importa candidaturas desde un fichero CSV externo separado por punto y
     * coma, asignando IDs nuevos y seguros a cada registro.
     *
     * @param externalCsvPath ruta del fichero CSV de origen
     * @return número de candidaturas importadas correctamente
     */
    int importFromCsv(Path externalCsvPath);
}
