package com.sigefve.servicios;

import com.sigefve.dao.TelemetriaDAO;
import com.sigefve.dao.VehiculoDAO;
import com.sigefve.modelos.Telemetria;
import com.sigefve.modelos.VehiculoElectrico;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TelemetriaServicio {
    private final TelemetriaDAO telemetriaDAO;
    private final VehiculoDAO vehiculoDAO;

    public TelemetriaServicio() {
        this.telemetriaDAO = new TelemetriaDAO();
        this.vehiculoDAO = new VehiculoDAO();
    }

    public Long registrarTelemetria(Telemetria telemetria) throws SQLException {
        validarTelemetria(telemetria);
        
        // Actualizar kilometraje del vehiculo
        Optional<VehiculoElectrico> vehiculoOpt = vehiculoDAO.obtenerPorId(telemetria.getVehiculoId());
        if (vehiculoOpt.isPresent()) {
            VehiculoElectrico vehiculo = vehiculoOpt.get();
            if (telemetria.getKilometrajeActual() > vehiculo.getKilometrajeTotal()) {
                vehiculo.setKilometrajeTotal(telemetria.getKilometrajeActual());
                vehiculoDAO.actualizar(vehiculo);
            }
        }
        
        return telemetriaDAO.registrar(telemetria);
    }

    public List<Telemetria> obtenerHistorial(Long vehiculoId, int limite) throws SQLException {
        if (limite <= 0 || limite > 1000) {
            limite = 100; // Limite por defecto
        }
        return telemetriaDAO.obtenerHistorialPorVehiculo(vehiculoId, limite);
    }

    public Telemetria obtenerUltimaTelemetria(Long vehiculoId) throws SQLException {
        return telemetriaDAO.obtenerUltimaPorVehiculo(vehiculoId);
    }

    public List<Telemetria> obtenerTelemetriaPorFechas(Long vehiculoId, LocalDateTime inicio, LocalDateTime fin) throws SQLException {
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha fin");
        }
        return telemetriaDAO.obtenerPorRangoFechas(vehiculoId, inicio, fin);
    }

    private void validarTelemetria(Telemetria telemetria) {
        if (telemetria.getVehiculoId() == null) {
            throw new IllegalArgumentException("El ID del vehiculo es obligatorio");
        }
        if (telemetria.getNivelBateria() < 0 || telemetria.getNivelBateria() > 100) {
            throw new IllegalArgumentException("El nivel de bateria debe estar entre 0 y 100");
        }
        if (telemetria.getLatitud() < -90 || telemetria.getLatitud() > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
        }
        if (telemetria.getLongitud() < -180 || telemetria.getLongitud() > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
        }
    }
}
