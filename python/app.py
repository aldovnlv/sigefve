import Config
from Config import Config as cf
from Eventos.GestorEventos import GestorEventos
from Eventos.ObservadorTelemetria import ObservadorTelemetria
from Procesamiento.AnalizadorRendimiento import AnalizadorRendimiento
from flask import Flask, request, jsonify
from flasgger import Swagger

# Inicialización de componentes globales y configuración del framework Flask
app = Flask(__name__)
swagger = Swagger(app)

# Instanciación de componentes de la lógica de negocio del SIGEFVE
# Analizador: Procesa métricas de eficiencia y kilometraje.
# GestorEventos: Maneja el procesamiento asíncrono de la telemetría entrante.
analizador = AnalizadorRendimiento()
gestor_eventos = GestorEventos()
observador = ObservadorTelemetria(gestor_eventos)

class ServicioPython:
    """
    Orquestador principal del subsistema de Análisis y Alertas del SIGEFVE.
    
    Este microservicio implementa la lógica de negocio requerida para el procesamiento
    de datos de la flota de vehículos eléctricos (Vans, Bicicletas y Motos). 
    Sus responsabilidades incluyen:
    1. Ingesta de telemetría enviada por el módulo Java.
    2. Detección de anomalías (Batería < 20%, Temperatura > 70°C).
    3. Cálculo de estadísticas de rendimiento (Km recorridos, eficiencia).
    """

    def iniciar(self):
        """
        Inicializa el contexto de ejecución del servicio Flask y los hilos de procesamiento.

        Levanta el hilo demonio del `GestorEventos` para permitir que el análisis de telemetría
        no bloquee las peticiones HTTP, cumpliendo con el requisito de arquitectura de 
        microservicios concurrentes.
        """
        gestor_eventos.iniciar_hilo()

        @app.route('/health', methods=['GET'])
        def health_check():
            """
            Verifica la disponibilidad del microservicio para el API Gateway (Go).
            
            Utilizado por el Gateway para enrutamiento y monitoreo de salud del sistema.

            Retorna:
                tuple: Respuesta JSON con estado 'OK' y código HTTP 200.
            """
            return jsonify({"status": "OK", "servicio": "python-service"}), 200

        @app.route('/telemetria', methods=['POST'])
        def recibir_telemetria():
            """
            Punto de entrada para la ingesta de datos de telemetría desde el módulo Java.
            
            Recibe paquetes de datos (nivel de batería, ubicación GPS, temperatura, velocidad)
            y los delega al `ObservadorTelemetria` para su análisis en busca de alertas
            automáticas.

            Parámetros (Payload JSON):
                telemetria (dict): Objeto con datos de sensores e ID del vehículo.

            Retorna:
                tuple: JSON confirmando la recepción para procesamiento asíncrono o error 500.
            """
            try:
                telemetria = request.json
                observador.recibir_datos(telemetria)
                return jsonify({"mensaje": "Telemetría procesada"}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        def _formatear_alertas(alertas):
            """
            Mapea las tuplas de la base de datos SQLite a objetos JSON estándar para el Frontend.

            Parámetros:
                alertas (list): Lista de tuplas obtenidas de la tabla 'alertas'.

            Retorna:
                list[dict]: Lista de diccionarios con claves explícitas (id, tipo, prioridad, etc).
            """
            resultado = []
            for alerta in alertas:
                resultado.append({
                    'id': alerta[0],
                    'tipo': alerta[1],   # Ej: 'BATERIA_BAJA', 'MANTENIMIENTO'
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
            Recupera todas las alertas activas del sistema SIGEFVE.
            
            El listado de alertas es ordenado por prioridad
            (Críticas primero) y temporalidad.

            Retorna:
                tuple: JSON con la lista de alertas activas y código 200.
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                # Ordenamiento por prioridad ASC (1=Alta) y fecha DESC
                cursor.execute(
                    'SELECT * FROM alertas WHERE estado = true ORDER BY prioridad ASC, fecha_generacion DESC')
                alertas = cursor.fetchall()
                conn.close()

                return jsonify({
                    'alertas': _formatear_alertas(alertas)
                }), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/<int:id_vehiculo>', methods=['GET'])
        def listar_alertas_por_vehiculo(id_vehiculo):
            """
            Consulta las incidencias activas filtradas por un vehículo específico.
            Útil para la vista de detalle de vehículo en el Dashboard.

            Parámetros:
                id_vehiculo (int): Identificador único del vehículo en la flota.

            Retorna:
                tuple: JSON con alertas filtradas o error 406 si falta el ID.
            """
            try:
                if id_vehiculo:
                    conn = Config.obtener_conexion()
                    cursor = conn.cursor()
                    cursor.execute(
                    'SELECT * FROM alertas WHERE estado = true AND id_vehiculo = ' + str(id_vehiculo) + ' ORDER BY prioridad ASC, fecha_generacion DESC')
                    alertas = cursor.fetchall()
                    conn.close()

                    return jsonify({
                        'alertas': _formatear_alertas(alertas)
                    }), 200
                else:
                    return jsonify({'error': 'No hay un "id_vehiculo" especificado.'}), 406
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/desactivar', methods=['PATCH'])
        def desactivar_alertas():
            """
            Realiza el cierre masivo de alertas.
            
            Cambia el estado de todas las alertas a 'false', indicando que han sido
            atendidas o reconocidas por el operador del sistema.

            Retorna:
                tuple: JSON con el estado actualizado de la tabla de alertas.
            """
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
                
                cursor.execute('UPDATE alertas SET estado = false')
                conn.commit()

                cursor.execute(
                'SELECT * FROM alertas ORDER BY prioridad ASC, fecha_generacion DESC'
                )
                alertas = cursor.fetchall()
                conn.close()

                return jsonify({
                    'alertas': _formatear_alertas(alertas)
                }), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/<int:id_vehiculo>/desactivar', methods=['PATCH'])
        def desactivar_alertas_por_vehiculo(id_vehiculo):
            """
            Cierra las alertas asociadas a un vehículo específico.
            
            Se utiliza cuando un vehículo sale de mantenimiento o completa su carga.

            Parámetros:
                id_vehiculo (int): ID del vehículo a desactivar.

            Retorna:
                tuple: JSON confirmando la actualización.
            """
            try:
                if id_vehiculo:
                    conn = Config.obtener_conexion()
                    cursor = conn.cursor()
                
                    cursor.execute('UPDATE alertas SET estado = false WHERE id_vehiculo = ' + str(id_vehiculo))
                    conn.commit()

                    cursor.execute(
                    'SELECT * FROM alertas WHERE id_vehiculo = ' + str(id_vehiculo) + ' ORDER BY prioridad ASC, fecha_generacion DESC'
                    )
                    alertas = cursor.fetchall()
                    conn.close()

                    return jsonify({
                        'alertas': _formatear_alertas(alertas)
                    }), 200
                else:
                    return jsonify({'error': 'No hay un "id_vehiculo" especificado.'}), 406
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/alertas/desactivar/id/<int:id>', methods=['PATCH'])
        def desactivar_alertas_por_id(id):
            """
            Cierra una alerta individual por su ID único.

            Parámetros:
                id (int): Clave primaria de la alerta.

            Retorna:
                tuple: JSON con el estado resultante de la alerta.
            """
            try:
                if id:
                    conn = Config.obtener_conexion()
                    cursor = conn.cursor()
                
                    cursor.execute('UPDATE alertas SET estado = false WHERE id = ' + str(id))
                    conn.commit()

                    cursor.execute(
                    'SELECT * FROM alertas WHERE id = ' + str(id) + ' ORDER BY prioridad ASC, fecha_generacion DESC'
                    )
                    alertas = cursor.fetchall()
                    conn.close()

                    return jsonify({
                        'alertas': _formatear_alertas(alertas)
                    }), 200
                else:
                    return jsonify({'error': 'No hay un "id" especificado.'}), 406
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        @app.route('/estadisticas', methods=['GET'])
        def obtener_estadisticas():
            """
            Genera las estadísticas globales de rendimiento de la flota SIGEFVE.
            
            Invoca al `AnalizadorRendimiento` para calcular métricas clave:
            - Kilómetros recorridos promedio.
            - Eficiencia de batería por tipo de vehículo.
            - Total de entregas completadas.

            Retorna:
                tuple: JSON con objeto de estadísticas agregadas o error 404.
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
            Obtiene métricas de rendimiento para un solo vehículo.

            Parámetros:
                id_vehiculo (int): ID del vehículo a analizar.

            Retorna:
                tuple: JSON con métricas específicas o reporte general si falla el ID.
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
            Genera un archivo CSV con el reporte histórico de rendimiento.
            
            WIP

            Retorna:
                tuple: JSON conteniendo la ruta del archivo generado.
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

        # Inicio del servidor Flask en el puerto configurado
        app.run(host=cf.FLASK_HOST, port=cf.FLASK_PORT)
    except KeyboardInterrupt:
        gestor_eventos.detener_hilo()
        print("\n[SIGEFVE] Apagado correcto del servicio Python (Graceful Shutdown)")