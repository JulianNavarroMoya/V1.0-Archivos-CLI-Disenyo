package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Criterios de búsqueda de ofertas. Un campo {@code null} significa "sin filtro".
 * Los criterios indicados se combinan con AND.
 *
 * @param idEmpresa     empresa concreta
 * @param estado        estado concreto
 * @param salarioMinimo salario mínimo
 * @param fechaDesde    fecha de postulación desde (inclusive)
 * @param fechaHasta    fecha de postulación hasta (inclusive)
 */
public record FiltroOferta(
        Long idEmpresa,
        EstadoOferta estado,
        BigDecimal salarioMinimo,
        LocalDate fechaDesde,
        LocalDate fechaHasta) {
}