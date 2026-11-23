package org.example.service.impl;

import org.example.exception.TrackerException;
import org.example.model.Entrevista;
import org.example.model.ResultadoEntrevista;
import org.example.repository.jdbc.EntrevistaJdbcDAO;
import org.example.repository.jdbc.OfertaJdbcDAO;
import org.example.service.EntrevistaService;

import java.util.List;

import static org.example.service.impl.Validaciones.opcional;

public class EntrevistaServiceImpl implements EntrevistaService {

    private final EntrevistaJdbcDAO entrevistaDAO;
    private final OfertaJdbcDAO ofertaDAO;

    public EntrevistaServiceImpl(EntrevistaJdbcDAO entrevistaDAO, OfertaJdbcDAO ofertaDAO) {
        this.entrevistaDAO = entrevistaDAO;
        this.ofertaDAO = ofertaDAO;
    }

    @Override
    public List<Entrevista> listarPorOferta(long idOferta) {
        return entrevistaDAO.findByOferta(idOferta);
    }

    @Override
    public Entrevista guardarEntrevista(Entrevista entrevista) {
        if (entrevista == null) {
            throw new TrackerException("No hay ninguna entrevista que guardar.");
        }
        if (entrevista.idOferta() == null || ofertaDAO.findById(entrevista.idOferta()).isEmpty()) {
            throw new TrackerException("La entrevista debe pertenecer a una oferta existente.");
        }
        if (entrevista.fecha() == null) {
            throw new TrackerException("La fecha de la entrevista es obligatoria.");
        }
        if (entrevista.tipo() == null) {
            throw new TrackerException("El tipo de entrevista es obligatorio.");
        }
        ResultadoEntrevista resultado = entrevista.resultado() != null
                ? entrevista.resultado() : ResultadoEntrevista.PENDIENTE;
        Entrevista limpia = new Entrevista(entrevista.id(), entrevista.idOferta(), entrevista.fecha(),
                entrevista.tipo(), opcional(entrevista.notas()), resultado);

        if (limpia.id() == null) {
            return entrevistaDAO.insert(limpia);
        }
        if (!entrevistaDAO.update(limpia)) {
            throw new TrackerException("No existe la entrevista con ID " + limpia.id() + ".");
        }
        return entrevistaDAO.findById(limpia.id())
                .orElseThrow(() -> new TrackerException("No se pudo releer la entrevista " + limpia.id() + "."));
    }

    @Override
    public void borrarEntrevista(long id) {
        if (!entrevistaDAO.deleteById(id)) {
            throw new TrackerException("No existe la entrevista con ID " + id + ".");
        }
    }
}