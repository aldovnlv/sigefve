# Procesamiento/ProcesadorEstadisticas.py

from abc import ABC, abstractmethod

class ProcesadorEstadisticas(ABC):
    """
    Clase abstracta para procesamiento genérico de datos de rendimiento.
    """
    @abstractmethod
    def procesar_datos(self, id_vehiculo):
        pass

    @abstractmethod
    def exportar_csv(self):
        pass
