package com.sigefve.modelos;

import com.sigefve.enums.TipoVehiculo;

public class MotoElectrica extends VehiculoElectrico {
    private double capacidadCarga; // kg
    private boolean tieneTopCase;

    public MotoElectrica() {
        super();
        this.tipo = TipoVehiculo.MOTO_ELECTRICA;
    }

    public MotoElectrica(String placa, String modelo, int anio, double capacidadBateria,
                        double autonomiaMaxima, double capacidadCarga, boolean tieneTopCase) {
        super(placa, modelo, anio, TipoVehiculo.MOTO_ELECTRICA, capacidadBateria, autonomiaMaxima);
        this.capacidadCarga = capacidadCarga;
        this.tieneTopCase = tieneTopCase;
    }

    @Override
    public double obtenerCapacidadCarga() {
        return tieneTopCase ? capacidadCarga * 1.3 : capacidadCarga;
    }

    @Override
    public double obtenerVelocidadMaxima() {
        return 80.0; // km/h
    }

    @Override
    public String obtenerDescripcion() {
        return String.format("Moto Electrica %s - Capacidad: %.0f kg%s", 
                           modelo, obtenerCapacidadCarga(),
                           tieneTopCase ? " (con top case)" : "");
    }

    public double getCapacidadCarga() { return capacidadCarga; }
    public void setCapacidadCarga(double capacidadCarga) { this.capacidadCarga = capacidadCarga; }
    
    public boolean isTieneTopCase() { return tieneTopCase; }
    public void setTieneTopCase(boolean tieneTopCase) { this.tieneTopCase = tieneTopCase; }
}