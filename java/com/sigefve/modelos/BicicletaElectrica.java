package com.sigefve.modelos;

import com.sigefve.enums.TipoVehiculo;

public class BicicletaElectrica extends VehiculoElectrico {
    private double capacidadCarga; // kg
    private boolean tieneCanastaExtra;

    public BicicletaElectrica() {
        super();
        this.tipo = TipoVehiculo.BICICLETA_ELECTRICA;
    }

    public BicicletaElectrica(String placa, String modelo, int anio, double capacidadBateria,
                             double autonomiaMaxima, double capacidadCarga, boolean tieneCanastaExtra) {
        super(placa, modelo, anio, TipoVehiculo.BICICLETA_ELECTRICA, capacidadBateria, autonomiaMaxima);
        this.capacidadCarga = capacidadCarga;
        this.tieneCanastaExtra = tieneCanastaExtra;
    }

    @Override
    public double obtenerCapacidadCarga() {
        return tieneCanastaExtra ? capacidadCarga * 1.5 : capacidadCarga;
    }

    @Override
    public double obtenerVelocidadMaxima() {
        return 25.0; // km/h
    }

    @Override
    public String obtenerDescripcion() {
        return String.format("Bicicleta Electrica %s - Capacidad: %.0f kg%s", 
                           modelo, obtenerCapacidadCarga(),
                           tieneCanastaExtra ? " (con canasta extra)" : "");
    }

    public double getCapacidadCarga() { return capacidadCarga; }
    public void setCapacidadCarga(double capacidadCarga) { this.capacidadCarga = capacidadCarga; }
    
    public boolean isTieneCanastaExtra() { return tieneCanastaExtra; }
    public void setTieneCanastaExtra(boolean tieneCanastaExtra) { this.tieneCanastaExtra = tieneCanastaExtra; }
}