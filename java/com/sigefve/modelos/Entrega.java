package com.sigefve.modelos;

import com.sigefve.enums.EstadoEntrega;
import java.time.LocalDateTime;

public class Entrega {
    private Long id;
    private Long rutaId;
    private String direccionDestino;
    private double latitud;
    private double longitud;
    private String descripcionPaquete;
    private double pesoKg;
    private EstadoEntrega estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCompletada;
    private String notasEntrega;

    public Entrega() {
        this.estado = EstadoEntrega.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Entrega(Long rutaId, String direccionDestino, double latitud, double longitud,
                  String descripcionPaquete, double pesoKg) {
        this();
        this.rutaId = rutaId;
        this.direccionDestino = direccionDestino;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcionPaquete = descripcionPaquete;
        this.pesoKg = pesoKg;
    }

    public void marcarComoCompletada(String notas) {
        this.estado = EstadoEntrega.COMPLETADA;
        this.fechaCompletada = LocalDateTime.now();
        this.notasEntrega = notas;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getRutaId() { return rutaId; }
    public void setRutaId(Long rutaId) { this.rutaId = rutaId; }
    
    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }
    
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    
    public String getDescripcionPaquete() { return descripcionPaquete; }
    public void setDescripcionPaquete(String descripcionPaquete) { this.descripcionPaquete = descripcionPaquete; }
    
    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
    
    public EstadoEntrega getEstado() { return estado; }
    public void setEstado(EstadoEntrega estado) { this.estado = estado; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaCompletada() { return fechaCompletada; }
    public void setFechaCompletada(LocalDateTime fechaCompletada) { this.fechaCompletada = fechaCompletada; }
    
    public String getNotasEntrega() { return notasEntrega; }
    public void setNotasEntrega(String notasEntrega) { this.notasEntrega = notasEntrega; }
}