package com.sigefve.dao;

import com.sigefve.config.ConfiguracionBaseDatos;
import com.sigefve.enums.EstadoEntrega;
import com.sigefve.modelos.Entrega;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntregaDAO {
    private final ConfiguracionBaseDatos config;

    public EntregaDAO() {
        this.config = ConfiguracionBaseDatos.obtenerInstancia();
    }

    public Long crear(Entrega entrega) throws SQLException {
        String sql = """
            INSERT INTO entregas (ruta_id, direccion_destino, latitud, longitud,
                                descripcion_paquete, peso_kg, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, entrega.getRutaId());
            stmt.setString(2, entrega.getDireccionDestino());
            stmt.setDouble(3, entrega.getLatitud());
            stmt.setDouble(4, entrega.getLongitud());
            stmt.setString(5, entrega.getDescripcionPaquete());
            stmt.setDouble(6, entrega.getPesoKg());
            stmt.setString(7, entrega.getEstado().name());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    entrega.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<Entrega> obtenerPorRuta(Long rutaId) throws SQLException {
        String sql = "SELECT * FROM entregas WHERE ruta_id = ? ORDER BY id";
        List<Entrega> entregas = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, rutaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entregas.add(mapearEntrega(rs));
            }
        }
        return entregas;
    }

    public List<Entrega> obtenerCompletadasPorVehiculo(Long vehiculoId) throws SQLException {
        String sql = """
            SELECT e.* FROM entregas e
            JOIN rutas r ON e.ruta_id = r.id
            WHERE r.vehiculo_id = ? AND e.estado = 'COMPLETADA'
            ORDER BY e.fecha_completada DESC
            """;

        List<Entrega> entregas = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entregas.add(mapearEntrega(rs));
            }
        }
        return entregas;
    }

    public boolean actualizarEstado(Long id, EstadoEntrega nuevoEstado, String notas) throws SQLException {
        String sql = """
            UPDATE entregas 
            SET estado = ?, 
                fecha_completada = CASE WHEN ? = 'COMPLETADA' THEN CURRENT_TIMESTAMP ELSE fecha_completada END,
                notas_entrega = ?
            WHERE id = ?
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado.name());
            stmt.setString(2, nuevoEstado.name());
            stmt.setString(3, notas);
            stmt.setLong(4, id);

            return stmt.executeUpdate() > 0;
        }
    }

    private Entrega mapearEntrega(ResultSet rs) throws SQLException {
        Entrega entrega = new Entrega();
        entrega.setId(rs.getLong("id"));
        entrega.setRutaId(rs.getLong("ruta_id"));
        entrega.setDireccionDestino(rs.getString("direccion_destino"));
        entrega.setLatitud(rs.getDouble("latitud"));
        entrega.setLongitud(rs.getDouble("longitud"));
        entrega.setDescripcionPaquete(rs.getString("descripcion_paquete"));
        entrega.setPesoKg(rs.getDouble("peso_kg"));
        entrega.setEstado(EstadoEntrega.valueOf(rs.getString("estado")));
        entrega.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        
        Timestamp fechaCompletada = rs.getTimestamp("fecha_completada");
        if (fechaCompletada != null) {
            entrega.setFechaCompletada(fechaCompletada.toLocalDateTime());
        }
        
        entrega.setNotasEntrega(rs.getString("notas_entrega"));
        return entrega;
    }
}