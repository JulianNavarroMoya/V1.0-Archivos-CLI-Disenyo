package org.example.service;

import org.example.model.Entrevista;

import java.util.List;

/**
 * Operaciones de negocio sobre entrevistas.
 */
public interface EntrevistaService {

    /**
     * @param idOferta identificador de la oferta
     * @return entrevistas de la oferta, ordenadas por fecha
     */
    List<Entrevista> listarPorOferta(long idOferta);

    /**
     * Inserta la entrevista si no tiene id, o la actualiza si ya lo tiene.
     *
     * @param entrevista entrevista a guardar
     * @return entrevista guardada con su id definitivo
     * @throws org.example.exception.TrackerException si los datos no son válidos
     */
    Entrevista guardarEntrevista(Entrevista entrevista);

    /**
     * @param id identificador de la entrevista
     */
    void borrarEntrevista(long id);
}
