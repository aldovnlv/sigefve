package com.sigefve.adaptadores;

// import com.google.gson.Gson;
// import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer; //
import com.google.gson.JsonDeserializer; //
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
// import com.sun.net.httpserver.HttpExchange;
// import com.sun.net.httpserver.HttpHandler;

// import java.io.IOException;
// import java.io.OutputStream;
// import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; //
// import java.util.List;
// import java.util.Map;
// import java.time.LocalDate;

import java.lang.reflect.Type;

/**
 * Adaptador de Gson para serializar y deserializar objetos LocalDateTime.
 * Utiliza un formato de fecha personalizado: "d::MMM::uuuu HH::mm::ss".
 */
public class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d::MMM::uuuu HH::mm::ss");

  /**
   * Serializa un objeto LocalDateTime a su representación JSON.
   * 
   * @param localDateTime El objeto fecha-hora a serializar.
   * @param srcType       El tipo del objeto fuente.
   * @param context       Contexto de serialización.
   * @return Un JsonElement que representa la fecha y hora.
   */
  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type srcType,
      JsonSerializationContext context) {
    System.out.println("serialize");
    return new JsonPrimitive(formatter.format(localDateTime));
  }
  
  /**
   * Deserializa un elemento JSON a un objeto LocalDateTime.
   * 
   * @param json    El elemento JSON a deserializar.
   * @param typeOfT El tipo del objeto destino.
   * @param context Contexto de deserialización.
   * @return El objeto LocalDateTime resultante.
   * @throws JsonParseException Si ocurre un error durante el parseo.
   */
  @Override
  public LocalDateTime deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    System.out.println("dessss");

    return LocalDateTime.parse(json.getAsString(), formatter);
  }
}