package org.example.service;

import org.example.exception.TrackerException;
import org.example.model.Entrevista;
import org.example.model.ResultadoEntrevista;
import org.example.repository.jdbc.EntrevistaJdbcDAO;
import org.example.repository.jdbc.OfertaJdbcDAO;

import java.util.List;
import java.util.Objects;

/**
 * Implementación de {@link EntrevistaService}: valida que la oferta exista y delega en el DAO.
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 2.0
 */
public class EntrevistaServiceImpl implements EntrevistaService {

    private final EntrevistaJdbcDAO dao;
    private final OfertaJdbcDAO ofertaDao;

    /**
     * @param dao       repositorio de entrevistas
     * @param ofertaDao repositorio de ofertas, para comprobar que la oferta existe
     */
    public EntrevistaServiceImpl(EntrevistaJdbcDAO dao, OfertaJdbcDAO ofertaDao) {
        this.dao = Objects.requireNonNull(dao, "El DAO de entrevistas no puede ser nulo");
        this.ofertaDao = Objects.requireNonNull(ofertaDao, "El DAO de ofertas no puede ser nulo");
    }

    @Override
    public List<Entrevista> listarPorOferta(long idOferta) {
        return dao.findByOferta(idOferta);
    }

    @Override
    public Entrevista guardarEntrevista(Entrevista entrevista) {
        if (entrevista == null) {
            throw new TrackerException("No hay datos de entrevista que guardar.");
        }
        if (entrevista.idOferta() == null) {
            throw new TrackerException("La entrevista debe estar asociada a una oferta.");
        }
        if (ofertaDao.findById(entrevista.idOferta()).isEmpty()) {
            throw new TrackerException("No existe la oferta con ID " + entrevista.idOferta() + ".");
        }
        if (entrevista.fecha() == null) {
            throw new TrackerException("La fecha de la entrevista es obligatoria.");
        }
        if (entrevista.tipo() == null) {
            throw new TrackerException("El tipo de entrevista es obligatorio.");
        }

        ResultadoEntrevista resultado = Objects.requireNonNullElse(
                entrevista.resultado(), ResultadoEntrevista.PENDIENTE);
        Entrevista limpia = new Entrevista(entrevista.id(), entrevista.idOferta(), entrevista.fecha(),
                entrevista.tipo(), entrevista.notas(), resultado);

        if (limpia.id() == null) {
            return dao.insert(limpia);
        }
        if (!dao.update(limpia)) {
            throw new TrackerException("No existe la entrevista con ID " + limpia.id() + ".");
        }
        return dao.findById(limpia.id())
                .orElseThrow(() -> new TrackerException("No existe la entrevista con ID " + limpia.id() + "."));
    }

    @Override
    public void borrarEntrevista(long id) {
        if (!dao.deleteById(id)) {
            throw new TrackerException("No existe la entrevista con ID " + id + ".");
        }
    }
}