package org.example.ui;

import org.example.exception.TrackerException;
import org.example.model.EstadoOferta;
import org.example.model.FiltroOferta;
import org.example.model.Oferta;
import org.example.service.OfertaService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de prueba con datos en memoria. Solo sirve para desarrollar la interfaz
 * mientras no está integrada la implementación con base de datos.
 */
public class OfertaServicioFalso implements OfertaService {

    private final List<Oferta> datos = new ArrayList<>(List.of(
            new Oferta(1L, 1L, "Indra", "Desarrollador Junior Java", null,
                    EstadoOferta.PENDIENTE, LocalDate.of(2026, 9, 15), null, new BigDecimal("24000"), null),
            new Oferta(2L, 2L, "NTT Data", "Backend Spring Boot", null,
                    EstadoOferta.ENTREVISTA, LocalDate.of(2026, 10, 18), null, new BigDecimal("36000"), null),
            new Oferta(3L, 3L, "Accenture", "Fullstack Mobile Developer", null,
                    EstadoOferta.ACEPTADO, LocalDate.of(2026, 10, 20), null, null, null)));

    @Override
    public List<Oferta> listarOfertas(FiltroOferta filtro) {
        return datos.stream()
                .filter(o -> filtro == null || filtro.estado() == null || o.estado() == filtro.estado())
                .toList();
    }

    @Override
    public Optional<Oferta> buscarOferta(long id) {
        return datos.stream().filter(o -> o.id() == id).findFirst();
    }

    @Override
    public Oferta guardarOferta(Oferta oferta) {
        if (oferta.id() == null) {
            long nuevoId = datos.stream().mapToLong(Oferta::id).max().orElse(0L) + 1;
            Oferta nueva = new Oferta(nuevoId, oferta.idEmpresa(), oferta.nombreEmpresa(), oferta.titulo(),
                    oferta.descripcion(), oferta.estado(), oferta.fechaPostulacion(),
                    oferta.enlace(), oferta.salario(), oferta.notas());
            datos.add(nueva);
            return nueva;
        }
        datos.replaceAll(o -> o.id().equals(oferta.id()) ? oferta : o);
        return oferta;
    }

    @Override
    public void cambiarEstado(long id, EstadoOferta nuevoEstado) {
        datos.replaceAll(o -> o.id() == id
                ? new Oferta(o.id(), o.idEmpresa(), o.nombreEmpresa(), o.titulo(), o.descripcion(),
                nuevoEstado, o.fechaPostulacion(), o.enlace(), o.salario(), o.notas())
                : o);
    }

    @Override
    public void borrarOferta(long id) {
        if (!datos.removeIf(o -> o.id() == id)) {
            throw new TrackerException("No existe la oferta con ID " + id + ".");
        }
    }

    @Override
    public int importarCsv(Path ruta) {
        return 0;
    }
}