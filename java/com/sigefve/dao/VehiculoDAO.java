package com.sigefve.dao;

import com.sigefve.config.ConfiguracionBaseDatos;
import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.enums.TipoVehiculo;
import com.sigefve.modelos.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculoDAO {
    private final ConfiguracionBaseDatos config;

    public VehiculoDAO() {
        this.config = ConfiguracionBaseDatos.obtenerInstancia();
    }

    public Long crear(VehiculoElectrico vehiculo) throws SQLException {
        String sql = """
            INSERT INTO vehiculos (placa, modelo, anio, tipo, estado, capacidad_bateria, 
                                 autonomia_maxima, consumo_promedio, capacidad_carga,
                                 numero_asientos, tiene_canasta_extra, tiene_top_case)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehiculo.getPlaca());
            stmt.setString(2, vehiculo.getModelo());
            stmt.setInt(3, vehiculo.getAnio());
            stmt.setString(4, vehiculo.getTipo().name());
            stmt.setString(5, vehiculo.getEstado().name());
            stmt.setDouble(6, vehiculo.getCapacidadBateria());
            stmt.setDouble(7, vehiculo.getAutonomiaMaxima());
            stmt.setDouble(8, vehiculo.getConsumoPromedio());

            // Campos especificos por tipo
            if (vehiculo instanceof Van van) {
                stmt.setDouble(9, van.getCapacidadCarga());
                stmt.setInt(10, van.getNumeroAsientos());
                stmt.setNull(11, Types.BOOLEAN);
                stmt.setNull(12, Types.BOOLEAN);
            } else if (vehiculo instanceof BicicletaElectrica bici) {
                stmt.setDouble(9, bici.getCapacidadCarga());
                stmt.setNull(10, Types.INTEGER);
                stmt.setBoolean(11, bici.isTieneCanastaExtra());
                stmt.setNull(12, Types.BOOLEAN);
            } else if (vehiculo instanceof MotoElectrica moto) {
                stmt.setDouble(9, moto.getCapacidadCarga());
                stmt.setNull(10, Types.INTEGER);
                stmt.setNull(11, Types.BOOLEAN);
                stmt.setBoolean(12, moto.isTieneTopCase());
            }

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo crear el vehiculo");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    vehiculo.setId(id);
                    return id;
                } else {
                    throw new SQLException("No se pudo obtener el ID del vehiculo creado");
                }
            }
        }
    }

    public Optional<VehiculoElectrico> obtenerPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM vehiculos WHERE id = ?";

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearVehiculo(rs));
            }
            return Optional.empty();
        }
    }

    public List<VehiculoElectrico> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM vehiculos ORDER BY id";
        List<VehiculoElectrico> vehiculos = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehiculos.add(mapearVehiculo(rs));
            }
        }
        return vehiculos;
    }

    public List<VehiculoElectrico> obtenerPorEstado(EstadoVehiculo estado) throws SQLException {
        String sql = "SELECT * FROM vehiculos WHERE estado = ? ORDER BY id";
        List<VehiculoElectrico> vehiculos = new ArrayList<>();

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vehiculos.add(mapearVehiculo(rs));
            }
        }
        return vehiculos;
    }

    public void actualizar(VehiculoElectrico vehiculo) throws SQLException {
        String sql = """
            UPDATE vehiculos 
            SET placa = ?, modelo = ?, anio = ?, estado = ?, 
                capacidad_bateria = ?, autonomia_maxima = ?, 
                kilometraje_total = ?, ultima_actualizacion = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehiculo.getPlaca());
            stmt.setString(2, vehiculo.getModelo());
            stmt.setInt(3, vehiculo.getAnio());
            stmt.setString(4, vehiculo.getEstado().name());
            stmt.setDouble(5, vehiculo.getCapacidadBateria());
            stmt.setDouble(6, vehiculo.getAutonomiaMaxima());
            stmt.setDouble(7, vehiculo.getKilometrajeTotal());
            stmt.setLong(8, vehiculo.getId());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se encontro el vehiculo con ID: " + vehiculo.getId());
            }
        }
    }

    public boolean cambiarEstado(Long id, EstadoVehiculo nuevoEstado) throws SQLException {
        String sql = "UPDATE vehiculos SET estado = ?, ultima_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado.name());
            stmt.setLong(2, id);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM vehiculos WHERE id = ?";

        try (Connection conn = config.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private VehiculoElectrico mapearVehiculo(ResultSet rs) throws SQLException {
        TipoVehiculo tipo = TipoVehiculo.valueOf(rs.getString("tipo"));
        VehiculoElectrico vehiculo;

        switch (tipo) {
            case VAN -> {
                Van van = new Van();
                van.setNumeroAsientos(rs.getInt("numero_asientos"));
                van.setCapacidadCarga(rs.getDouble("capacidad_carga"));
                vehiculo = van;
            }
            case BICICLETA_ELECTRICA -> {
                BicicletaElectrica bici = new BicicletaElectrica();
                bici.setCapacidadCarga(rs.getDouble("capacidad_carga"));
                bici.setTieneCanastaExtra(rs.getBoolean("tiene_canasta_extra"));
                vehiculo = bici;
            }
            case MOTO_ELECTRICA -> {
                MotoElectrica moto = new MotoElectrica();
                moto.setCapacidadCarga(rs.getDouble("capacidad_carga"));
                moto.setTieneTopCase(rs.getBoolean("tiene_top_case"));
                vehiculo = moto;
            }
            default -> throw new SQLException("Tipo de vehiculo desconocido: " + tipo);
        }

        vehiculo.setId(rs.getLong("id"));
        vehiculo.setPlaca(rs.getString("placa"));
        vehiculo.setModelo(rs.getString("modelo"));
        vehiculo.setAnio(rs.getInt("anio"));
        vehiculo.setEstado(EstadoVehiculo.valueOf(rs.getString("estado")));
        vehiculo.setCapacidadBateria(rs.getDouble("capacidad_bateria"));
        vehiculo.setAutonomiaMaxima(rs.getDouble("autonomia_maxima"));
        vehiculo.setConsumoPromedio(rs.getDouble("consumo_promedio"));
        vehiculo.setKilometrajeTotal(rs.getDouble("kilometraje_total"));
        vehiculo.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        vehiculo.setUltimaActualizacion(rs.getTimestamp("ultima_actualizacion").toLocalDateTime());

        return vehiculo;
    }
}
