package org.example.repository.jdbc;

import org.example.config.ConfiguracionBD;
import org.example.exception.TrackerException;
import org.example.model.EstadoOferta;
import org.example.model.FiltroOferta;
import org.example.model.Oferta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Acceso a la tabla OFERTA con JDBC. Las consultas de lectura unen la tabla EMPRESA
 * para obtener el nombre de la empresa. Cada operación abre y cierra su propia conexión.
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 2.0
 */
public class OfertaJdbcDAO {

    private static final String SQL_SELECT =
            "SELECT o.id, o.id_empresa, e.nombre AS nombreEmpresa, o.titulo, o.descripcion, "
                    + "o.estado, o.fechaPostulacion, o.enlace, o.salario, o.notas "
                    + "FROM OFERTA o JOIN EMPRESA e ON e.id = o.id_empresa";

    private final ConfiguracionBD configuracion;

    /**
     * @param configuracion configuración de conexión a la base de datos
     */
    public OfertaJdbcDAO(ConfiguracionBD configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * Busca ofertas aplicando todos los criterios indicados, combinados con AND.
     * Un criterio {@code null} no filtra.
     *
     * @param filtro criterios de búsqueda; si es {@code null} se devuelven todas
     * @return ofertas ordenadas por fecha de postulación descendente
     * @throws TrackerException si falla la consulta
     */
    public List<Oferta> findAll(FiltroOferta filtro) {
        FiltroOferta criterios = Objects.requireNonNullElse(filtro,
                new FiltroOferta(null, null, null, null, null));

        StringBuilder sql = new StringBuilder(SQL_SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (criterios.idEmpresa() != null) {
            sql.append(" AND o.id_empresa = ?");
            parametros.add(criterios.idEmpresa());
        }
        if (criterios.estado() != null) {
            sql.append(" AND o.estado = ?");
            parametros.add(criterios.estado().valorBd());
        }
        if (criterios.salarioMinimo() != null) {
            sql.append(" AND o.salario >= ?");
            parametros.add(criterios.salarioMinimo());
        }
        if (criterios.fechaDesde() != null) {
            sql.append(" AND o.fechaPostulacion >= ?");
            parametros.add(Date.valueOf(criterios.fechaDesde()));
        }
        if (criterios.fechaHasta() != null) {
            sql.append(" AND o.fechaPostulacion <= ?");
            parametros.add(Date.valueOf(criterios.fechaHasta()));
        }
        sql.append(" ORDER BY o.fechaPostulacion DESC, o.id DESC");

        List<Oferta> ofertas = new ArrayList<>();
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                pst.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudieron leer las ofertas.", e);
        }
        return ofertas;
    }

    /**
     * @param id identificador de la oferta
     * @return oferta encontrada o vacío si no existe
     * @throws TrackerException si falla la consulta
     */
    public Optional<Oferta> findById(long id) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(SQL_SELECT + " WHERE o.id = ?")) {
            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo leer la oferta con ID " + id + ".", e);
        }
    }

    /**
     * Inserta una oferta nueva. Si no se indica estado se usa {@code pendiente}, y si no
     * se indica fecha de postulación, la fecha de hoy.
     *
     * @param oferta oferta sin id
     * @return oferta guardada con el id generado
     * @throws TrackerException si falla la inserción
     */
    public Oferta insert(Oferta oferta) {
        String sql = "INSERT INTO OFERTA (id_empresa, titulo, descripcion, estado, "
                + "fechaPostulacion, enlace, salario, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        EstadoOferta estado = oferta.estado() != null ? oferta.estado() : EstadoOferta.PENDIENTE;
        LocalDate fecha = oferta.fechaPostulacion() != null ? oferta.fechaPostulacion() : LocalDate.now();

        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setLong(1, oferta.idEmpresa());
            pst.setString(2, oferta.titulo());
            pst.setString(3, oferta.descripcion());
            pst.setString(4, estado.valorBd());
            pst.setDate(5, Date.valueOf(fecha));
            pst.setString(6, oferta.enlace());
            pst.setBigDecimal(7, oferta.salario());
            pst.setString(8, oferta.notas());
            pst.executeUpdate();

            try (ResultSet claves = pst.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new TrackerException("La base de datos no devolvió el ID de la nueva oferta.");
                }
                return new Oferta(claves.getLong(1), oferta.idEmpresa(), oferta.nombreEmpresa(),
                        oferta.titulo(), oferta.descripcion(), estado, fecha,
                        oferta.enlace(), oferta.salario(), oferta.notas());
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo guardar la oferta '" + oferta.titulo() + "'.", e);
        }
    }

    /**
     * Actualiza todos los datos editables de una oferta existente.
     *
     * @param oferta oferta con id
     * @return {@code true} si se actualizó alguna fila
     * @throws TrackerException si falla la actualización
     */
    public boolean update(Oferta oferta) {
        String sql = "UPDATE OFERTA SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, "
                + "fechaPostulacion = ?, enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, oferta.idEmpresa());
            pst.setString(2, oferta.titulo());
            pst.setString(3, oferta.descripcion());
            pst.setString(4, oferta.estado().valorBd());
            pst.setDate(5, Date.valueOf(oferta.fechaPostulacion()));
            pst.setString(6, oferta.enlace());
            pst.setBigDecimal(7, oferta.salario());
            pst.setString(8, oferta.notas());
            pst.setLong(9, oferta.id());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo actualizar la oferta con ID " + oferta.id() + ".", e);
        }
    }

    /**
     * Cambia solo el estado de una oferta.
     *
     * @param id     identificador de la oferta
     * @param estado nuevo estado
     * @return {@code true} si se actualizó alguna fila
     * @throws TrackerException si falla la actualización
     */
    public boolean updateEstado(long id, EstadoOferta estado) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement("UPDATE OFERTA SET estado = ? WHERE id = ?")) {
            pst.setString(1, estado.valorBd());
            pst.setLong(2, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo cambiar el estado de la oferta " + id + ".", e);
        }
    }

    /**
     * Borra una oferta. Sus entrevistas se borran en cascada por la clave foránea.
     *
     * @param id identificador de la oferta
     * @return {@code true} si se borró alguna fila
     * @throws TrackerException si falla el borrado
     */
    public boolean deleteById(long id) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement("DELETE FROM OFERTA WHERE id = ?")) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo borrar la oferta con ID " + id + ".", e);
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en una {@link Oferta}.
     *
     * @param rs resultado posicionado en la fila a leer
     * @return oferta construida con los datos de la fila
     * @throws SQLException si falla la lectura de alguna columna
     */
    private static Oferta mapear(ResultSet rs) throws SQLException {
        return new Oferta(
                rs.getLong("id"),
                rs.getLong("id_empresa"),
                rs.getString("nombreEmpresa"),
                rs.getString("titulo"),
                rs.getString("descripcion"),
                EstadoOferta.desdeBd(rs.getString("estado")),
                rs.getDate("fechaPostulacion").toLocalDate(),
                rs.getString("enlace"),
                rs.getBigDecimal("salario"),
                rs.getString("notas"));
    }
}
