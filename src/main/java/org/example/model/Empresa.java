package org.example.model;

import java.time.LocalDate;

/**
 * Empresa a la que se envían candidaturas. Un {@code id} nulo indica que aún no está guardada.
 *
 * @param id            identificador de la base de datos
 * @param nombre        nombre de la empresa
 * @param sector        sector de actividad
 * @param web           sitio web corporativo
 * @param fechaRegistro fecha en que se registró la empresa
 */
public record Empresa(Long id, String nombre, String sector, String web, LocalDate fechaRegistro) {
}