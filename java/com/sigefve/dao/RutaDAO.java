package com.sigefve.dao;

import com.sigefve.config.ConfiguracionBaseDatos;
import com.sigefve.modelos.Ruta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RutaDAO {
    private final ConfiguracionBaseDatos config;

    public RutaDAO() {
        this.config = ConfiguracionBaseDatos.obtenerInstancia();
    }

    public Long crear(Ruta ruta) throws SQLException {
        String sql = """
            INSERT INTO rutas (nombre, vehiculo_id, distancia_total, numero_entregas)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ruta.getNombre());
            if (ruta.getVehiculoId() != null) {
                stmt.setLong(2, ruta.getVehiculoId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setDouble(3, ruta.getDistanciaTotal());
            stmt.setInt(4, ruta.getNumeroEntregas());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    ruta.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Optional<Ruta> obtenerPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM rutas WHERE id = ?";

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearRuta(rs));
            }
        }
        return Optional.empty();
    }

    public List<Ruta> obtenerPorVehiculo(Long vehiculoId) throws SQLException {
        String sql = "SELECT * FROM rutas WHERE vehiculo_id = ? ORDER BY fecha_inicio DESC";
        List<Ruta> rutas = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rutas.add(mapearRuta(rs));
            }
        }
        return rutas;
    }

    public List<Ruta> obtenerActivas() throws SQLException {
        String sql = "SELECT * FROM rutas WHERE completada = FALSE ORDER BY fecha_inicio DESC";
        List<Ruta> rutas = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rutas.add(mapearRuta(rs));
            }
        }
        return rutas;
    }

    public boolean asignarVehiculo(Long rutaId, Long vehiculoId) throws SQLException {
        String sql = "UPDATE rutas SET vehiculo_id = ? WHERE id = ?";

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculoId);
            stmt.setLong(2, rutaId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean marcarComoCompletada(Long rutaId) throws SQLException {
        String sql = "UPDATE rutas SET completada = TRUE, fecha_fin = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, rutaId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Ruta mapearRuta(ResultSet rs) throws SQLException {
        Ruta ruta = new Ruta();
        ruta.setId(rs.getLong("id"));
        ruta.setNombre(rs.getString("nombre"));
        
        long vehiculoId = rs.getLong("vehiculo_id");
        if (!rs.wasNull()) {
            ruta.setVehiculoId(vehiculoId);
        }
        
        ruta.setDistanciaTotal(rs.getDouble("distancia_total"));
        ruta.setNumeroEntregas(rs.getInt("numero_entregas"));
        ruta.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        
        Timestamp fechaFin = rs.getTimestamp("fecha_fin");
        if (fechaFin != null) {
            ruta.setFechaFin(fechaFin.toLocalDateTime());
        }
        
        ruta.setCompletada(rs.getBoolean("completada"));
        return ruta;
    }
}