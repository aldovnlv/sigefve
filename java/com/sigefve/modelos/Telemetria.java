package com.sigefve.modelos;

import java.time.LocalDateTime;

public class Telemetria {
    private Long id;
    private Long vehiculoId;
    private double nivelBateria; // porcentaje
    private double latitud;
    private double longitud;
    private double temperaturaMotor; // C
    private double velocidadActual; // km/h
    private double kilometrajeActual;
    private LocalDateTime timestamp;

    public Telemetria() {
        this.timestamp = LocalDateTime.now();
    }

    public Telemetria(Long vehiculoId, double nivelBateria, double latitud, double longitud,
                     double temperaturaMotor, double velocidadActual, double kilometrajeActual) {
        this();
        this.vehiculoId = vehiculoId;
        this.nivelBateria = nivelBateria;
        this.latitud = latitud;
        this.longitud = longitud;
        this.temperaturaMotor = temperaturaMotor;
        this.velocidadActual = velocidadActual;
        this.kilometrajeActual = kilometrajeActual;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
    
    public double getNivelBateria() { return Math.round(nivelBateria*100.0)/100.00; }
    public void setNivelBateria(double nivelBateria) { this.nivelBateria = nivelBateria; }
    
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    
    public double getTemperaturaMotor() { return Math.round(temperaturaMotor*100.0)/100.0; }
    public void setTemperaturaMotor(double temperaturaMotor) { this.temperaturaMotor = temperaturaMotor; }
    
    public double getVelocidadActual() { return Math.round(velocidadActual*100.0)/100.0; }
    public void setVelocidadActual(double velocidadActual) { this.velocidadActual = velocidadActual; }
    
    public double getKilometrajeActual() { return Math.round(kilometrajeActual*100.0)/100.0; }
    public void setKilometrajeActual(double kilometrajeActual) { this.kilometrajeActual = kilometrajeActual; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}