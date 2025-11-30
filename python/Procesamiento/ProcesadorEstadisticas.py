# Procesamiento/ProcesadorEstadisticas.py

from abc import ABC, abstractmethod


class ProcesadorEstadisticas(ABC):
    """
    Define la interfaz para cualquier clase responsable de calcular y
    analizar estadísticas de rendimiento o datos de telemetría. 
    Es una clase abstracta.
    """
    @abstractmethod
    def procesar_datos(self, id_vehiculo):
        """
        Método abstracto que permite a las subclases a implementar la lógica
        para realizar el análisis de datos, ya sea para un vehículo
        específico o para toda la flota.
        
        Parámetros:
            id_vehiculo: Identificador del vehículo a analizar, o un valor
                         predeterminado si se analiza la flota completa.
        
        Returns:
            list/dict: Los resultados del análisis.
        """
        pass

    @abstractmethod
    def exportar_csv(self):
        """
        Método abstracto que permite a las subclases a implementar la funcionalidad
        para generar un reporte de los resultados del análisis en formato CSV.
        
        Parámetros:
            Ninguno
        
        Returns:
            bytes: El contenido del archivo CSV codificado.
        """
        pass