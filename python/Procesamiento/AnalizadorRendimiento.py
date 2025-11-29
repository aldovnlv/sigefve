# Procesamiento/AnalizadorRendimiento.py

from Config import Config as cf
import csv
import io
from .ProcesadorEstadisticas import ProcesadorEstadisticas
import requests

class AnalizadorRendimiento(ProcesadorEstadisticas):
    """
    Clase utilizada para el análisis de rendimiento del vehículo.
    """

    def _obtener_vehiculos_java(self):
        """Obtiene todos los vehículos desde el microservicio Java"""
        try:
            url = f"{cf.JAVA_URL}/vehiculos"
            respuesta = requests.get(url, timeout=5)

            if respuesta.status_code == 200:
                return respuesta.json()
            else:
                print(f"[ERROR] Error al obtener vehículos: {respuesta.status_code}")
                return []
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] No se pudo conectar con Java service: {e}")
            return []

    def _obtener_telemetria_java(self, id_vehiculo):
        """Obtiene datos de telemetría desde el microservicio Java"""
        try:
            url = f"{cf.JAVA_URL}/telemetria/vehiculo/{id_vehiculo}"
            respuesta = requests.get(url, timeout=5)

            if respuesta.status_code == 200:
                return respuesta.json()
            else:
                print(
                    f"[ERROR] Error al obtener telemetría del vehículo {id_vehiculo}: {respuesta.status_code}"
                )
                return []
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] No se pudo conectar con Java service: {e}")
            return []
        
    def _calcular_estadisticas_telemetria(self, telemetrias):
        """
        Calcula estadísticas a partir de un conjunto de datos de telemetría.
        Retorna un diccionario con las métricas calculadas.
        """
        if not telemetrias:
            return {
                "kilometros_totales": 0,
                "eficiencia_bateria": 100.0,
                "entregas_completadas": 0,
                "registros_procesados": 0
            }

        return {
            "kilometros_totales": self._calcular_kilometros(telemetrias),
            "eficiencia_bateria": self._calcular_eficiencia_bateria(telemetrias),
            "entregas_completadas": self._calcular_entregas(telemetrias),
            "registros_procesados": len(telemetrias)
        }

    def _analizar_vehiculo(self, id_vehiculo):
        """Analiza todo el historial de telemetría de un vehículo desde Java"""
        print(f"[ANALISIS] Obteniendo telemetría del vehículo {id_vehiculo}...")

        # Obtener datos desde Java
        telemetrias = self._obtener_telemetria_java(id_vehiculo)

        if not telemetrias:
            print(f"[ANALISIS] No hay datos para el vehículo {id_vehiculo}")
            return None

        print(f"[ANALISIS] Procesando {len(telemetrias)} registros de telemetría...")

        estadisticas = self._calcular_estadisticas_telemetria(telemetrias)
        estadisticas["id_vehiculo"] = id_vehiculo

        return estadisticas

    def _analizar_vehiculos(self):
        """Analiza todo el historial de telemetría de todos los vehículos desde Java"""
        print("[ANALISIS] Obteniendo lista de vehículos...")

        vehiculos = self._obtener_vehiculos_java()

        if not vehiculos:
            print("[ANALISIS] No se encontraron vehículos")
            return []

        print(f"[ANALISIS] Se encontraron {len(vehiculos)} vehículos. Procesando...")

        resultados = []

        for vehiculo in vehiculos:
            id_vehiculo = vehiculo.get("id")

            if id_vehiculo:
                print(f"[ANALISIS] Analizando vehículo ID: {id_vehiculo}")

                resultado = self._analizar_vehiculo(id_vehiculo)

                if resultado:
                    resultados.append(resultado)

        print(
            f"[ANALISIS] Análisis completado. {len(resultados)} vehículos procesados exitosamente."
        )

        return resultados

    def _calcular_kilometros(self, telemetrias):
        kilometros = [telemetria.get("kilometrajeActual", 0) for telemetria in telemetrias]

        return max(kilometros) if kilometros else 0

    def _calcular_eficiencia_bateria(self, telemetrias):
        baterias = [telemetria.get("nivelBateria", 0) for telemetria in telemetrias]

        return round(sum(baterias) / len(baterias), 2)

    def _calcular_entregas(self, telemetrias):
        entregas = [telemetria.get("entregasCompletadas", 0) for telemetria in telemetrias]

        return max(entregas) if entregas else 0

    def procesar_datos(self, id_vehiculo = -1):
        if id_vehiculo != -1 :
            return self._analizar_vehiculo(id_vehiculo)
        
        return self._analizar_vehiculos()

    def exportar_csv(self):
        try:
            # Generar CSV en memoria
            output = io.StringIO()
            escritor = csv.writer(output)
            escritor.writerow(
                ["Vehiculo ID", "Kilometros", "Eficiencia Bateria (%)", "Entregas"]
            )

            estadisticas = self._analizar_vehiculos()

            for datos in estadisticas:
                escritor.writerow(
                    [datos["id_vehiculo"], datos["kilometros_totales"], datos["eficiencia_bateria"], datos["entregas_completadas"]]
                )

            output.seek(0)

            return output.getvalue().encode("utf-8")
        except Exception as e:
            return None
