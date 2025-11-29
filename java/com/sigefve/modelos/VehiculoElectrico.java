package com.sigefve.modelos;

import com.sigefve.enums.TipoVehiculo;

public abstract class VehiculoElectrico extends Vehiculo {
    protected double capacidadBateria; // kWh
    protected double autonomiaMaxima; // km
    protected double consumoPromedio; // kWh/km

    public VehiculoElectrico() {
        super();
    }

    public VehiculoElectrico(String placa, String modelo, int anio, TipoVehiculo tipo,
                            double capacidadBateria, double autonomiaMaxima) {
        super(placa, modelo, anio, tipo);
        this.capacidadBateria = capacidadBateria;
        this.autonomiaMaxima = autonomiaMaxima;
        this.consumoPromedio = capacidadBateria / autonomiaMaxima;
    }

    public double calcularAutonomiaActual(double nivelBateria) {
        return (nivelBateria / 100.0) * autonomiaMaxima;
    }

    // Getters y Setters
    public double getCapacidadBateria() { return capacidadBateria; }
    public void setCapacidadBateria(double capacidadBateria) { this.capacidadBateria = capacidadBateria; }
    
    public double getAutonomiaMaxima() { return autonomiaMaxima; }
    public void setAutonomiaMaxima(double autonomiaMaxima) { this.autonomiaMaxima = autonomiaMaxima; }
    
    public double getConsumoPromedio() { return consumoPromedio; }
    public void setConsumoPromedio(double consumoPromedio) { this.consumoPromedio = Math.round(consumoPromedio*100.0)/100.0; }
}
