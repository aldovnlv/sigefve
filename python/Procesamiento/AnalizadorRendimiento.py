# Procesamiento/AnalizadorRendimiento.py

from datetime import datetime
import Config
from Config import Config as cf
import csv
import io
from .ProcesadorEstadisticas import ProcesadorEstadisticas
import requests


class AnalizadorRendimiento(ProcesadorEstadisticas):
    """
    Clase utilizada para el análisis de rendimiento del vehículo.
    """

    def __init__(self):
        self._datos_vehiculos = {}

    def obtener_vehiculos_java(self):
        """Obtiene todos los vehículos desde el microservicio Java"""
        try:
            url = f"{cf.JAVA_URL}/vehiculos"
            response = requests.get(url, timeout=5)

            if response.status_code == 200:
                return response.json()
            else:
                print(f"[ERROR] Error al obtener vehículos: {response.status_code}")
                return []
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] No se pudo conectar con Java service: {e}")
            return []

    def obtener_telemetria_java(self, id_vehiculo):
        """Obtiene datos de telemetría desde el microservicio Java"""
        try:
            url = f"{cf.JAVA_URL}/telemetria/vehiculo/{id_vehiculo}"
            response = requests.get(url, timeout=5)

            if response.status_code == 200:
                return response.json()
            else:
                print(
                    f"[ERROR] Error al obtener telemetría del vehículo {id_vehiculo}: {response.status_code}"
                )
                return []
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] No se pudo conectar con Java service: {e}")
            return []

    def analizar_vehiculo(self, id_vehiculo):
        """Analiza todo el historial de telemetría de un vehículo desde Java"""
        print(f"[ANALISIS] Obteniendo telemetría del vehículo {id_vehiculo}...")

        # Obtener datos desde Java
        telemetrias = self.obtener_telemetria_java(id_vehiculo)

        if not telemetrias:
            print(f"[ANALISIS] No hay datos para el vehículo {id_vehiculo}")
            return None

        print(f"[ANALISIS] Procesando {len(telemetrias)} registros de telemetría...")

        # Inicializar datos del vehículo si no existen
        if id_vehiculo not in self._datos_vehiculos:
            self._datos_vehiculos[id_vehiculo] = {
                "kilometros": 0,
                "consumo_bateria": [],
                "entregas": 0,
                "ultimo_kilometraje": 0,
            }

        # Procesar todos los registros
        kilometrajes = []
        baterias = []

        for telemetria in telemetrias:
            # Recolectar datos
            km = telemetria.get("kilometrajeActual", 0)
            bat = telemetria.get("nivelBateria", 0)

            kilometrajes.append(km)

            baterias.append(bat)

        # Calcular estadísticas
        if kilometrajes:
            self._datos_vehiculos[id_vehiculo]["kilometros"] = max(kilometrajes)

        if baterias:
            self._datos_vehiculos[id_vehiculo]["consumo_bateria"] = baterias

        return {
            "id_vehiculo": id_vehiculo,
            "registros_procesados": len(telemetrias),
            "kilometros_totales": self._datos_vehiculos[id_vehiculo]["kilometros"],
            "eficiencia_bateria": self.calcular_eficiencia_bateria(id_vehiculo),
            "entregas": self._datos_vehiculos[id_vehiculo]["entregas"],
        }

    def analizar_vehiculos(self):
        """Analiza todo el historial de telemetría de todos los vehículos desde Java"""
        print("[ANALISIS] Obteniendo lista de vehículos...")

        vehiculos = self.obtener_vehiculos_java()

        if not vehiculos:
            print("[ANALISIS] No se encontraron vehículos")
            return []

        print(f"[ANALISIS] Se encontraron {len(vehiculos)} vehículos. Procesando...")

        resultados = []

        for vehiculo in vehiculos:
            id_vehiculo = vehiculo.get("id")

            if id_vehiculo:
                print(f"[ANALISIS] Analizando vehículo ID: {id_vehiculo}")

                resultado = self.analizar_vehiculo(id_vehiculo)

                if resultado:
                    resultados.append(resultado)

        print(
            f"[ANALISIS] Análisis completado. {len(resultados)} vehículos procesados exitosamente."
        )

        return resultados

    def procesar_datos(self, telemetria):
        id_vehiculo = telemetria.get("id_vehiculo")

        if id_vehiculo not in self._datos_vehiculos:
            self._datos_vehiculos[id_vehiculo] = {
                "kilometros": 0,
                "consumo_bateria": [],
                "entregas": 0,
            }

        # Simular cálculo de kilómetros (basado en velocidad)
        velocidad = telemetria.get("velocidad", 0)
        self._datos_vehiculos[id_vehiculo]["kilometros"] += velocidad * (
            15 / 3600
        )  # 15 seg a horas

        # Registrar consumo de batería
        bateria = telemetria.get("nivel_bateria", 100)
        self._datos_vehiculos[id_vehiculo]["consumo_bateria"].append(bateria)

    def calcular_kilometros(self, id_vehiculo):
        return self._datos_vehiculos.get(id_vehiculo, {}).get("kilometros", 0)

    def calcular_eficiencia_bateria(self, id_vehiculo):
        consumos = self._datos_vehiculos.get(id_vehiculo, {}).get("consumo_bateria", [])
        if len(consumos) < 2:
            return 100.0
        # Eficiencia = promedio de batería restante
        return sum(consumos) / len(consumos)

    def calcular_entregas(self, id_vehiculo):
        return self._datos_vehiculos.get(id_vehiculo, {}).get("entregas", 0)

    def generar_reporte(self):
        return self._datos_vehiculos

    def exportar_csv(self, ruta="reporte_rendimiento.csv"):
        try:
            # Generar CSV en memoria
            output = io.StringIO()
            escritor = csv.writer(output)
            escritor.writerow(
                ["Vehiculo ID", "Kilometros", "Eficiencia Bateria (%)", "Entregas"]
            )

            for id_vehiculo, datos in self._datos_vehiculos.items():
                eficiencia = self.calcular_eficiencia_bateria(id_vehiculo)
                escritor.writerow(
                    [id_vehiculo, datos["kilometros"], eficiencia, datos["entregas"]]
                )

            output.seek(0)

            return output.getvalue().encode("utf-8")
        except Exception as e:
            return None
