package org.example.service;

import org.example.model.EstadoOferta;
import org.example.model.FiltroOferta;
import org.example.model.Oferta;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones de negocio sobre ofertas (candidaturas).
 */
public interface OfertaService {

    /**
     * @param filtro criterios de búsqueda; puede tener todos sus campos a {@code null}
     * @return ofertas que cumplen todos los criterios, con el nombre de su empresa
     */
    List<Oferta> listarOfertas(FiltroOferta filtro);

    /**
     * @param id identificador de la oferta
     * @return oferta encontrada o vacío si no existe
     */
    Optional<Oferta> buscarOferta(long id);

    /**
     * Inserta la oferta si no tiene id, o la actualiza si ya lo tiene.
     *
     * @param oferta oferta a guardar
     * @return oferta guardada con su id definitivo
     * @throws org.example.exception.TrackerException si los datos no son válidos
     */
    Oferta guardarOferta(Oferta oferta);

    /**
     * @param id          identificador de la oferta
     * @param nuevoEstado estado nuevo
     */
    void cambiarEstado(long id, EstadoOferta nuevoEstado);

    /**
     * Borra una oferta junto con sus entrevistas.
     *
     * @param id identificador de la oferta
     */
    void borrarOferta(long id);

    /**
     * Importa ofertas desde el CSV inicial en una única transacción.
     *
     * @param ruta fichero CSV de origen
     * @return número de ofertas importadas
     */
    int importarCsv(Path ruta);
}
