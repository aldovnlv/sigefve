package com.sigefve.simulador;

import com.sigefve.adaptadores.ClienteAPI;

import com.sigefve.dao.VehiculoDAO;
// import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.modelos.Telemetria;
import com.sigefve.modelos.VehiculoElectrico;
import com.sigefve.servicios.TelemetriaServicio;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimuladorTelemetria {
    private final TelemetriaServicio telemetriaServicio;
    private final VehiculoDAO vehiculoDAO;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    private final Map<Long, EstadoSimulacion> estadosVehiculos;
    
    // Coordenadas base (Celaya, Guanajuato, Mexico)
    private static final double LATITUD_BASE = 18.85923285;
    private static final double LONGITUD_BASE = -97.1106537405747;
    private static final double RADIO_OPERACION = 0.1; // ~11 km
    private ClienteAPI peticion = new ClienteAPI();

    public SimuladorTelemetria() {
        this.telemetriaServicio = new TelemetriaServicio();
        this.vehiculoDAO = new VehiculoDAO();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.random = new Random();
        this.estadosVehiculos = new HashMap<>();
    }

    public void iniciar() {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      1      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println("Iniciando simulador de telemetria...");
        
        // Programar la generacion de telemetria cada 15 segundos
        scheduler.scheduleAtFixedRate(() -> {
            try {
                generarTelemetriaParaTodos();
            } catch (Exception e) {
                System.err.println("Error en simulacion: " + e.getMessage());
            }
        }, 0, 15, TimeUnit.SECONDS);

        System.out.println("Simulador iniciado. Generando telemetria cada 15 segundos...");
    }

    public void detener() {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      2      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println("Deteniendo simulador de telemetria...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void generarTelemetriaParaTodos() throws SQLException {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      3      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        List<VehiculoElectrico> vehiculos = vehiculoDAO.obtenerTodos();
        
        for (VehiculoElectrico vehiculo : vehiculos) {
            EstadoSimulacion estado = estadosVehiculos.computeIfAbsent(
                vehiculo.getId(), 
                id -> new EstadoSimulacion(vehiculo)
            );
            
            Telemetria telemetria = generarTelemetria(vehiculo, estado);
            telemetriaServicio.registrarTelemetria(telemetria);

            // Envio hacia Python
            String json = "{\n    \"id_vehiculo\":"+vehiculo.getId()+",\n    \"nivel_bateria\":"+Math.round(telemetria.getNivelBateria() * 100.0)/100.0+",\n    \"temperatura\":"+Math.round(telemetria.getTemperaturaMotor()*100.0)/100.0+"}";
            peticion.peticionPost("telemetria", json);
            
            // Log periodico cada minuto (cada 4 ciclos de 15s)
            if (estado.ciclos % 4 == 0) {
                System.out.printf("[%s] Bateria: %.6f%% | Temp: %.6f C | Km: %.2f%n",
                    vehiculo.getPlaca(),
                    telemetria.getNivelBateria(),
                    telemetria.getTemperaturaMotor(),
                    telemetria.getKilometrajeActual()
                );
            }
            
            estado.ciclos++;
        }
    }

    private Telemetria generarTelemetria(VehiculoElectrico vehiculo, EstadoSimulacion estado) {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      4      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Telemetria telemetria = new Telemetria();
        telemetria.setVehiculoId(vehiculo.getId());
        
        // Generar datos segun el estado del vehiculo
        switch (vehiculo.getEstado()) {
            case EN_RUTA -> generarTelemetriaEnRuta(telemetria, vehiculo, estado);
            case CARGANDO -> generarTelemetriaCargando(telemetria, vehiculo, estado);
            case MANTENIMIENTO -> generarTelemetriaMantenimiento(telemetria, vehiculo, estado);
            default -> generarTelemetriaDisponible(telemetria, vehiculo, estado);
        }

        return telemetria;
    }

    private void generarTelemetriaEnRuta(Telemetria t, VehiculoElectrico v, EstadoSimulacion e) {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      5      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        // Velocidad variable segun tipo de vehiculo
        double velocidadMaxima = v.obtenerVelocidadMaxima();
        t.setVelocidadActual(velocidadMaxima * (0.5 + random.nextDouble() * 0.5));
        
        // Consumo de bateria (0.5% - 2% cada 15 segundos)
        e.nivelBateria -= 0.5 + random.nextDouble() * 1.5;
        e.nivelBateria = Math.max(0, e.nivelBateria);
        t.setNivelBateria(e.nivelBateria);
        
        // Incrementar kilometraje (distancia = velocidad * tiempo)
        double kmRecorridos = t.getVelocidadActual() * (15.0 / 3600.0); // 15 seg a horas
        e.kilometraje += kmRecorridos;
        t.setKilometrajeActual(e.kilometraje);
        
        // Temperatura del motor (aumenta con el uso)
        e.temperaturaMotor = Math.min(75, e.temperaturaMotor + random.nextDouble() * 2);
        t.setTemperaturaMotor(e.temperaturaMotor);
        
        // Ubicacion (movimiento simulado)
        actualizarUbicacion(e);
        t.setLatitud(e.latitud);
        t.setLongitud(e.longitud);
    }

    private void generarTelemetriaCargando(Telemetria t, VehiculoElectrico v, EstadoSimulacion e) {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      6      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        t.setVelocidadActual(0);
        
        // Incremento de bateria (2% - 5% cada 15 segundos)
        e.nivelBateria += 2 + random.nextDouble() * 3;
        e.nivelBateria = Math.min(100, e.nivelBateria);
        t.setNivelBateria(e.nivelBateria);
        
        // Sin movimiento
        t.setKilometrajeActual(e.kilometraje);
        
        // Temperatura estable o bajando
        e.temperaturaMotor = Math.max(25, e.temperaturaMotor - random.nextDouble());
        t.setTemperaturaMotor(e.temperaturaMotor);
        
        // Ubicacion fija
        t.setLatitud(e.latitud);
        t.setLongitud(e.longitud);
    }

    private void generarTelemetriaMantenimiento(Telemetria t, VehiculoElectrico v, EstadoSimulacion e) {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      7      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        t.setVelocidadActual(0);
        t.setNivelBateria(e.nivelBateria);
        t.setKilometrajeActual(e.kilometraje);
        
        // Temperatura ambiente
        e.temperaturaMotor = 20 + random.nextDouble() * 5;
        t.setTemperaturaMotor(e.temperaturaMotor);
        
        t.setLatitud(e.latitud);
        t.setLongitud(e.longitud);
    }

    private void generarTelemetriaDisponible(Telemetria t, VehiculoElectrico v, EstadoSimulacion e) {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      8      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        t.setVelocidadActual(0);
        
        // Bateria estable o carga lenta
        if (e.nivelBateria < 80) {
            e.nivelBateria += random.nextDouble() * 0.5;
        }
        t.setNivelBateria(Math.min(100, e.nivelBateria));
        
        t.setKilometrajeActual(e.kilometraje);
        
        // Temperatura ambiente
        e.temperaturaMotor = 20 + random.nextDouble() * 10;
        t.setTemperaturaMotor(e.temperaturaMotor);
        
        t.setLatitud(e.latitud);
        t.setLongitud(e.longitud);
    }

    private void actualizarUbicacion(EstadoSimulacion estado) {
        // System.out.println(" >>>>>>>>>>>>>>>>>>>>      9      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        // Movimiento aleatorio dentro del radio de operacion
        double angulo = random.nextDouble() * 2 * Math.PI;
        double distancia = random.nextDouble() * 0.002; // ~200 metros
        
        estado.latitud += distancia * Math.cos(angulo);
        estado.longitud += distancia * Math.sin(angulo);
        
        // Mantener dentro del area de operacion
        double distanciaDelCentro = Math.sqrt(
            Math.pow(estado.latitud - LATITUD_BASE, 2) + 
            Math.pow(estado.longitud - LONGITUD_BASE, 2)
        );
        
        if (distanciaDelCentro > RADIO_OPERACION) {
            estado.latitud = LATITUD_BASE;
            estado.longitud = LONGITUD_BASE;
        }
    }

    // Clase interna para mantener el estado de simulacion
    private static class EstadoSimulacion {
        double nivelBateria;
        double latitud;
        double longitud;
        double kilometraje;
        double temperaturaMotor;
        int ciclos;

        EstadoSimulacion(VehiculoElectrico vehiculo) {
            // Inicializar con valores aleatorios pero realistas
            this.nivelBateria = 60 + new Random().nextDouble() * 30; // 60-90%
            this.latitud = LATITUD_BASE + (new Random().nextDouble() - 0.5) * RADIO_OPERACION;
            this.longitud = LONGITUD_BASE + (new Random().nextDouble() - 0.5) * RADIO_OPERACION;
            this.kilometraje = vehiculo.getKilometrajeTotal();
            this.temperaturaMotor = 25 + new Random().nextDouble() * 15;
            this.ciclos = 0;
        }
    }
}
