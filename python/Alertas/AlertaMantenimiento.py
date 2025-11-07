from Config import Config
from Abstractas.Notificable import Notificable
from Abstractas.Registrable import Registrable
from Alertas.Alerta import Alerta

class AlertaMantenimiento(Alerta, Notificable, Registrable):
    """
    Alerta para mantenimiento preventivo.
    """
    def __init__(self, descripcion, prioridad, id_vehiculo, kilometraje_actual):
        super().__init__("MANTENIMIENTO", descripcion, prioridad, id_vehiculo)
        self._kilometraje_actual = kilometraje_actual

    def enviar_notificacion(self):
        print(f"[NOTIFICACIÓN MANTENIMIENTO] {self.descripcion}")

    def registrar_evento(self):
        self.guardar()

    def verificar_kilometraje(self):
        return self._kilometraje_actual >= Config.KILOMETRAJE_MANTENIMIENTO