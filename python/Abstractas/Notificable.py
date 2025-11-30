# Abstractas/Notificable.py

from abc import ABC, abstractmethod


class Notificable(ABC):
    """
    Define la interfaz para cualquier objeto del sistema que deba tener la
    capacidad de enviar notificaciones al usuario, a otros servicios o a consolas.
    Esta es una clase base abstracta.
    """
    @abstractmethod
    def enviar_notificacion(self):
        """
        Método abstracto que permite a las subclases a implementar la lógica
        específica de envío de una notificación.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        pass