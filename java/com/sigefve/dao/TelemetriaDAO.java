package com.sigefve.dao;

import com.sigefve.config.ConfiguracionBaseDatos;
import com.sigefve.modelos.Telemetria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TelemetriaDAO {
    private final ConfiguracionBaseDatos config;

    public TelemetriaDAO() {
        this.config = ConfiguracionBaseDatos.obtenerInstancia();
    }

    public Long registrar(Telemetria telemetria) throws SQLException {
        String sql = """
            INSERT INTO telemetria (vehiculo_id, nivel_bateria, latitud, longitud,
                                   temperatura_motor, velocidad_actual, kilometraje_actual)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, telemetria.getVehiculoId());
            stmt.setDouble(2, telemetria.getNivelBateria());
            stmt.setDouble(3, telemetria.getLatitud());
            stmt.setDouble(4, telemetria.getLongitud());
            stmt.setDouble(5, telemetria.getTemperaturaMotor());
            stmt.setDouble(6, telemetria.getVelocidadActual());
            stmt.setDouble(7, telemetria.getKilometrajeActual());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    telemetria.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<Telemetria> obtenerHistorialPorVehiculo(Long vehiculoId, int limite) throws SQLException {
        String sql = """
            SELECT * FROM telemetria 
            WHERE vehiculo_id = ? 
            ORDER BY timestamp DESC 
            LIMIT ?
            """;

        List<Telemetria> historial = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculoId);
            stmt.setInt(2, limite);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                historial.add(mapearTelemetria(rs));
            }
        }
        return historial;
    }

    public Telemetria obtenerUltimaPorVehiculo(Long vehiculoId) throws SQLException {
        String sql = """
            SELECT * FROM telemetria 
            WHERE vehiculo_id = ? 
            ORDER BY timestamp DESC 
            LIMIT 1
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearTelemetria(rs);
            }
        }
        return null;
    }

    public List<Telemetria> obtenerPorRangoFechas(Long vehiculoId, LocalDateTime inicio, LocalDateTime fin) throws SQLException {
        String sql = """
            SELECT * FROM telemetria 
            WHERE vehiculo_id = ? AND timestamp BETWEEN ? AND ?
            ORDER BY timestamp DESC
            """;

        List<Telemetria> resultados = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculoId);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio));
            stmt.setTimestamp(3, Timestamp.valueOf(fin));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultados.add(mapearTelemetria(rs));
            }
        }
        return resultados;
    }

    private Telemetria mapearTelemetria(ResultSet rs) throws SQLException {
        Telemetria t = new Telemetria();
        t.setId(rs.getLong("id"));
        t.setVehiculoId(rs.getLong("vehiculo_id"));
        t.setNivelBateria(rs.getDouble("nivel_bateria"));
        t.setLatitud(rs.getDouble("latitud"));
        t.setLongitud(rs.getDouble("longitud"));
        t.setTemperaturaMotor(rs.getDouble("temperatura_motor"));
        t.setVelocidadActual(rs.getDouble("velocidad_actual"));
        t.setKilometrajeActual(rs.getDouble("kilometraje_actual"));
        t.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return t;
    }
}