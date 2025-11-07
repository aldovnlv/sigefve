from abc import ABC, abstractmethod

class ProcesadorEstadisticas(ABC):
    """
    Clase abstracta para procesamiento genérico de datos de rendimiento.
    """
    @abstractmethod
    def procesar_datos(self, telemetria):
        pass

    @abstractmethod
    def generar_reporte(self):
        pass
