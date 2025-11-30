# Procesamiento/AnalizadorRendimiento.py

from Config import Config as cf
import csv
import io
from .ProcesadorEstadisticas import ProcesadorEstadisticas
import requests


class AnalizadorRendimiento(ProcesadorEstadisticas):
    """
    Clase principal para el análisis de rendimiento. Hereda de
    ProcesadorEstadisticas y se encarga de:
    1. Obtener datos históricos de telemetría de los vehículos desde el microservicio Java.
    2. Calcular métricas clave (ej. Kilómetros, Eficiencia de Batería, Entregas).
    3. Implementar métodos para exportar los resultados en un formato específico.
    """

    def _obtener_vehiculos_java(self):
        """
        Realiza una petición GET al microservicio de Java para obtener la lista
        completa de vehículos registrados en el sistema.
        
        Parámetros:
            Ninguno
        
        Returns:
            list: Lista de diccionarios, donde cada diccionario representa un vehículo, o lista vacía en caso de error.
        """
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
        """
        Realiza una petición GET al microservicio de Java para obtener todo el historial
        de telemetría registrado para un vehículo específico.
        
        Parámetros:
            id_vehiculo: Identificador único del vehículo del cual se desea la telemetría.
        
        Returns:
            list: Lista de registros de telemetría para el vehículo, o lista vacía en caso de error.
        """
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
        Coordina el cálculo de todas las métricas de rendimiento (eficiencia, kilometraje, etc.)
        a partir de un conjunto de datos de telemetría.
        
        Parámetros:
            telemetrias: Lista de diccionarios de datos históricos de telemetría.
        
        Returns:
            dict: Diccionario que contiene las estadísticas resumidas.
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
        """
        Proceso completo para analizar el rendimiento de un vehículo específico:
        obtiene los datos, calcula las estadísticas y retorna el resultado.
        
        Parámetros:
            id_vehiculo: Identificador del vehículo a analizar.
        
        Returns:
            dict: Diccionario con las estadísticas del vehículo o None si no hay datos.
        """
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
        """
        Ejecuta el proceso de análisis de rendimiento sobre toda la flota de vehículos,
        iterando sobre cada vehículo obtenido desde el microservicio Java.
        
        Parámetros:
            Ninguno
        
        Returns:
            list: Lista de diccionarios, cada uno conteniendo las estadísticas de un vehículo.
        """
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
        """
        Calcula el kilometraje total recorrido considerando que el kilometraje actual
        es el valor máximo registrado en la telemetría.
        
        Parámetros:
            telemetrias: Lista de registros de telemetría.
        
        Returns:
            float: Kilometraje máximo registrado.
        """
        kilometros = [telemetria.get("kilometrajeActual", 0.0) for telemetria in telemetrias]

        return max(kilometros) if kilometros else 0.0

    def _calcular_eficiencia_bateria(self, telemetrias):
        """
        Calcula la eficiencia promedio de la batería, tomando el promedio de todos
        los niveles de batería registrados.
        
        Parámetros:
            telemetrias: Lista de registros de telemetría.
        
        Returns:
            float: Nivel promedio de batería redondeado a 2 decimales.
        """
        baterias = [telemetria.get("nivelBateria", 0) for telemetria in telemetrias]

        # Evita división por cero si la lista está vacía
        return round(sum(baterias) / len(baterias), 2) if baterias else 0.0

    def _calcular_entregas(self, telemetrias):
        """
        Calcula el número total de entregas completadas, tomando el valor máximo
        registrado en la telemetría.
        
        Parámetros:
            telemetrias: Lista de registros de telemetría.
        
        Returns:
            int: Número máximo de entregas completadas.
        """
        entregas = [telemetria.get("entregasCompletadas", 0) for telemetria in telemetrias]

        return max(entregas) if entregas else 0

    def procesar_datos(self, id_vehiculo = -1):
        """
        Método público principal que inicia el análisis, ya sea para un solo vehículo
        o para toda la flota.
        
        Parámetros:
            id_vehiculo: ID del vehículo a procesar. Si es -1 (por defecto), procesa toda la flota.
        
        Returns:
            list/dict: Lista de estadísticas de la flota o estadísticas de un vehículo individual.
        """
        if id_vehiculo != -1 :
            return self._analizar_vehiculo(id_vehiculo)
        
        return self._analizar_vehiculos()

    def exportar_csv(self):
        """
        Ejecuta el análisis de toda la flota y genera un reporte CSV de los resultados
        en memoria (buffer StringIO).
        
        Parámetros:
            Ninguno
        
        Returns:
            bytes: Contenido del archivo CSV codificado en UTF-8, listo para ser enviado como respuesta HTTP.
        """
        try:
            # Generar CSV en memoria
            output = io.StringIO()
            # Utiliza el módulo csv para escribir el reporte
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
            # En caso de cualquier error
            print(f"[ERROR EXPORT] Falló la exportación CSV: {e}")
            return None