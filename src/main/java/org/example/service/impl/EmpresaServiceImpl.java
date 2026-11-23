package org.example.service.impl;

import org.example.exception.TrackerException;
import org.example.model.Empresa;
import org.example.repository.jdbc.EmpresaJdbcDAO;
import org.example.service.EmpresaService;

import java.util.List;

import static org.example.service.impl.Validaciones.obligatorio;
import static org.example.service.impl.Validaciones.opcional;
import static org.example.service.impl.Validaciones.urlValida;

public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaJdbcDAO empresaDAO;

    public EmpresaServiceImpl(EmpresaJdbcDAO empresaDAO) {
        this.empresaDAO = empresaDAO;
    }

    @Override
    public List<Empresa> listarEmpresas() {
        return empresaDAO.findAll();
    }

    @Override
    public Empresa guardarEmpresa(Empresa empresa) {
        if (empresa == null) {
            throw new TrackerException("No hay ninguna empresa que guardar.");
        }
        String nombre = obligatorio(empresa.nombre(), "El nombre de la empresa es obligatorio.");
        if (nombre.length() > 120) {
            throw new TrackerException("El nombre de la empresa no puede tener más de 120 caracteres.");
        }
        String sector = opcional(empresa.sector());
        if (sector != null && sector.length() > 100) {
            throw new TrackerException("El sector no puede tener más de 100 caracteres.");
        }
        String web = opcional(empresa.web());
        urlValida(web, "La web de la empresa debe empezar por http:// o https://.");

        boolean nombreRepetido = empresaDAO.findAll().stream()
                .anyMatch(e -> e.nombre().equalsIgnoreCase(nombre) && !e.id().equals(empresa.id()));
        if (nombreRepetido) {
            throw new TrackerException("Ya existe una empresa llamada '" + nombre + "'.");
        }

        Empresa limpia = new Empresa(empresa.id(), nombre, sector, web, empresa.fechaRegistro());
        if (limpia.id() == null) {
            Empresa insertada = empresaDAO.insert(limpia);
            return empresaDAO.findById(insertada.id()).orElse(insertada);
        }
        if (!empresaDAO.update(limpia)) {
            throw new TrackerException("No existe la empresa con ID " + limpia.id() + ".");
        }
        return empresaDAO.findById(limpia.id())
                .orElseThrow(() -> new TrackerException("No se pudo releer la empresa " + limpia.id() + "."));
    }

    @Override
    public void borrarEmpresa(long id) {
        if (!empresaDAO.deleteById(id)) {
            throw new TrackerException("No existe la empresa con ID " + id + ".");
        }
    }
}