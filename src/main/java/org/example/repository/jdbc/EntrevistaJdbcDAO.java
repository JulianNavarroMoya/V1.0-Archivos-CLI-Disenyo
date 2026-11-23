package org.example.repository.jdbc;

import org.example.config.ConfiguracionBD;
import org.example.exception.TrackerException;
import org.example.model.Entrevista;
import org.example.model.ResultadoEntrevista;
import org.example.model.TipoEntrevista;

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
 * Acceso a la tabla ENTREVISTA con JDBC. Cada operación abre y cierra su propia conexión.
 *
 * @author Julian Navarro Moya || Chakir Hachmi
 * @version 2.0
 */
public class EntrevistaJdbcDAO {

    private static final String SQL_SELECT =
            "SELECT id, id_oferta, fecha, tipo, notas, resultado FROM ENTREVISTA";

    private final ConfiguracionBD configuracion;

    /**
     * @param configuracion configuración de conexión a la base de datos
     */
    public EntrevistaJdbcDAO(ConfiguracionBD configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * @param idOferta identificador de la oferta
     * @return entrevistas de la oferta, ordenadas por fecha
     * @throws TrackerException si falla la consulta
     */
    public List<Entrevista> findByOferta(long idOferta) {
        List<Entrevista> entrevistas = new ArrayList<>();
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(SQL_SELECT + " WHERE id_oferta = ? ORDER BY fecha")) {
            pst.setLong(1, idOferta);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    entrevistas.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudieron leer las entrevistas de la oferta " + idOferta + ".", e);
        }
        return entrevistas;
    }

    /**
     * @param id identificador de la entrevista
     * @return entrevista encontrada o vacío si no existe
     * @throws TrackerException si falla la consulta
     */
    public Optional<Entrevista> findById(long id) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(SQL_SELECT + " WHERE id = ?")) {
            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo leer la entrevista con ID " + id + ".", e);
        }
    }

    /**
     * Inserta una entrevista nueva. Si no se indica resultado, se usa {@code pendiente}.
     *
     * @param entrevista entrevista sin id
     * @return entrevista guardada con el id generado
     * @throws TrackerException si falla la inserción
     */
    public Entrevista insert(Entrevista entrevista) {
        String sql = "INSERT INTO ENTREVISTA (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        ResultadoEntrevista resultado = entrevista.resultado() != null
                ? entrevista.resultado() : ResultadoEntrevista.PENDIENTE;

        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setLong(1, entrevista.idOferta());
            pst.setDate(2, Date.valueOf(entrevista.fecha()));
            pst.setString(3, entrevista.tipo().valorBd());
            pst.setString(4, entrevista.notas());
            pst.setString(5, resultado.valorBd());
            pst.executeUpdate();

            try (ResultSet claves = pst.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new TrackerException("La base de datos no devolvió el ID de la nueva entrevista.");
                }
                return new Entrevista(claves.getLong(1), entrevista.idOferta(), entrevista.fecha(),
                        entrevista.tipo(), entrevista.notas(), resultado);
            }
        } catch (SQLException e) {
            throw new TrackerException("No se pudo guardar la entrevista de la oferta "
                    + entrevista.idOferta() + ".", e);
        }
    }

    /**
     * Actualiza los datos de una entrevista existente.
     *
     * @param entrevista entrevista con id
     * @return {@code true} si se actualizó alguna fila
     * @throws TrackerException si falla la actualización
     */
    public boolean update(Entrevista entrevista) {
        String sql = "UPDATE ENTREVISTA SET fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(entrevista.fecha()));
            pst.setString(2, entrevista.tipo().valorBd());
            pst.setString(3, entrevista.notas());
            pst.setString(4, entrevista.resultado().valorBd());
            pst.setLong(5, entrevista.id());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo actualizar la entrevista con ID " + entrevista.id() + ".", e);
        }
    }

    /**
     * @param id identificador de la entrevista
     * @return {@code true} si se borró alguna fila
     * @throws TrackerException si falla el borrado
     */
    public boolean deleteById(long id) {
        try (Connection conn = configuracion.abrirConexion();
             PreparedStatement pst = conn.prepareStatement("DELETE FROM ENTREVISTA WHERE id = ?")) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new TrackerException("No se pudo borrar la entrevista con ID " + id + ".", e);
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en una {@link Entrevista}.
     *
     * @param rs resultado posicionado en la fila a leer
     * @return entrevista construida con los datos de la fila
     * @throws SQLException si falla la lectura de alguna columna
     */
    private static Entrevista mapear(ResultSet rs) throws SQLException {
        LocalDate fecha = rs.getDate("fecha").toLocalDate();
        return new Entrevista(
                rs.getLong("id"),
                rs.getLong("id_oferta"),
                fecha,
                TipoEntrevista.desdeBd(rs.getString("tipo")),
                rs.getString("notas"),
                ResultadoEntrevista.desdeBd(rs.getString("resultado")));
    }
}
