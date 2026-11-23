package org.example.model;

import java.time.LocalDate;

/**
 * Entrevista asociada a una oferta. Un {@code id} nulo indica que aún no está guardada.
 *
 * @param id        identificador de la base de datos
 * @param idOferta  identificador de la oferta a la que pertenece
 * @param fecha     fecha de la entrevista
 * @param tipo      tipo de entrevista
 * @param notas     notas sobre la entrevista
 * @param resultado resultado de la entrevista
 */
public record Entrevista(
        Long id,
        Long idOferta,
        LocalDate fecha,
        TipoEntrevista tipo,
        String notas,
        ResultadoEntrevista resultado) {
}
