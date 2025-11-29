# Abstractas/Registrable.py

from abc import ABC, abstractmethod

class Registrable(ABC):
    """
    Clase abstracta para objetos que pueden registrar eventos.
    """
    @abstractmethod
    def registrar_evento(self):
        pass