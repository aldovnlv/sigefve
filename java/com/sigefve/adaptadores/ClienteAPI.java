package com.sigefve.adaptadores;

// url = https://tapython.xipatlani.tk/
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.concurrent.CompletableFuture;
import java.lang.System;

public class ClienteAPI {

    private String urlBaseSigefve = "https://tapython.xipatlani.tk/";

    /**
     * 
     * @param endpoint
     * @param cuerpoJson
     */
    public void peticionPost(String endpoint, String cuerpoJson){
        // Crear una instancia de HttpClient
        HttpClient cliente = HttpClient.newHttpClient();
        String direccionAPI = urlBaseSigefve+endpoint;
        // Crear una HttpRequest
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(direccionAPI))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(cuerpoJson))
                .build();

        // Enviar la peticion asincronamente

        CompletableFuture<HttpResponse<String>> respuestaFutura = cliente.sendAsync(peticion, HttpResponse.BodyHandlers.ofString());
        System.out.println("Peticion a "+direccionAPI);
        System.out.println("Contenido:\n"+cuerpoJson);
        // Manejar la respuesta
        respuestaFutura.thenApply(HttpResponse::body)
                      .thenAccept(System.out::println)
                      .join(); // Esperar por la respuesta
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        ClienteAPI cliente = new ClienteAPI();

        String cuerpoJson = """
{
    \"id_vehiculo\":9876,
    \"nivel_bateria\":100,
    \"temperatura\":50
}
        """;
        // "telemetria = evento.get('telemetria', {})\r\n" + //
        //                 "        id_vehiculo = telemetria.get('id_vehiculo')\r\n" + //
        //                 "        nivel_bateria = telemetria.get('nivel_bateria', 100)\r\n" + //
        //                 "        temperatura = telemetria.get('temperatura_motor', 0)"
        cliente.peticionPost("telemetria", cuerpoJson);
        
    }
}