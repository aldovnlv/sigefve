package com.sigefve.modelos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ruta {
    private Long id;
    private String nombre;
    private Long vehiculoId;
    private double distanciaTotal; // km
    private int numeroEntregas;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean completada;
    private List<Entrega> entregas;

    public Ruta() {
        this.entregas = new ArrayList<>();
        this.completada = false;
    }

    public Ruta(String nombre, Long vehiculoId, double distanciaTotal) {
        this();
        this.nombre = nombre;
        this.vehiculoId = vehiculoId;
        this.distanciaTotal = distanciaTotal;
        this.fechaInicio = LocalDateTime.now();
    }

    public void agregarEntrega(Entrega entrega) {
        this.entregas.add(entrega);
        this.numeroEntregas = this.entregas.size();
    }

    public int contarEntregasCompletadas() {
        return (int) entregas.stream()
            .filter(e -> e.getEstado() == com.sigefve.enums.EstadoEntrega.COMPLETADA)
            .count();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
    
    public double getDistanciaTotal() { return distanciaTotal; }
    public void setDistanciaTotal(double distanciaTotal) { this.distanciaTotal = distanciaTotal; }
    
    public int getNumeroEntregas() { return numeroEntregas; }
    public void setNumeroEntregas(int numeroEntregas) { this.numeroEntregas = numeroEntregas; }
    
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    
    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { 
        this.completada = completada;
        if (completada && this.fechaFin == null) {
            this.fechaFin = LocalDateTime.now();
        }
    }
    
    public List<Entrega> getEntregas() { return entregas; }
    public void setEntregas(List<Entrega> entregas) { 
        this.entregas = entregas;
        this.numeroEntregas = entregas != null ? entregas.size() : 0;
    }
}