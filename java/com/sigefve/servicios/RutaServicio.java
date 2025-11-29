package com.sigefve.servicios;

import com.sigefve.dao.EntregaDAO;
import com.sigefve.dao.RutaDAO;
import com.sigefve.dao.VehiculoDAO;
import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.modelos.Entrega;
import com.sigefve.modelos.Ruta;
import com.sigefve.modelos.VehiculoElectrico;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RutaServicio {
    private final RutaDAO rutaDAO;
    private final EntregaDAO entregaDAO;
    private final VehiculoDAO vehiculoDAO;

    public RutaServicio() {
        this.rutaDAO = new RutaDAO();
        this.entregaDAO = new EntregaDAO();
        this.vehiculoDAO = new VehiculoDAO();
    }

    public Long crearRuta(Ruta ruta) throws SQLException {
        validarRuta(ruta);
        return rutaDAO.crear(ruta);
    }

    public Optional<Ruta> obtenerRuta(Long id) throws SQLException {
        Optional<Ruta> rutaOpt = rutaDAO.obtenerPorId(id);
        if (rutaOpt.isPresent()) {
            Ruta ruta = rutaOpt.get();
            // Cargar entregas asociadas
            List<Entrega> entregas = entregaDAO.obtenerPorRuta(id);
            ruta.setEntregas(entregas);
        }
        return rutaOpt;
    }

    public List<Ruta> listarRutasActivas() throws SQLException {
        return rutaDAO.obtenerActivas();
    }

    public List<Ruta> listarRutasPorVehiculo(Long vehiculoId) throws SQLException {
        return rutaDAO.obtenerPorVehiculo(vehiculoId);
    }

    public boolean asignarRutaAVehiculo(Long rutaId, Long vehiculoId) throws SQLException {
        // Verificar que el vehiculo existe y esta disponible
        Optional<VehiculoElectrico> vehiculoOpt = vehiculoDAO.obtenerPorId(vehiculoId);
        if (vehiculoOpt.isEmpty()) {
            throw new IllegalArgumentException("Vehiculo no encontrado con ID: " + vehiculoId);
        }

        VehiculoElectrico vehiculo = vehiculoOpt.get();
        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
            throw new IllegalStateException("El vehiculo no esta disponible para asignar rutas");
        }

        // Asignar la ruta
        boolean asignado = rutaDAO.asignarVehiculo(rutaId, vehiculoId);
        
        if (asignado) {
            // Cambiar estado del vehiculo a EN_RUTA
            vehiculoDAO.cambiarEstado(vehiculoId, EstadoVehiculo.EN_RUTA);
        }

        return asignado;
    }

    public Long agregarEntregaARuta(Long rutaId, Entrega entrega) throws SQLException {
        Optional<Ruta> rutaOpt = rutaDAO.obtenerPorId(rutaId);
        if (rutaOpt.isEmpty()) {
            throw new IllegalArgumentException("Ruta no encontrada con ID: " + rutaId);
        }

        entrega.setRutaId(rutaId);
        return entregaDAO.crear(entrega);
    }

    public List<Entrega> obtenerEntregasPorRuta(Long rutaId) throws SQLException {
        return entregaDAO.obtenerPorRuta(rutaId);
    }

    public List<Entrega> obtenerEntregasCompletadasPorVehiculo(Long vehiculoId) throws SQLException {
        return entregaDAO.obtenerCompletadasPorVehiculo(vehiculoId);
    }

    public boolean completarRuta(Long rutaId) throws SQLException {
        Optional<Ruta> rutaOpt = rutaDAO.obtenerPorId(rutaId);
        if (rutaOpt.isEmpty()) {
            throw new IllegalArgumentException("Ruta no encontrada con ID: " + rutaId);
        }

        Ruta ruta = rutaOpt.get();
        boolean completada = rutaDAO.marcarComoCompletada(rutaId);

        if (completada && ruta.getVehiculoId() != null) {
            // Cambiar estado del vehiculo a DISPONIBLE
            vehiculoDAO.cambiarEstado(ruta.getVehiculoId(), EstadoVehiculo.DISPONIBLE);
        }

        return completada;
    }

    private void validarRuta(Ruta ruta) {
        if (ruta.getNombre() == null || ruta.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio");
        }
        if (ruta.getDistanciaTotal() <= 0) {
            throw new IllegalArgumentException("La distancia total debe ser mayor a 0");
        }
    }
}
