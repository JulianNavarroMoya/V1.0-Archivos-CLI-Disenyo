package org.example.service;

import org.example.exception.TrackerException;
import org.example.model.EstadoOferta;
import org.example.model.FiltroOferta;
import org.example.model.Oferta;
import org.example.repository.jdbc.EmpresaJdbcDAO;
import org.example.repository.jdbc.OfertaJdbcDAO;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementación de {@link OfertaService}: valida los datos y delega en {@link OfertaJdbcDAO}.
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 2.0
 */
public class OfertaServiceImpl implements OfertaService {

    private final OfertaJdbcDAO dao;
    private final EmpresaJdbcDAO empresaDao;

    /**
     * @param dao        repositorio de ofertas
     * @param empresaDao repositorio de empresas, para comprobar que la empresa existe
     */
    public OfertaServiceImpl(OfertaJdbcDAO dao, EmpresaJdbcDAO empresaDao) {
        this.dao = Objects.requireNonNull(dao, "El DAO de ofertas no puede ser nulo");
        this.empresaDao = Objects.requireNonNull(empresaDao, "El DAO de empresas no puede ser nulo");
    }

    @Override
    public List<Oferta> listarOfertas(FiltroOferta filtro) {
        return dao.findAll(filtro);
    }

    @Override
    public Optional<Oferta> buscarOferta(long id) {
        return dao.findById(id);
    }

    @Override
    public Oferta guardarOferta(Oferta oferta) {
        if (oferta == null) {
            throw new TrackerException("No hay datos de oferta que guardar.");
        }
        if (oferta.idEmpresa() == null || empresaDao.findById(oferta.idEmpresa()).isEmpty()) {
            throw new TrackerException("Debes indicar una empresa existente para la oferta.");
        }
        String titulo = exigirTexto(oferta.titulo(), "título");
        String enlace = normalizar(oferta.enlace());
        validarEnlace(enlace);
        validarSalario(oferta.salario());

        Oferta limpia = new Oferta(
                oferta.id(),
                oferta.idEmpresa(),
                null,
                titulo,
                normalizar(oferta.descripcion()),
                Objects.requireNonNullElse(oferta.estado(), EstadoOferta.PENDIENTE),
                Objects.requireNonNullElse(oferta.fechaPostulacion(), LocalDate.now()),
                enlace,
                oferta.salario(),
                normalizar(oferta.notas()));

        if (limpia.id() == null) {
            Oferta guardada = dao.insert(limpia);
            return dao.findById(guardada.id())
                    .orElseThrow(() -> new TrackerException("No se pudo leer la oferta recién guardada."));
        }
        if (!dao.update(limpia)) {
            throw new TrackerException("No existe la oferta con ID " + limpia.id() + ".");
        }
        return dao.findById(limpia.id())
                .orElseThrow(() -> new TrackerException("No existe la oferta con ID " + limpia.id() + "."));
    }

    @Override
    public void cambiarEstado(long id, EstadoOferta nuevoEstado) {
        if (nuevoEstado == null) {
            throw new TrackerException("Debes indicar el nuevo estado.");
        }
        if (!dao.updateEstado(id, nuevoEstado)) {
            throw new TrackerException("No existe la oferta con ID " + id + ".");
        }
    }

    @Override
    public void borrarOferta(long id) {
        if (!dao.deleteById(id)) {
            throw new TrackerException("No existe la oferta con ID " + id + ".");
        }
    }

    /**
     * Importación del CSV inicial. Se implementa en el siguiente commit.
     *
     * @param ruta fichero CSV de origen
     * @return número de ofertas importadas
     * @throws TrackerException siempre, hasta que se implemente la importación
     */
    @Override
    public int importarCsv(Path ruta) {
        throw new TrackerException("La importación de CSV todavía no está implementada.");
    }

    private static void validarEnlace(String enlace) {
        if (enlace != null && !enlace.startsWith("http://") && !enlace.startsWith("https://")) {
            throw new TrackerException("El enlace debe empezar por http:// o https://");
        }
    }

    private static void validarSalario(BigDecimal salario) {
        if (salario != null && salario.signum() < 0) {
            throw new TrackerException("El salario no puede ser negativo.");
        }
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new TrackerException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
