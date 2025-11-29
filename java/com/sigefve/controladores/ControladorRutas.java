package com.sigefve.controladores;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
// import com.sigefve.enums.EstadoEntrega;
import com.sigefve.modelos.Entrega;
import com.sigefve.modelos.Ruta;
import com.sigefve.servicios.RutaServicio;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.google.gson.GsonBuilder;
import com.sigefve.adaptadores.LocalDateTimeTypeAdapter;
import java.time.LocalDateTime;

public class ControladorRutas implements HttpHandler {
    private final RutaServicio rutaServicio;
    private final Gson gson;

    public ControladorRutas() {
        this.rutaServicio = new RutaServicio();
        // this.gson = new Gson();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        configurarCORS(exchange);
        
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            enviarRespuesta(exchange, 200, "");
            return;
        }

        try {
            String metodo = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] partes = path.split("/");

            switch (metodo) {
                case "GET" -> manejarGET(exchange, partes);
                case "POST" -> manejarPOST(exchange, partes);
                case "PUT" -> manejarPUT(exchange, partes);
                default -> enviarError(exchange, 405, "Metodo no permitido");
            }
        } catch (Exception e) {
            enviarError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void manejarGET(HttpExchange exchange, String[] partes) throws IOException {
        try {
            // GET /rutas/:id/entregas
            if (partes.length >= 4 && partes[3].equals("entregas")) {
                Long rutaId = Long.parseLong(partes[2]);
                List<Entrega> entregas = rutaServicio.obtenerEntregasPorRuta(rutaId);
                enviarJSON(exchange, 200, entregas);
            }
            // GET /rutas/:id
            else if (partes.length >= 3 && !partes[2].isEmpty()) {
                Long id = Long.parseLong(partes[2]);
                Optional<Ruta> ruta = rutaServicio.obtenerRuta(id);
                
                if (ruta.isPresent()) {
                    enviarJSON(exchange, 200, ruta.get());
                } else {
                    enviarError(exchange, 404, "Ruta no encontrada");
                }
            }
            // GET /rutas
            else {
                List<Ruta> rutas = rutaServicio.listarRutasActivas();
                enviarJSON(exchange, 200, rutas);
            }
        } catch (Exception e) {
            enviarError(exchange, 500, e.getMessage());
        }
    }

    private void manejarPOST(HttpExchange exchange, String[] partes) throws IOException {
        try {
            String body = leerCuerpo(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // POST /rutas/:id/entregas - Agregar entrega a ruta
            if (partes.length >= 4 && partes[3].equals("entregas")) {
                Long rutaId = Long.parseLong(partes[2]);
                
                Entrega entrega = new Entrega(
                    rutaId,
                    json.get("direccionDestino").getAsString(),
                    json.get("latitud").getAsDouble(),
                    json.get("longitud").getAsDouble(),
                    json.get("descripcionPaquete").getAsString(),
                    json.get("pesoKg").getAsDouble()
                );
                
                Long id = rutaServicio.agregarEntregaARuta(rutaId, entrega);
                
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("id", id);
                respuesta.addProperty("mensaje", "Entrega agregada exitosamente");
                enviarJSON(exchange, 201, respuesta);
            }
            // POST /rutas - Crear nueva ruta
            else {
                Ruta ruta = new Ruta(
                    json.get("nombre").getAsString(),
                    json.has("vehiculoId") ? json.get("vehiculoId").getAsLong() : null,
                    json.get("distanciaTotal").getAsDouble()
                );
                
                Long id = rutaServicio.crearRuta(ruta);
                
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("id", id);
                respuesta.addProperty("mensaje", "Ruta creada exitosamente");
                enviarJSON(exchange, 201, respuesta);
            }
        } catch (Exception e) {
            enviarError(exchange, 400, e.getMessage());
        }
    }

    private void manejarPUT(HttpExchange exchange, String[] partes) throws IOException {
        try {
            String body = leerCuerpo(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // PUT /rutas/:id/asignar - Asignar vehiculo a ruta
            if (partes.length >= 4 && partes[3].equals("asignar")) {
                Long rutaId = Long.parseLong(partes[2]);
                Long vehiculoId = json.get("vehiculoId").getAsLong();
                
                rutaServicio.asignarRutaAVehiculo(rutaId, vehiculoId);
                
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("mensaje", "Vehiculo asignado exitosamente");
                enviarJSON(exchange, 200, respuesta);
            }
            // PUT /rutas/:id/completar - Marcar ruta como completada
            else if (partes.length >= 4 && partes[3].equals("completar")) {
                Long rutaId = Long.parseLong(partes[2]);
                rutaServicio.completarRuta(rutaId);
                
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("mensaje", "Ruta completada exitosamente");
                enviarJSON(exchange, 200, respuesta);
            }
        } catch (Exception e) {
            enviarError(exchange, 400, e.getMessage());
        }
    }

    private void configurarCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String leerCuerpo(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void enviarJSON(HttpExchange exchange, int codigo, Object obj) throws IOException {
        String json = gson.toJson(obj);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        enviarRespuesta(exchange, codigo, json);
    }

    private void enviarError(HttpExchange exchange, int codigo, String mensaje) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", mensaje);
        enviarJSON(exchange, codigo, error);
    }

    private void enviarRespuesta(HttpExchange exchange, int codigo, String respuesta) throws IOException {
        byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}