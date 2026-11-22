package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Candidatura a una oferta de empleo. Un {@code id} nulo indica que aún no está guardada.
 *
 * @param id               identificador de la base de datos
 * @param idEmpresa        identificador de la empresa asociada
 * @param nombreEmpresa    nombre de la empresa (dato de lectura, procede de un JOIN)
 * @param titulo           puesto al que se postula
 * @param descripcion      descripción de la oferta
 * @param estado           estado actual del proceso
 * @param fechaPostulacion fecha en que se envió la candidatura
 * @param enlace           URL de la oferta
 * @param salario          salario ofrecido
 * @param notas            notas personales
 */
public record Oferta(
        Long id,
        Long idEmpresa,
        String nombreEmpresa,
        String titulo,
        String descripcion,
        EstadoOferta estado,
        LocalDate fechaPostulacion,
        String enlace,
        BigDecimal salario,
        String notas) {
}