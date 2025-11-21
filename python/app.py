import Config
from Config import Config as cf
from Eventos.GestorEventos import GestorEventos
from Eventos.ObservadorTelemetria import ObservadorTelemetria
from Procesamiento.AnalizadorRendimiento import AnalizadorRendimiento
from flask import Flask, request, jsonify
from flasgger import Swagger

# Inicialización de componentes globales
app = Flask(__name__)

# Configuración de Swagger para que luzca profesional
app.config['SWAGGER'] = {
    'title': 'API Telemetría SIGEFVE',
    'uiversion': 3,
    'version': '1.0.0',
    'description': 'Microservicio de análisis de rendimiento y gestión de alertas para vehículos eléctricos.'
}
swagger = Swagger(app)

# Instanciación de componentes de lógica de negocio
analizador = AnalizadorRendimiento()
gestor_eventos = GestorEventos()
observador = ObservadorTelemetria(gestor_eventos)

class ServicioPython:
    """
    Orquestador principal del subsistema de Análisis y Alertas del SIGEFVE.
    """

    def iniciar(self):
        """
        Inicializa el contexto de ejecución y define las rutas.
        """
        gestor_eventos.iniciar_hilo()

        @app.route('/health', methods=['GET'])
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

        @app.route('/telemetria', methods=['POST'])
        def recibir_telemetria():
            """
            Punto de entrada para la ingesta de datos de telemetría.
            
            Recibe paquetes de datos (nivel de batería, ubicación GPS, temperatura)
            y los delega al ObservadorTelemetria.

            Parámetros (Payload):
                telemetria (dict): Datos de sensores.
            
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
                    bateria:
                      type: integer
                      example: 85
                    temperatura:
                      type: number
                      example: 65.5
                    latitud:
                      type: number
                      example: 19.4326
                    longitud:
                      type: number
                      example: -99.1332
            responses:
              200:
                description: Telemetría recibida correctamente
              500:
                description: Error de procesamiento
            """
            try:
                telemetria = request.json
                observador.recibir_datos(telemetria)
                return jsonify({"mensaje": "Telemetría procesada"}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        def _formatear_alertas(alertas):
            """Formatea tuplas de BD a diccionarios JSON."""
            resultado = []
            for alerta in alertas:
                resultado.append({
                    'id': alerta[0],
                    'tipo': alerta[1],
                    'descripcion': alerta[2],
                    'prioridad': alerta[3],
                    'fecha_generacion': alerta[4],
                    'estado': alerta[5],
                    'vehiculo_id': alerta[6]
                })
            return resultado

        @app.route('/alertas', methods=['GET'])
        def listar_alertas():
            """
            Recupera todas las alertas activas del sistema.
            ---
            tags:
              - Alertas
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
                          tipo:
                            type: string
                          prioridad:
                            type: integer
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                cursor.execute(
                    'SELECT * FROM alertas WHERE estado = true ORDER BY prioridad ASC, fecha_generacion DESC')
                alertas = cursor.fetchall()
                conn.close()
                return jsonify({'alertas': _formatear_alertas(alertas)}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/<int:id_vehiculo>', methods=['GET'])
        def listar_alertas_por_vehiculo(id_vehiculo):
            """
            Consulta las incidencias activas por vehículo.
            ---
            tags:
              - Alertas
            parameters:
              - name: id_vehiculo
                in: path
                type: integer
                required: true
                description: ID del vehículo a consultar
            responses:
              200:
                description: Alertas filtradas por vehículo
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                cursor.execute(
                    'SELECT * FROM alertas WHERE estado = true AND id_vehiculo = ' + str(id_vehiculo) + ' ORDER BY prioridad ASC, fecha_generacion DESC')
                alertas = cursor.fetchall()
                conn.close()
                return jsonify({'alertas': _formatear_alertas(alertas)}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/desactivar', methods=['PATCH'])
        def desactivar_alertas():
            """
            Realiza el cierre masivo de alertas (Soft Delete).
            ---
            tags:
              - Gestión de Alertas
            responses:
              200:
                description: Todas las alertas han sido desactivadas
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                cursor.execute('UPDATE alertas SET estado = false')
                conn.commit()
                cursor.execute('SELECT * FROM alertas ORDER BY prioridad ASC, fecha_generacion DESC')
                alertas = cursor.fetchall()
                conn.close()
                return jsonify({'alertas': _formatear_alertas(alertas)}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/<int:id_vehiculo>/desactivar', methods=['PATCH'])
        def desactivar_alertas_por_vehiculo(id_vehiculo):
            """
            Cierra las alertas de un vehículo específico.
            ---
            tags:
              - Gestión de Alertas
            parameters:
              - name: id_vehiculo
                in: path
                type: integer
                required: true
            responses:
              200:
                description: Alertas del vehículo desactivadas
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                cursor.execute('UPDATE alertas SET estado = false WHERE id_vehiculo = ' + str(id_vehiculo))
                conn.commit()
                cursor.execute(
                    'SELECT * FROM alertas WHERE id_vehiculo = ' + str(id_vehiculo) + ' ORDER BY prioridad ASC, fecha_generacion DESC')
                alertas = cursor.fetchall()
                conn.close()
                return jsonify({'alertas': _formatear_alertas(alertas)}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/desactivar/id/<int:id>', methods=['PATCH'])
        def desactivar_alertas_por_id(id):
            """
            Cierra una alerta individual por su ID.
            ---
            tags:
              - Gestión de Alertas
            parameters:
              - name: id
                in: path
                type: integer
                required: true
            responses:
              200:
                description: Alerta desactivada
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                cursor.execute('UPDATE alertas SET estado = false WHERE id = ' + str(id))
                conn.commit()
                cursor.execute(
                    'SELECT * FROM alertas WHERE id = ' + str(id) + ' ORDER BY prioridad ASC, fecha_generacion DESC')
                alertas = cursor.fetchall()
                conn.close()
                return jsonify({'alertas': _formatear_alertas(alertas)}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/estadisticas', methods=['GET'])
        def obtener_estadisticas():
            """
            Genera estadísticas globales de la flota.
            ---
            tags:
              - Reportes
            responses:
              200:
                description: Estadísticas agregadas
              404:
                description: No hay datos suficientes
            """
            try:
                resultado = analizador.analizar_vehiculos()
                if resultado:
                    return jsonify(resultado), 200
                else:
                    return jsonify({"error": "No se encontraron datos de vehículos"}), 404
            except Exception as e:
                return jsonify({"error": str(e)}), 500

        @app.route('/estadisticas/<int:id_vehiculo>', methods=['GET'])
        def obtener_estadisticas_por_vechiulo(id_vehiculo):
            """
            Obtiene estadísticas de un vehículo.
            ---
            tags:
              - Reportes
            parameters:
              - name: id_vehiculo
                in: path
                type: integer
                required: true
            responses:
              200:
                description: Estadísticas del vehículo
            """
            try:
                if id_vehiculo:
                    resultado = analizador.analizar_vehiculo(int(id_vehiculo))
                    if resultado:
                        return jsonify(resultado), 200
                    else:
                        return jsonify({"error": "No se encontraron datos para el vehículo"}), 404
                else:
                    reporte = analizador.generar_reporte()
                    return jsonify(reporte), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500

        @app.route('/reporte/csv', methods=['GET'])
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
            """
            try:
                ruta = analizador.exportar_csv()
                return jsonify({"mensaje": "Reporte generado", "archivo": ruta}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    try:
        servicio = ServicioPython()
        servicio.iniciar()

        app.run(host=cf.FLASK_HOST, port=cf.FLASK_PORT)
        
    except KeyboardInterrupt:
        gestor_eventos.detener_hilo()
        print("\n[SIGEFVE] Apagado correcto del servicio Python (Graceful Shutdown)")