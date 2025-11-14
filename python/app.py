import Config
from Config import Config as cf
from Eventos.GestorEventos import GestorEventos
from Eventos.ObservadorTelemetria import ObservadorTelemetria
from Procesamiento.AnalizadorRendimiento import AnalizadorRendimiento
from flask import Flask, request, jsonify

app = Flask(__name__)

analizador = AnalizadorRendimiento()
gestor_eventos = GestorEventos()
observador = ObservadorTelemetria(gestor_eventos)

class ServicioPython:
    """
    Clase principal del microservicio de Python con Flask.
    """

    def iniciar(self):
        """
        Inicia los hilos y servicios Flask (futuro).
        """
        gestor_eventos.iniciar_hilo()

        @app.route('/health', methods=['GET'])
        def health_check():
            return jsonify({"status": "OK", "servicio": "python-service"}), 200

        @app.route('/telemetria', methods=['POST'])
        def recibir_telemetria():
            """Recibe datos de telemetría desde Java"""
            try:
                telemetria = request.json
                observador.recibir_datos(telemetria)
                return jsonify({"mensaje": "Telemetría procesada"}), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500
            
        def _formatear_alertas(alertas):
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
            """Lista todas las alertas activas ordenadas por prioridad"""
            try:
                conn = Config.obtener_conexion()
                cursor = conn.cursor()
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
            """Lista las alertas activas de un vehículo ordenadas por prioridad"""
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

        @app.route('/estadisticas/<int:id_vehiculo>', methods=['GET'])
        def obtener_estadisticas(id_vehiculo):
            """Obtiene estadísticas de rendimiento de un vehículo específico"""
            try:
                if id_vehiculo:
                    # Analizar vehículo específico desde Java
                    resultado = analizador.analizar_vehiculo(int(id_vehiculo))
                    if resultado:
                        return jsonify(resultado), 200
                    else:
                        return jsonify({"error": "No se encontraron datos para el vehículo"}), 404
                else:
                    # Retornar estadísticas en memoria
                    reporte = analizador.generar_reporte()
                    return jsonify(reporte), 200
            except Exception as e:
                return jsonify({"error": str(e)}), 500

        @app.route('/reporte/csv', methods=['GET'])
        def exportar_reporte_csv():
            """Exporta reporte de rendimiento en CSV"""
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
        print("\n[SISTEMA] Apagado correcto")