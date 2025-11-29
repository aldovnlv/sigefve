package com.sigefve.controladores;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
// import com.google.gson.JsonSerializer;
// import com.google.gson.JsonDeserializer;
// import com.google.gson.*;
import com.sigefve.modelos.Telemetria;
import com.sigefve.servicios.TelemetriaServicio;
import com.sigefve.adaptadores.LocalDateTimeTypeAdapter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
//import java.time.LocalDate;

// import java.lang.reflect.Type;

public class ControladorTelemetria implements HttpHandler {
    private final TelemetriaServicio telemetriaServicio;
    private final Gson gson;

    public ControladorTelemetria() {
        this.telemetriaServicio = new TelemetriaServicio();
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

            if (metodo.equals("GET")) {
                manejarGET(exchange, partes);
            } else if (metodo.equals("POST")) {
                manejarPOST(exchange);
            } else {
                enviarError(exchange, 405, "Metodo no permitido");
            }
        } catch (Exception e) {
            enviarError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void manejarGET(HttpExchange exchange, String[] partes) throws IOException {
        try {
            // GET /telemetria/vehiculo/:id/ultima
            if (partes.length >= 5 && partes[4].equals("ultima")) {
                Long vehiculoId = Long.parseLong(partes[3]);
                Telemetria telemetria = telemetriaServicio.obtenerUltimaTelemetria(vehiculoId);
                
                if (telemetria != null) {
                    enviarJSON(exchange, 200, telemetria);
                } else {
                    enviarError(exchange, 404, "No hay telemetria para este vehiculo");
                }
            }
            // GET /telemetria/vehiculo/:id?limite=100
            else if (partes.length >= 4) {
                Long vehiculoId = Long.parseLong(partes[3]);
                String query = exchange.getRequestURI().getQuery();
                int limite = 100;
                
                if (query != null) {
                    Map<String, String> params = parsearParametros(query);
                    if (params.containsKey("limite")) {
                        limite = Integer.parseInt(params.get("limite"));
                    }
                }
                
                List<Telemetria> historial = telemetriaServicio.obtenerHistorial(vehiculoId, limite);
                enviarJSON(exchange, 200, historial);
            } else {
                enviarError(exchange, 400, "ID de vehiculo requerido");
            }
        } catch (Exception e) {
            enviarError(exchange, 500, e.getClass().getName() + " :> " + e.getMessage());
        }
    }

    private void manejarPOST(HttpExchange exchange) throws IOException {
        try {
            String body = leerCuerpo(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            Telemetria telemetria = new Telemetria(
                json.get("vehiculoId").getAsLong(),
                json.get("nivelBateria").getAsDouble(),
                json.get("latitud").getAsDouble(),
                json.get("longitud").getAsDouble(),
                json.get("temperaturaMotor").getAsDouble(),
                json.get("velocidadActual").getAsDouble(),
                json.get("kilometrajeActual").getAsDouble()
            );
            
            Long id = telemetriaServicio.registrarTelemetria(telemetria);
            
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("id", id);
            respuesta.addProperty("mensaje", "Telemetria registrada exitosamente");
            
            enviarJSON(exchange, 201, respuesta);
        } catch (Exception e) {
            enviarError(exchange, 400, e.getMessage());
        }
    }

    private void configurarCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String leerCuerpo(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parsearParametros(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length == 2) {
                params.put(par[0], par[1]);
            }
        }
        return params;
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