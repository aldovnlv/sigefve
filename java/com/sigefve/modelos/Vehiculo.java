package com.sigefve.modelos;

import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.enums.TipoVehiculo;
import java.time.LocalDateTime;

public abstract class Vehiculo {
    protected Long id;
    protected String placa;
    protected String modelo;
    protected int anio;
    protected EstadoVehiculo estado;
    protected TipoVehiculo tipo;
    protected double kilometrajeTotal;
    protected LocalDateTime fechaRegistro;
    protected LocalDateTime ultimaActualizacion;

    public Vehiculo() {
        this.estado = EstadoVehiculo.DISPONIBLE;
        this.fechaRegistro = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
        this.kilometrajeTotal = 0.0;
    }

    public Vehiculo(String placa, String modelo, int anio, TipoVehiculo tipo) {
        this();
        this.placa = placa;
        this.modelo = modelo;
        this.anio = anio;
        this.tipo = tipo;
    }

    public abstract double obtenerCapacidadCarga();
    public abstract double obtenerVelocidadMaxima();
    public abstract String obtenerDescripcion();

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    
    public EstadoVehiculo getEstado() { return estado; }
    public void setEstado(EstadoVehiculo estado) { 
        this.estado = estado;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }
    
    public double getKilometrajeTotal() { return kilometrajeTotal; }
    public void setKilometrajeTotal(double kilometrajeTotal) { this.kilometrajeTotal = kilometrajeTotal; }
    
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}