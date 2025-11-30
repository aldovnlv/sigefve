package com.sigefve;

import com.sigefve.config.ConfiguracionBaseDatos;
import com.sigefve.controladores.ControladorInicio;
import com.sigefve.controladores.ControladorRutas;
import com.sigefve.controladores.ControladorTelemetria;
import com.sigefve.controladores.ControladorVehiculos;
import com.sigefve.simulador.SimuladorTelemetria;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServidorHTTP {
    private static final int PUERTO = Integer.parseInt(
        System.getenv().getOrDefault("JAVA_PORT", "8585")
    );
    
    private HttpServer servidor;
    private SimuladorTelemetria simulador;

    public void iniciar() throws Exception {
        System.out.println("Iniciando SIGEFVE - Modulo Java");
        System.out.println("=======================================");
        
        // Inicializar base de datos
        System.out.println("Conectando a PostgreSQL...");
        ConfiguracionBaseDatos.obtenerInstancia();
        
        // Crear servidor HTTP
        servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.setExecutor(Executors.newFixedThreadPool(10));
        
        // Registrar controladores
        servidor.createContext("/", new ControladorInicio());
        servidor.createContext("/vehiculos", new ControladorVehiculos());
        servidor.createContext("/telemetria", new ControladorTelemetria());
        servidor.createContext("/rutas", new ControladorRutas());
        
        // Health check
        servidor.createContext("/health", exchange -> {
            String respuesta = "{\"status\":\"OK\",\"servicio\":\"SIGEFVE-Java\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, respuesta.length());
            exchange.getResponseBody().write(respuesta.getBytes());
            exchange.getResponseBody().close();
        });
        
        servidor.start();
        System.out.println("Servidor HTTP iniciado en puerto " + PUERTO);
        
        // Iniciar simulador de telemetria
        simulador = new SimuladorTelemetria();
        simulador.iniciar();
        
        System.out.println("=======================================");
        System.out.println("SIGEFVE - Modulo Java en ejecucion");
        System.out.println("API disponible en: http://localhost:" + PUERTO);
        System.out.println("=======================================\n");
    }

    public void detener() {
        System.out.println("\n Deteniendo servidor...");
        if (simulador != null) {
            simulador.detener();
        }
        if (servidor != null) {
            servidor.stop(0);
        }
        System.out.println("Servidor detenido correctamente");
    }

    public static void main(String[] args) {
        ServidorHTTP servidor = new ServidorHTTP();
        
        // Manejar cierre graceful
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
        
        try {
            servidor.iniciar();
            Thread.currentThread().join(); // Mantener el servidor ejecutandose
        } catch (Exception e) {
            System.err.println(" Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}