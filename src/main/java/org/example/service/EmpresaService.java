package org.example.service;

import org.example.model.Empresa;

import java.util.List;

/**
 * Operaciones de negocio sobre empresas.
 */
public interface EmpresaService {

    /**
     * @return todas las empresas registradas
     */
    List<Empresa> listarEmpresas();

    /**
     * Inserta la empresa si no tiene id, o la actualiza si ya lo tiene.
     *
     * @param empresa empresa a guardar
     * @return empresa guardada con su id definitivo
     * @throws org.example.exception.TrackerException si los datos no son válidos
     */
    Empresa guardarEmpresa(Empresa empresa);

    /**
     * Borra una empresa. Falla si tiene ofertas asociadas.
     *
     * @param id identificador de la empresa
     * @throws org.example.exception.TrackerException si tiene ofertas asociadas o no existe
     */
    void borrarEmpresa(long id);
}
