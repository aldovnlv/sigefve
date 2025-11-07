from Abstractas.Notificable import Notificable
from Abstractas.Registrable import Registrable
from Alertas.Alerta import Alerta

class AlertaUrgente(Alerta, Notificable, Registrable):
    """
    Alerta urgente.
    """
    def enviar_notificacion(self):
        print(f"[NOTIFICACIÓN URGENTE] {self.descripcion}")

    def registrar_evento(self):
        self.guardar()

    def notificar_inmediatamente(self):
        self.enviar_notificacion()
        self.registrar_evento()