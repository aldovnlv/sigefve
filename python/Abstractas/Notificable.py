from abc import ABC, abstractmethod

class Notificable(ABC):
    """
    Clase abstracta para objetos que pueden enviar notificaciones.
    """
    @abstractmethod
    def enviar_notificacion(self):
        pass