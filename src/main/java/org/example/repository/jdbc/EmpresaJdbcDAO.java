package org.example.repository.jdbc;

import org.example.config.ConfiguracionBD;
import org.example.exception.TrackerException;
import org.example.model.Empresa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla EMPRESA con JDBC. Cada operación abre y cierra su propia conexión.
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 2.0
 */
public class EmpresaJdbcDAO {

    private static final String SQL_SELECT =
            "SELECT id, nombre, sector, web, fechaRegistro FROM EMPRESA";

    private final ConfiguracionBD configuracion;

    /**
     * @param configuracion configuración de conexión a la base de datos
     */
    public EmpresaJdbcDAO(ConfiguracionBD configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * @return todas las empresas, ordenadas por nombre
     * @throws TrackerException si falla la consulta
     */
    public List<Empresa> findAll() {
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(SQL_SELECT + " ORDER BY nombre");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                empresas.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudieron leer las empresas.", e);
        }
        return empresas;
    }

    /**
     * @param id identificador de la empresa
     * @return empresa encontrada o vacío si no existe
     * @throws TrackerException si falla la consulta
     */
    public Optional<Empresa> findById(long id) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(SQL_SELECT + " WHERE id = ?")) {
            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo leer la empresa con ID " + id + ".", e);
        }
    }

    /**
     * Inserta una empresa nueva. Si no se indica fecha de registro, se usa la fecha de hoy.
     *
     * @param empresa empresa sin id
     * @return empresa guardada con el id generado por la base de datos
     * @throws TrackerException si falla la inserción
     */
    public Empresa insert(Empresa empresa) {
        String sql = "INSERT INTO EMPRESA (nombre, sector, web, fechaRegistro) VALUES (?, ?, ?, ?)";
        LocalDate fecha = empresa.fechaRegistro() != null ? empresa.fechaRegistro() : LocalDate.now();

        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, empresa.nombre());
            pst.setString(2, empresa.sector());
            pst.setString(3, empresa.web());
            pst.setDate(4, Date.valueOf(fecha));
            pst.executeUpdate();

            try (ResultSet claves = pst.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new TrackerException("La base de datos no devolvió el ID de la nueva empresa.");
                }
                return new Empresa(claves.getLong(1), empresa.nombre(), empresa.sector(), empresa.web(), fecha);
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo guardar la empresa '" + empresa.nombre() + "'.", e);
        }
    }

    /**
     * Actualiza nombre, sector y web de una empresa existente. La fecha de registro no cambia.
     *
     * @param empresa empresa con id
     * @return {@code true} si se actualizó alguna fila
     * @throws TrackerException si falla la actualización
     */
    public boolean update(Empresa empresa) {
        String sql = "UPDATE EMPRESA SET nombre = ?, sector = ?, web = ? WHERE id = ?";
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, empresa.nombre());
            pst.setString(2, empresa.sector());
            pst.setString(3, empresa.web());
            pst.setLong(4, empresa.id());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo actualizar la empresa con ID " + empresa.id() + ".", e);
        }
    }

    /**
     * Borra una empresa. Si tiene ofertas asociadas, la base de datos lo impide.
     *
     * @param id identificador de la empresa
     * @return {@code true} si se borró alguna fila
     * @throws TrackerException si la empresa tiene ofertas o si falla el borrado
     */
    public boolean deleteById(long id) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement("DELETE FROM EMPRESA WHERE id = ?")) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new TrackerException("No se puede borrar la empresa " + id
                        + " porque tiene ofertas asociadas.", e);
            }
            throw new TrackerException("No se pudo borrar la empresa con ID " + id + ".", e);
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en una {@link Empresa}.
     *
     * @param rs resultado posicionado en la fila a leer
     * @return empresa construida con los datos de la fila
     * @throws SQLException si falla la lectura de alguna columna
     */
    private static Empresa mapear(ResultSet rs) throws SQLException {
        return new Empresa(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("sector"),
                rs.getString("web"),
                rs.getDate("fechaRegistro").toLocalDate());
    }
}
