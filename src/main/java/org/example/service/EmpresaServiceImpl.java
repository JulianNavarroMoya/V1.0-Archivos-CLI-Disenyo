package org.example.service;

import org.example.exception.TrackerException;
import org.example.model.Empresa;
import org.example.repository.jdbc.EmpresaJdbcDAO;

import java.util.List;
import java.util.Objects;

/**
 * Implementación de {@link EmpresaService}: valida los datos y delega en {@link EmpresaJdbcDAO}.
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 2.0
 */
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaJdbcDAO dao;

    /**
     * @param dao repositorio de empresas
     */
    public EmpresaServiceImpl(EmpresaJdbcDAO dao) {
        this.dao = Objects.requireNonNull(dao, "El DAO de empresas no puede ser nulo");
    }

    @Override
    public List<Empresa> listarEmpresas() {
        return dao.findAll();
    }

    @Override
    public Empresa guardarEmpresa(Empresa empresa) {
        if (empresa == null) {
            throw new TrackerException("No hay datos de empresa que guardar.");
        }
        String nombre = exigirTexto(empresa.nombre(), "nombre");
        String sector = normalizar(empresa.sector());
        String web = normalizar(empresa.web());
        validarWeb(web);
        validarNombreUnico(nombre, empresa.id());

        Empresa limpia = new Empresa(empresa.id(), nombre, sector, web, empresa.fechaRegistro());
        if (limpia.id() == null) {
            return dao.insert(limpia);
        }
        if (!dao.update(limpia)) {
            throw new TrackerException("No existe la empresa con ID " + limpia.id() + ".");
        }
        return dao.findById(limpia.id())
                .orElseThrow(() -> new TrackerException("No existe la empresa con ID " + limpia.id() + "."));
    }

    @Override
    public void borrarEmpresa(long id) {
        if (!dao.deleteById(id)) {
            throw new TrackerException("No existe la empresa con ID " + id + ".");
        }
    }

    /**
     * Comprueba que no exista otra empresa con el mismo nombre (sin distinguir mayúsculas).
     *
     * @param nombre   nombre a comprobar
     * @param idActual id de la empresa que se edita, o {@code null} si es nueva
     * @throws TrackerException si ya existe otra empresa con ese nombre
     */
    private void validarNombreUnico(String nombre, Long idActual) {
        boolean duplicada = dao.findAll().stream()
                .anyMatch(e -> e.nombre().equalsIgnoreCase(nombre) && !Objects.equals(e.id(), idActual));
        if (duplicada) {
            throw new TrackerException("Ya existe una empresa llamada '" + nombre + "'.");
        }
    }

    /**
     * @param web sitio web (puede ser {@code null})
     * @throws TrackerException si no empieza por http:// o https://
     */
    private static void validarWeb(String web) {
        if (web != null && !web.startsWith("http://") && !web.startsWith("https://")) {
            throw new TrackerException("La web debe empezar por http:// o https://");
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