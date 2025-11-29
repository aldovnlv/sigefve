package com.sigefve.controladores;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigefve.enums.EstadoVehiculo;
import com.sigefve.enums.TipoVehiculo;
import com.sigefve.modelos.*;
import com.sigefve.servicios.VehiculoServicio;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.GsonBuilder;
import com.sigefve.adaptadores.LocalDateTimeTypeAdapter;
import java.time.LocalDateTime;

public class ControladorVehiculos implements HttpHandler {
    private final VehiculoServicio vehiculoServicio;
    private final Gson gson;

    public ControladorVehiculos() {
        this.vehiculoServicio = new VehiculoServicio();
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
                case "POST" -> manejarPOST(exchange);
                case "PUT" -> manejarPUT(exchange, partes);
                case "DELETE" -> manejarDELETE(exchange, partes);
                default -> enviarError(exchange, 405, "Metodo no permitido");
            }
        } catch (Exception e) {
            enviarError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void manejarGET(HttpExchange exchange, String[] partes) throws IOException {
        try {
            // GET /vehiculos/:id
            if (partes.length >= 3 && !partes[2].isEmpty()) {
                Long id = Long.parseLong(partes[2]);
                Optional<VehiculoElectrico> vehiculo = vehiculoServicio.obtenerVehiculo(id);
                
                if (vehiculo.isPresent()) {
                    enviarJSON(exchange, 200, vehiculo.get());
                } else {
                    enviarError(exchange, 404, "Vehiculo no encontrado");
                }
            }
            // GET /vehiculos?estado=DISPONIBLE
            else if (exchange.getRequestURI().getQuery() != null) {
                Map<String, String> params = parsearParametros(exchange.getRequestURI().getQuery());
                
                if (params.containsKey("estado")) {
                    // EstadoVehiculo estado = EstadoVehiculo.valueOf(params.get("estado"));
                    List<VehiculoElectrico> vehiculos = vehiculoServicio.listarVehiculosDisponibles();
                    enviarJSON(exchange, 200, vehiculos);
                } else {
                    List<VehiculoElectrico> vehiculos = vehiculoServicio.listarTodosLosVehiculos();
                    enviarJSON(exchange, 200, vehiculos);
                }
            }
            // GET /vehiculos
            else {
                List<VehiculoElectrico> vehiculos = vehiculoServicio.listarTodosLosVehiculos();
                enviarJSON(exchange, 200, vehiculos);
            }
        } catch (Exception e) {
            enviarError(exchange, 500, e.getMessage());
        }
    }

    private void manejarPOST(HttpExchange exchange) throws IOException {
        try {
            String body = leerCuerpo(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            VehiculoElectrico vehiculo = crearVehiculoDesdeJSON(json);
            Long id = vehiculoServicio.crearVehiculo(vehiculo);
            
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("id", id);
            respuesta.addProperty("mensaje", "Vehiculo creado exitosamente");
            
            enviarJSON(exchange, 201, respuesta);
        } catch (Exception e) {
            enviarError(exchange, 400, e.getMessage());
        }
    }

    private void manejarPUT(HttpExchange exchange, String[] partes) throws IOException {
        try {
            if (partes.length < 3 || partes[2].isEmpty()) {
                enviarError(exchange, 400, "ID de vehiculo requerido");
                return;
            }

            Long id = Long.parseLong(partes[2]);
            String body = leerCuerpo(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // PUT /vehiculos/:id/estado - Cambiar solo el estado
            if (partes.length >= 4 && partes[3].equals("estado")) {
                String nuevoEstado = json.get("estado").getAsString();
                vehiculoServicio.cambiarEstadoVehiculo(id, EstadoVehiculo.valueOf(nuevoEstado));
                
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("mensaje", "Estado actualizado exitosamente");
                enviarJSON(exchange, 200, respuesta);
            }
            // PUT /vehiculos/:id - Actualizar vehiculo completo
            else {
                VehiculoElectrico vehiculo = crearVehiculoDesdeJSON(json);
                vehiculo.setId(id);
                vehiculoServicio.actualizarVehiculo(vehiculo);
                
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("mensaje", "Vehiculo actualizado exitosamente");
                enviarJSON(exchange, 200, respuesta);
            }
        } catch (Exception e) {
            enviarError(exchange, 400, e.getMessage());
        }
    }

    private void manejarDELETE(HttpExchange exchange, String[] partes) throws IOException {
        try {
            if (partes.length < 3 || partes[2].isEmpty()) {
                enviarError(exchange, 400, "ID de vehiculo requerido");
                return;
            }

            Long id = Long.parseLong(partes[2]);
            boolean eliminado = vehiculoServicio.eliminarVehiculo(id);
            
            if (eliminado) {
                JsonObject respuesta = new JsonObject();
                respuesta.addProperty("mensaje", "Vehiculo eliminado exitosamente");
                enviarJSON(exchange, 200, respuesta);
            } else {
                enviarError(exchange, 404, "Vehiculo no encontrado");
            }
        } catch (Exception e) {
            enviarError(exchange, 500, e.getMessage());
        }
    }

    private VehiculoElectrico crearVehiculoDesdeJSON(JsonObject json) {
        TipoVehiculo tipo = TipoVehiculo.valueOf(json.get("tipo").getAsString());
        String placa = json.get("placa").getAsString();
        String modelo = json.get("modelo").getAsString();
        int anio = json.get("anio").getAsInt();
        double capacidadBateria = json.get("capacidadBateria").getAsDouble();
        double autonomiaMaxima = json.get("autonomiaMaxima").getAsDouble();

        return switch (tipo) {
            case VAN -> new Van(
                placa, modelo, anio, capacidadBateria, autonomiaMaxima,
                json.get("capacidadCarga").getAsDouble(),
                json.get("numeroAsientos").getAsInt()
            );
            case BICICLETA_ELECTRICA -> new BicicletaElectrica(
                placa, modelo, anio, capacidadBateria, autonomiaMaxima,
                json.get("capacidadCarga").getAsDouble(),
                json.get("tieneCanastaExtra").getAsBoolean()
            );
            case MOTO_ELECTRICA -> new MotoElectrica(
                placa, modelo, anio, capacidadBateria, autonomiaMaxima,
                json.get("capacidadCarga").getAsDouble(),
                json.get("tieneTopCase").getAsBoolean()
            );
        };
    }

    // Metodos auxiliares reutilizables
    private void configurarCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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