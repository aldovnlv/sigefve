package com.sigefve.modelos;

import com.sigefve.enums.TipoVehiculo;

public class Van extends VehiculoElectrico {
    private double capacidadCarga; // kg
    private int numeroAsientos;

    public Van() {
        super();
        this.tipo = TipoVehiculo.VAN;
    }

    public Van(String placa, String modelo, int anio, double capacidadBateria, 
               double autonomiaMaxima, double capacidadCarga, int numeroAsientos) {
        super(placa, modelo, anio, TipoVehiculo.VAN, capacidadBateria, autonomiaMaxima);
        this.capacidadCarga = capacidadCarga;
        this.numeroAsientos = numeroAsientos;
    }

    @Override
    public double obtenerCapacidadCarga() {
        return capacidadCarga;
    }

    @Override
    public double obtenerVelocidadMaxima() {
        return 120.0; // km/h
    }

    @Override
    public String obtenerDescripcion() {
        return String.format("Van %s - Capacidad: %.0f kg - Asientos: %d", 
                           modelo, capacidadCarga, numeroAsientos);
    }

    public double getCapacidadCarga() { return capacidadCarga; }
    public void setCapacidadCarga(double capacidadCarga) { this.capacidadCarga = capacidadCarga; }
    
    public int getNumeroAsientos() { return numeroAsientos; }
    public void setNumeroAsientos(int numeroAsientos) { this.numeroAsientos = numeroAsientos; }
}
