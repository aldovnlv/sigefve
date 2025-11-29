package com.sigefve.utils;

import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.modelos.*;
import com.sigefve.servicios.RutaServicio;
import com.sigefve.servicios.VehiculoServicio;

import java.sql.SQLException;

/**
 * Clase utilitaria para inicializar la base de datos con datos de prueba
 */
public class InicializadorDatos {
    
    private final VehiculoServicio vehiculoServicio;
    private final RutaServicio rutaServicio;

    public InicializadorDatos() {
        this.vehiculoServicio = new VehiculoServicio();
        this.rutaServicio = new RutaServicio();
    }

    public void inicializar() {
        System.out.println("Inicializando datos de prueba...");
        
        try {
            crearVehiculos();
            crearRutas();
            System.out.println("Datos de prueba inicializados correctamente\n");
        } catch (SQLException e) {
            System.err.println("Error al inicializar datos: " + e.getMessage());
        }
    }

    private void crearVehiculos() throws SQLException {
        System.out.println("Creando vehiculos...");
        
        // Crear 5 Vans
        for (int i = 1; i <= 5; i++) {
            Van van = new Van(
                "VAN-" + String.format("%03d", i),
                "Mercedes eSprinter " + (2020 + i % 3),
                2020 + i % 3,
                90.0,  // 90 kWh
                150.0, // 150 km autonomia
                1500.0, // 1500 kg capacidad
                3       // 3 asientos
            );
            
            if (i <= 2) {
                van.setEstado(EstadoVehiculo.DISPONIBLE);
            } else if (i == 3) {
                van.setEstado(EstadoVehiculo.EN_RUTA);
            } else if (i == 4) {
                van.setEstado(EstadoVehiculo.CARGANDO);
            } else {
                van.setEstado(EstadoVehiculo.MANTENIMIENTO);
            }
            
            Long id = vehiculoServicio.crearVehiculo(van);
            System.out.println("  Van creada: " + van.getPlaca() + " (ID: " + id + ")");
        }

        // Crear 5 Bicicletas Electricas
        for (int i = 1; i <= 5; i++) {
            BicicletaElectrica bici = new BicicletaElectrica(
                "BICI-" + String.format("%03d", i),
                "Cargo Bike E-" + i,
                2022 + i % 2,
                1.5,   // 1.5 kWh
                50.0,  // 50 km autonomia
                80.0,  // 80 kg capacidad
                i % 2 == 0  // Mitad con canasta extra
            );
            
            bici.setEstado(i <= 3 ? EstadoVehiculo.DISPONIBLE : EstadoVehiculo.EN_RUTA);
            
            Long id = vehiculoServicio.crearVehiculo(bici);
            System.out.println("  Bicicleta creada: " + bici.getPlaca() + " (ID: " + id + ")");
        }

        // Crear 5 Motos Electricas
        for (int i = 1; i <= 5; i++) {
            MotoElectrica moto = new MotoElectrica(
                "MOTO-" + String.format("%03d", i),
                "Super Soco TC Max",
                2023,
                3.0,   // 3 kWh
                80.0,  // 80 km autonomia
                50.0,  // 50 kg capacidad
                i % 3 == 0  // Algunas con top case
            );
            
            if (i <= 3) {
                moto.setEstado(EstadoVehiculo.DISPONIBLE);
            } else {
                moto.setEstado(EstadoVehiculo.EN_RUTA);
            }
            
            Long id = vehiculoServicio.crearVehiculo(moto);
            System.out.println("  Moto creada: " + moto.getPlaca() + " (ID: " + id + ")");
        }
    }

    private void crearRutas() throws SQLException {
        System.out.println("\n Creando rutas de ejemplo...");
        
        // Ruta 1: Zona Centro
        Ruta rutaCentro = new Ruta("Entregas Zona Centro", null, 15.5);
        Long rutaCentroId = rutaServicio.crearRuta(rutaCentro);
        
        rutaServicio.agregarEntregaARuta(rutaCentroId, new Entrega(
            rutaCentroId,
            "Av. Juarez 123, Centro",
            20.5288,
            -100.8157,
            "Paquete pequenyo - Documentos",
            2.0
        ));
        
        rutaServicio.agregarEntregaARuta(rutaCentroId, new Entrega(
            rutaCentroId,
            "Calle Hidalgo 456, Centro",
            20.5295,
            -100.8165,
            "Paquete mediano - Electronicos",
            5.5
        ));
        
        System.out.println("  Ruta creada: " + rutaCentro.getNombre() + " (ID: " + rutaCentroId + ")");

        // Ruta 2: Zona Norte
        Ruta rutaNorte = new Ruta("Entregas Zona Norte", null, 22.3);
        Long rutaNorteId = rutaServicio.crearRuta(rutaNorte);
        
        rutaServicio.agregarEntregaARuta(rutaNorteId, new Entrega(
            rutaNorteId,
            "Boulevard Norte 789",
            20.5450,
            -100.8200,
            "Paquete grande - Muebles",
            25.0
        ));
        
        System.out.println("  Ruta creada: " + rutaNorte.getNombre() + " (ID: " + rutaNorteId + ")");

        // Ruta 3: Zona Sur
        Ruta rutaSur = new Ruta("Entregas Zona Sur", null, 18.7);
        Long rutaSurId = rutaServicio.crearRuta(rutaSur);
        
        rutaServicio.agregarEntregaARuta(rutaSurId, new Entrega(
            rutaSurId,
            "Av. Sur 321",
            20.5100,
            -100.8100,
            "Paquete pequenyo - Farmacia",
            1.5
        ));
        
        rutaServicio.agregarEntregaARuta(rutaSurId, new Entrega(
            rutaSurId,
            "Calle Morelos 654",
            20.5080,
            -100.8120,
            "Paquete mediano - Alimentos",
            8.0
        ));
        
        rutaServicio.agregarEntregaARuta(rutaSurId, new Entrega(
            rutaSurId,
            "Av. Revolucion 987",
            20.5050,
            -100.8150,
            "Paquete grande - Equipamiento",
            15.0
        ));
        
        System.out.println("  Ruta creada: " + rutaSur.getNombre() + " (ID: " + rutaSurId + ")");
    }

    public static void main(String[] args) {
        InicializadorDatos inicializador = new InicializadorDatos();
        inicializador.inicializar();
    }
}