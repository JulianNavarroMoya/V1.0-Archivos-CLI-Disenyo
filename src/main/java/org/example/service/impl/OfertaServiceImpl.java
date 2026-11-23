package org.example.service.impl;

import org.example.exception.TrackerException;
import org.example.model.Empresa;
import org.example.model.EstadoOferta;
import org.example.model.FiltroOferta;
import org.example.model.Oferta;
import org.example.repository.jdbc.EmpresaJdbcDAO;
import org.example.repository.jdbc.OfertaJdbcDAO;
import org.example.service.OfertaService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.example.service.impl.Validaciones.obligatorio;
import static org.example.service.impl.Validaciones.opcional;
import static org.example.service.impl.Validaciones.urlValida;

public class OfertaServiceImpl implements OfertaService {

    private final OfertaJdbcDAO ofertaDAO;
    private final EmpresaJdbcDAO empresaDAO;

    public OfertaServiceImpl(OfertaJdbcDAO ofertaDAO, EmpresaJdbcDAO empresaDAO) {
        this.ofertaDAO = ofertaDAO;
        this.empresaDAO = empresaDAO;
    }

    @Override
    public List<Oferta> listarOfertas(FiltroOferta filtro) {
        return ofertaDAO.findAll(filtro);
    }

    @Override
    public Optional<Oferta> buscarOferta(long id) {
        return ofertaDAO.findById(id);
    }

    @Override
    public Oferta guardarOferta(Oferta oferta) {
        if (oferta == null) {
            throw new TrackerException("No hay ninguna oferta que guardar.");
        }
        if (oferta.idEmpresa() == null || empresaDAO.findById(oferta.idEmpresa()).isEmpty()) {
            throw new TrackerException("Debes seleccionar una empresa existente.");
        }
        String titulo = obligatorio(oferta.titulo(), "El título del puesto es obligatorio.");
        String enlace = opcional(oferta.enlace());
        urlValida(enlace, "El enlace de la oferta debe empezar por http:// o https://.");
        if (oferta.salario() != null && oferta.salario().signum() < 0) {
            throw new TrackerException("El salario no puede ser negativo.");
        }

        EstadoOferta estado = oferta.estado() != null ? oferta.estado() : EstadoOferta.PENDIENTE;
        LocalDate fecha = oferta.fechaPostulacion() != null ? oferta.fechaPostulacion() : LocalDate.now();
        Oferta limpia = new Oferta(oferta.id(), oferta.idEmpresa(), null, titulo,
                opcional(oferta.descripcion()), estado, fecha, enlace, oferta.salario(),
                opcional(oferta.notas()));

        if (limpia.id() == null) {
            Oferta insertada = ofertaDAO.insert(limpia);
            return ofertaDAO.findById(insertada.id()).orElse(insertada);
        }
        if (!ofertaDAO.update(limpia)) {
            throw new TrackerException("No existe la oferta con ID " + limpia.id() + ".");
        }
        return ofertaDAO.findById(limpia.id())
                .orElseThrow(() -> new TrackerException("No se pudo releer la oferta " + limpia.id() + "."));
    }

    @Override
    public void cambiarEstado(long id, EstadoOferta nuevoEstado) {
        if (nuevoEstado == null) {
            throw new TrackerException("Debes indicar un estado.");
        }
        if (!ofertaDAO.updateEstado(id, nuevoEstado)) {
            throw new TrackerException("No existe la oferta con ID " + id + ".");
        }
    }

    @Override
    public void borrarOferta(long id) {
        // Sus entrevistas se borran en cascada por la clave foránea
        if (!ofertaDAO.deleteById(id)) {
            throw new TrackerException("No existe la oferta con ID " + id + ".");
        }
    }

    @Override
    public int importarCsv(Path ruta) {
        if (ruta == null || !Files.isRegularFile(ruta)) {
            throw new TrackerException("No se encuentra el fichero " + ruta + ".");
        }
        List<String> lineas;
        try {
            lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TrackerException("No se puede leer el fichero CSV.", e);
        }

        // 1) Validar todas las filas antes de escribir nada
        List<FilaCsv> filas = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) { // la posición 0 es la cabecera
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            try {
                filas.add(parsearFila(i + 1, linea));
            } catch (IllegalArgumentException e) {
                errores.add("Línea " + (i + 1) + ": " + e.getMessage());
            }
        }
        if (!errores.isEmpty()) {
            throw new TrackerException("El CSV contiene errores, no se ha importado nada:\n"
                    + String.join("\n", errores));
        }

        // 2) Escribir
        for (FilaCsv fila : filas) {
            long idEmpresa = buscarOCrearEmpresa(fila.empresa());
            guardarOferta(new Oferta(null, idEmpresa, null, fila.titulo(), null,
                    fila.estado(), fila.fechaPostulacion(), fila.enlace(), fila.salario(), fila.notas()));
        }
        return filas.size();
    }

    /**
     * Fila del CSV ya validada.
     */
    private record FilaCsv(int linea, String empresa, String titulo, EstadoOferta estado,
                           LocalDate fechaPostulacion, BigDecimal salario, String enlace, String notas) {
    }

    /**
     * Formato esperado: empresa;titulo;estado;fechaPostulacion;salario;enlace;notas
     */
    private FilaCsv parsearFila(int numero, String linea) {
        String[] c = linea.split(";", -1);
        if (c.length != 7) {
            throw new IllegalArgumentException("se esperaban 7 columnas y hay " + c.length);
        }
        String empresa = c[0].trim();
        String titulo = c[1].trim();
        if (empresa.isEmpty() || titulo.isEmpty()) {
            throw new IllegalArgumentException("empresa y título son obligatorios");
        }
        EstadoOferta estado = Arrays.stream(EstadoOferta.values())
                .filter(e -> e.valorBd().equals(c[2].trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("estado no válido: '" + c[2].trim() + "'"));
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(c[3].trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("fecha no válida (use aaaa-mm-dd): '" + c[3].trim() + "'");
        }
        BigDecimal salario = null;
        String textoSalario = c[4].trim();
        if (!textoSalario.isEmpty()) {
            try {
                salario = new BigDecimal(textoSalario);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("salario no numérico: '" + textoSalario + "'");
            }
            if (salario.signum() < 0) {
                throw new IllegalArgumentException("el salario no puede ser negativo");
            }
        }
        return new FilaCsv(numero, empresa, titulo, estado, fecha, salario,
                opcional(c[5]), opcional(c[6]));
    }

    /**
     * Busca la empresa por nombre sin distinguir mayúsculas; si no existe, la crea.
     */
    private long buscarOCrearEmpresa(String nombre) {
        return empresaDAO.findAll().stream()
                .filter(e -> e.nombre().equalsIgnoreCase(nombre))
                .findFirst()
                .map(Empresa::id)
                .orElseGet(() -> empresaDAO.insert(new Empresa(null, nombre, null, null, null)).id());
    }
}