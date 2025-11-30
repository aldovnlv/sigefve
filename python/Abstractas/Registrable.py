# Abstractas/Registrable.py

from abc import ABC, abstractmethod


class Registrable(ABC):
    """
    Define la interfaz para cualquier objeto del sistema que deba tener la
    capacidad de registrarse en la base de datos. Es una clase base abstracta.
    """
    @abstractmethod
    def registrar_evento(self):
        """
        Método abstracto que permite a las subclases a implementar el mecanismo
        para guardar el estado del objeto.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        pass