import Config
from Config import Config as cf
from Eventos.GestorEventos import GestorEventos
from Eventos.ObservadorTelemetria import ObservadorTelemetria
from Procesamiento.AnalizadorRendimiento import AnalizadorRendimiento
from flask import Flask, request, jsonify, send_file
from flasgger import Swagger
import io


# Inicialización de componentes globales
app = Flask(__name__)

app.config["SWAGGER"] = {
    "title": "API Telemetría SIGEFVE",
    "uiversion": 3,
    "version": "1.0.0",
    "description": "Microservicio de análisis de rendimiento y gestión de alertas para vehículos eléctricos.",
}
swagger = Swagger(app)

# Instanciación de componentes de lógica de negocio
analizador = AnalizadorRendimiento()
gestor_eventos = GestorEventos()
observador = ObservadorTelemetria()
observador.suscribir(gestor_eventos)


class ServicioPython:
    """
    Orquestador principal del subsistema de Análisis y Alertas del SIGEFVE.
    """

    def iniciar(self):
        """
        Inicializa el contexto de ejecución y define las rutas.
        """
        gestor_eventos.iniciar_hilo()

        @app.route("/health", methods=["GET"])
        def health_check():
            """
            Verifica la disponibilidad del microservicio.
            ---
            tags:
              - Monitoreo
            responses:
              200:
                description: Servicio operativo
                schema:
                  type: object
                  properties:
                    status:
                      type: string
                      example: "OK"
                    servicio:
                      type: string
                      example: "python-service"
            """
            return jsonify({"status": "OK", "servicio": "python-service"}), 200

        @app.route("/telemetria", methods=["POST"])
        def recibir_telemetria():
            """
            Punto de entrada para la ingesta de datos de telemetría.

            Recibe paquetes de datos (nivel de batería, ubicación GPS, temperatura)
            y los delega al ObservadorTelemetria.
            ---
            tags:
              - Telemetría
            parameters:
              - in: body
                name: body
                required: true
                schema:
                  type: object
                  required:
                    - id_vehiculo
                    - bateria
                    - temperatura
                  properties:
                    id_vehiculo:
                      type: integer
                      example: 1
                    nivel_bateria:
                      type: number
                      example: 85.00
                    temperatura_motor:
                      type: number
                      example: 65.50
                    kilometros_totales:
                      type: number
                      example: 19.43
            responses:
              200:
                description: Telemetría procesada
                schema:
                  type: object
                  properties:
                    mensaje:
                      type: string
              500:
                description: Error en el servidor
                schema:
                  type: object
                  properties:
                    error:
                      type: string
            """
            try:
                telemetria = request.json
                observador.recibir_datos(telemetria)
                return jsonify({"mensaje": "Telemetría procesada"}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500

        def _formatear_alertas(alertas):
          """
          Formatea las alertas de la base de datos a objetos del modelo correspondiente.
          
          Parámetros:
            alertas: Lista de tuplas con datos de alertas
          
          Returns:
            Lista de objetos Alerta (AlertaUrgente, AlertaMantenimiento o Alerta)
          """
          from Alertas.AlertaUrgente import AlertaUrgente
          from Alertas.AlertaMantenimiento import AlertaMantenimiento
          from Alertas.Alerta import Alerta
    
          resultado = []
          for alerta in alertas:
            # alerta = (id, descripcion, prioridad, fecha_generacion, estado, id_vehiculo, mensaje, mantenimiento)
            id_alerta = alerta[0]
            descripcion = alerta[1]
            prioridad = alerta[2]
            fecha_generacion = alerta[3]
            estado = alerta[4]
            id_vehiculo = alerta[5]
        
            # Determinar tipo de alerta y crear instancia correspondiente
            if len(alerta) > 6 and alerta[6] is not None:
                # AlertaUrgente
                mensaje = alerta[6]
                alerta_obj = AlertaUrgente(
                    id_vehiculo=id_vehiculo,
                    mensaje=mensaje,
                    prioridad=prioridad,
                    descripcion=descripcion
                )
                alerta_obj.id = id_alerta
                alerta_obj.fecha_generacion = fecha_generacion
            
            elif len(alerta) > 7 and alerta[7] is not None:
                # AlertaMantenimiento
                alerta_obj = AlertaMantenimiento(
                    id_vehiculo=id_vehiculo,
                    kilometraje_actual=0,  # Ajustar según necesites
                    descripcion=descripcion,
                    prioridad=prioridad
                )
                alerta_obj.id = id_alerta
                alerta_obj.fecha_generacion = fecha_generacion
                alerta_obj._fecha_mantenimiento = alerta[7]
            
            else:
                # Alerta general
                alerta_obj = Alerta(
                    descripcion=descripcion,
                    id_vehiculo=id_vehiculo,
                    prioridad=prioridad,
                    id=id_alerta,
                    estado=estado,
                    fecha_generacion=fecha_generacion
                )
            
            resultado.append(alerta_obj)
    
          return resultado

        @app.route("/alertas", methods=["GET"])
        def listar_alertas():
            """
            Recupera todas las alertas activas del sistema.
            ---
            tags:
              - Alertas
            parameters:
              - name: prioridad
                in: query
                type: integer
                required: false
                description: Filtrar por prioridad específica
              - name: id_vehiculo
                in: query
                type: integer
                required: false
                description: Filtrar por vehículo específico
            responses:
              200:
                description: Lista de alertas activas
                schema:
                  type: object
                  properties:
                    alertas:
                      type: array
                      items:
                        type: object
                        properties:
                          id:
                            type: integer
                          descripcion:
                            type: string
                          prioridad:
                            type: integer
                          fecha_generacion:
                            type: string
                          id_vehiculo:
                            type: integer
                          tipo:
                            type: string
                          mantenimiento:
                            type: string
                          mensaje:
                            type: string
              500:
                description: Error en el servidor
                schema:
                  type: object
                  properties:
                    error:
                      type: string
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()

                prioridad_filtro = request.args.get("prioridad", type=int)
                vehiculo_filtro = request.args.get("id_vehiculo", type=int)

                query = """
                        SELECT 
                                a.id,
                                a.descripcion,
                                a.prioridad,
                                a.fecha_generacion,
                                a.estado,
                                a.id_vehiculo,
                                au.mensaje,
                                am.mantenimiento
                        FROM alerta a
                        LEFT JOIN alerta_urgente au ON a.id = au.id_alerta
                        LEFT JOIN alerta_mantenimiento am ON a.id = am.id_alerta
                        WHERE a.estado = 1
                        """
                parametros = []

                if prioridad_filtro is not None:
                    query += " AND a.prioridad = ?"
                    parametros.append(prioridad_filtro)

                if vehiculo_filtro is not None:
                    query += " AND a.id_vehiculo = ?"
                    parametros.append(vehiculo_filtro)

                query += " ORDER BY a.prioridad ASC, a.fecha_generacion DESC"

                cursor.execute(query, parametros)
                alertas = cursor.fetchall()
                conn.close()

                if len(alertas) > 0:
                  alertas_json = [alerta.to_dict() for alerta in _formatear_alertas(alertas)]

                  return jsonify({"alertas": alertas_json}), 200
                
                return jsonify({"alertas": []}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500

        @app.route("/alertas/desactivar", methods=["PATCH"])
        def desactivar_alertas():
            """
            Desactiva alertas del sistema de forma flexible.
            Puede desactivar:
            - Todas las alertas (sin parámetros)
            - Alertas de un vehículo específico (?id_vehiculo=X)
            - Una alerta específica (?id=X)
            ---
            tags:
              - Gestión de Alertas
            parameters:
              - name: id
                in: query
                type: integer
                required: false
                description: ID de la alerta específica a desactivar
              - name: id_vehiculo
                in: query
                type: integer
                required: false
                description: ID del vehículo cuyas alertas se desactivarán
            responses:
              200:
                description: Alertas desactivadas correctamente
                schema:
                  type: object
                  properties:
                    mensaje:
                      type: string
                    alertas_desactivadas:
                      type: integer
              404:
                description: No se encontraron alertas activas para desactivar
                schema:
                  type: object
                  properties:
                    mensaje:
                      type: string
                    alertas_desactivadas:
                      type: integer
              500:
                description: Error en el servidor
                schema:
                  type: object
                  properties:
                    error:
                      type: string
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()

                id_alerta = request.args.get("id", type=int)
                id_vehiculo = request.args.get("id_vehiculo", type=int)

                if id_alerta is not None:
                    cursor.execute(
                        "UPDATE alerta SET estado = 0 WHERE id = ? AND estado = 1", (id_alerta,)
                    )
                    mensaje = f"Alerta {id_alerta} desactivada"

                elif id_vehiculo is not None:
                    cursor.execute(
                        "UPDATE alerta SET estado = 0 WHERE id_vehiculo = ? AND estado = 1",
                        (id_vehiculo,),
                    )
                    mensaje = f"Alertas del vehículo {id_vehiculo} desactivadas"

                else:
                    cursor.execute("UPDATE alerta SET estado = 0 WHERE estado = 1")
                    mensaje = "Todas las alertas desactivadas"

                alertas_desactivadas = cursor.rowcount
                conn.commit()

                if alertas_desactivadas == 0:
                    conn.close()
                    return (
                        jsonify(
                            {
                                "mensaje": "No se encontraron alertas activas para desactivar",
                                "alertas_desactivadas": 0,
                            }
                        ),
                        404,
                    )

                conn.close()

                return (
                    jsonify(
                        {
                            "mensaje": mensaje,
                            "alertas_desactivadas": alertas_desactivadas
                        }
                    ),
                    200,
                )

            except Exception as e:
                return jsonify({"error": str(e)}), 500

        @app.route("/estadisticas", methods=["GET"])
        def obtener_estadisticas():
            """
            Genera estadísticas de la flota.
            Si se proporciona 'id_vehiculo', devuelve estadísticas específicas.
            Si no, devuelve las estadísticas globales agregadas.
            ---
            tags:
              - Reportes
            parameters:
              - name: id_vehiculo
                in: query
                type: integer
                required: false
                description: ID del vehículo para estadísticas específicas
            responses:
              200:
                description: |
                  Estadísticas exitosas. Puede devolver:
                  - Sin id_vehiculo: estadísticas globales de todos los vehículos
                  - Con id_vehiculo: estadísticas de un vehículo específico
                examples:
                  application/json:
                    estadisticas globales:
                      estadisticas:
                        - id_vehiculo: 1
                          registros_procesados: 150
                          kilometros_totales: 1250.5
                          eficiencia_bateria: 95.3
                          entregas_completadas: 45
                        - id_vehiculo: 2
                          registros_procesados: 200
                          kilometros_totales: 1800.2
                          eficiencia_bateria: 92.1
                          entregas_completadas: 60
                    vehículo específico:
                      id_vehiculo: 1
                      registros_procesados: 150
                      kilometros_totales: 1250.5
                      eficiencia_bateria: 95.3
                      entregas_completadas: 45
                schema:
                  type: object
              404:
                description: No se encontraron datos
                schema:
                  type: object
                  properties:
                    error:
                      type: string
              500:
                description: Error en el servidor
                schema:
                  type: object
                  properties:
                    error:
                      type: string
            """
            try:
                id_vehiculo = request.args.get("id_vehiculo", type=int)

                if id_vehiculo:
                    # Lógica para vehículo específico
                    resultado = analizador.procesar_datos(id_vehiculo=id_vehiculo)
                    if resultado:
                        return jsonify(resultado), 200
                    else:
                        return (
                            jsonify(
                                {"error": f"No se encontraron datos para el vehículo {id_vehiculo}"}
                            ),
                            404,
                        )
                else:
                    # Lógica global
                    resultado = analizador.procesar_datos()
                    if resultado:
                        return jsonify({"estadisticas": resultado}), 200
                    else:
                        return (
                            jsonify({"error": "No se encontraron datos de vehículos"}),
                            404,
                        )

            except Exception as e:
                return jsonify({"error": str(e)}), 500

        @app.route("/reporte/csv", methods=["GET"])
        def exportar_reporte_csv():
            """
            Exporta el reporte histórico a CSV.
            ---
            tags:
              - Reportes
            responses:
              200:
                description: Ruta del archivo generado
                schema:
                  type: object
                  properties:
                    mensaje:
                      type: string
                    archivo:
                      type: string
              500:
                description: Error en el servidor
                schema:
                  type: object
                  properties:
                    error:
                      type: string
            """
            try:
                output = analizador.exportar_csv()

                return send_file(
                    io.BytesIO(output),
                    mimetype="text/csv",
                    as_attachment=True,
                    download_name="reporte_rendimiento.csv",
                )
            except Exception as e:
                return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    try:
        servicio = ServicioPython()
        servicio.iniciar()
        # Asegúrate de que el puerto sea entero, por si viene como string en Config
        app.run(host=cf.FLASK_HOST, port=int(cf.FLASK_PORT))

    except KeyboardInterrupt:
        observador.desuscribir(gestor_eventos)
        gestor_eventos.detener_hilo()
        print("\n[SIGEFVE] Apagado correcto del servicio Python (Graceful Shutdown)")