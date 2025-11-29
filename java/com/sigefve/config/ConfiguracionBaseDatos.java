package com.sigefve.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
//import org.postgresql.*;

public class ConfiguracionBaseDatos {
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://postgresql_java:5432/sigefve");
    private static final String USUARIO = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String CONTRASENA = System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    
    private static ConfiguracionBaseDatos instancia;
    
    private ConfiguracionBaseDatos() {
        try {
            Class.forName("org.postgresql.Driver");
            inicializarEsquema();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado", e);
        }
    }
    
    public static ConfiguracionBaseDatos obtenerInstancia() {
        if (instancia == null) {
            synchronized (ConfiguracionBaseDatos.class) {
                if (instancia == null) {
                    instancia = new ConfiguracionBaseDatos();
                }
            }
        }
        return instancia;
    }
    
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
    
    private void inicializarEsquema() {
        String[] sqlCreacionTablas = {
            // Tabla vehiculos
            """
            CREATE TABLE IF NOT EXISTS vehiculos (
                id SERIAL PRIMARY KEY,
                placa VARCHAR(20) UNIQUE NOT NULL,
                modelo VARCHAR(100) NOT NULL,
                anio INTEGER NOT NULL,
                tipo VARCHAR(50) NOT NULL,
                estado VARCHAR(50) NOT NULL,
                capacidad_bateria DOUBLE PRECISION,
                autonomia_maxima DOUBLE PRECISION,
                consumo_promedio DOUBLE PRECISION,
                capacidad_carga DOUBLE PRECISION,
                numero_asientos INTEGER,
                tiene_canasta_extra BOOLEAN,
                tiene_top_case BOOLEAN,
                kilometraje_total DOUBLE PRECISION DEFAULT 0,
                fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // Tabla telemetria
            """
            CREATE TABLE IF NOT EXISTS telemetria (
                id SERIAL PRIMARY KEY,
                vehiculo_id INTEGER NOT NULL,
                nivel_bateria DOUBLE PRECISION NOT NULL,
                latitud DOUBLE PRECISION NOT NULL,
                longitud DOUBLE PRECISION NOT NULL,
                temperatura_motor DOUBLE PRECISION NOT NULL,
                velocidad_actual DOUBLE PRECISION NOT NULL,
                kilometraje_actual DOUBLE PRECISION NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id) ON DELETE CASCADE
            )
            """,
            
            // Tabla rutas
            """
            CREATE TABLE IF NOT EXISTS rutas (
                id SERIAL PRIMARY KEY,
                nombre VARCHAR(200) NOT NULL,
                vehiculo_id INTEGER,
                distancia_total DOUBLE PRECISION NOT NULL,
                numero_entregas INTEGER DEFAULT 0,
                fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                fecha_fin TIMESTAMP,
                completada BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id) ON DELETE SET NULL
            )
            """,
            
            // Tabla entregas
            """
            CREATE TABLE IF NOT EXISTS entregas (
                id SERIAL PRIMARY KEY,
                ruta_id INTEGER NOT NULL,
                direccion_destino VARCHAR(300) NOT NULL,
                latitud DOUBLE PRECISION NOT NULL,
                longitud DOUBLE PRECISION NOT NULL,
                descripcion_paquete TEXT,
                peso_kg DOUBLE PRECISION NOT NULL,
                estado VARCHAR(50) NOT NULL,
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                fecha_completada TIMESTAMP,
                notas_entrega TEXT,
                FOREIGN KEY (ruta_id) REFERENCES rutas(id) ON DELETE CASCADE
            )
            """,
            
            // indices para mejorar rendimiento
            "CREATE INDEX IF NOT EXISTS idx_telemetria_vehiculo ON telemetria(vehiculo_id)",
            "CREATE INDEX IF NOT EXISTS idx_telemetria_timestamp ON telemetria(timestamp DESC)",
            "CREATE INDEX IF NOT EXISTS idx_rutas_vehiculo ON rutas(vehiculo_id)",
            "CREATE INDEX IF NOT EXISTS idx_entregas_ruta ON entregas(ruta_id)",
            "CREATE INDEX IF NOT EXISTS idx_vehiculos_estado ON vehiculos(estado)"
        };
        
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement()) {
            
            for (String sql : sqlCreacionTablas) {
                stmt.execute(sql);
            }
            
            System.out.println("Esquema de base de datos inicializado correctamente");
            
        } catch (SQLException e) {
            System.err.println("Error al inicializar esquema: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar la base de datos", e);
        }
    }
}