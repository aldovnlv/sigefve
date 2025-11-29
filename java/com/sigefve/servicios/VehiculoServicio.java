package com.sigefve.servicios;

import com.sigefve.dao.VehiculoDAO;
import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.modelos.VehiculoElectrico;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class VehiculoServicio {
    private final VehiculoDAO vehiculoDAO;

    public VehiculoServicio() {
        this.vehiculoDAO = new VehiculoDAO();
    }

    public Long crearVehiculo(VehiculoElectrico vehiculo) throws SQLException {
        validarVehiculo(vehiculo);
        return vehiculoDAO.crear(vehiculo);
    }

    public Optional<VehiculoElectrico> obtenerVehiculo(Long id) throws SQLException {
        return vehiculoDAO.obtenerPorId(id);
    }

    public List<VehiculoElectrico> listarTodosLosVehiculos() throws SQLException {
        return vehiculoDAO.obtenerTodos();
    }

    public List<VehiculoElectrico> listarVehiculosDisponibles() throws SQLException {
        return vehiculoDAO.obtenerPorEstado(EstadoVehiculo.DISPONIBLE);
    }

    public void actualizarVehiculo(VehiculoElectrico vehiculo) throws SQLException {
        if (vehiculo.getId() == null) {
            throw new IllegalArgumentException("El ID del vehiculo no puede ser nulo");
        }
        validarVehiculo(vehiculo);
        vehiculoDAO.actualizar(vehiculo);
    }

    public boolean cambiarEstadoVehiculo(Long id, EstadoVehiculo nuevoEstado) throws SQLException {
        Optional<VehiculoElectrico> vehiculoOpt = vehiculoDAO.obtenerPorId(id);
        if (vehiculoOpt.isEmpty()) {
            throw new IllegalArgumentException("Vehiculo no encontrado con ID: " + id);
        }

        return vehiculoDAO.cambiarEstado(id, nuevoEstado);
    }

    public boolean eliminarVehiculo(Long id) throws SQLException {
        return vehiculoDAO.eliminar(id);
    }

    private void validarVehiculo(VehiculoElectrico vehiculo) {
        if (vehiculo.getPlaca() == null || vehiculo.getPlaca().trim().isEmpty()) {
            throw new IllegalArgumentException("La placa del vehiculo es obligatoria");
        }
        if (vehiculo.getModelo() == null || vehiculo.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo del vehiculo es obligatorio");
        }
        if (vehiculo.getAnio() < 2000 || vehiculo.getAnio() > 2030) {
            throw new IllegalArgumentException("El anyo del vehiculo debe estar entre 2000 y 2030");
        }
        if (vehiculo.getCapacidadBateria() <= 0) {
            throw new IllegalArgumentException("La capacidad de bateria debe ser mayor a 0");
        }
        if (vehiculo.getAutonomiaMaxima() <= 0) {
            throw new IllegalArgumentException("La autonomia maxima debe ser mayor a 0");
        }
    }
}