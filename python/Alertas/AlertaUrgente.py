# Alertas/AlertaUrgente.py

import Config
from Abstractas.Notificable import Notificable
from Abstractas.Registrable import Registrable
from Alertas.Alerta import Alerta


class AlertaUrgente(Alerta, Notificable, Registrable):
    """
    Alerta urgente.
    """

    def __init__(self, id_vehiculo, mensaje, prioridad, descripcion="URGENTE"):
        super().__init__(descripcion, id_vehiculo, prioridad)
        self._mensaje = mensaje

    def guardar(self):
        id_alerta = super().guardar()

        conn = Config.obtener_conexion()
        cursor = conn.cursor()
        cursor.execute(
            """
                       INSERT INTO alerta_urgente (id_alerta, mensaje)
                       VALUES (?, ?)
                       """,
            (id_alerta, self._mensaje),
        )
        self._id = cursor.lastrowid
        conn.commit()
        conn.close()

    def notificar_inmediatamente(self):
        self.enviar_notificacion()
        self.registrar_evento()

    def enviar_notificacion(self):
        print(f"[NOTIFICACIÓN URGENTE] {self._mensaje}")

    def registrar_evento(self):
        self.guardar()

    def to_dict(self):
        """Convierte la alerta urgente a un diccionario serializable."""
        base_dict = super().to_dict()
        base_dict["tipo"] = "urgente"
        base_dict["mensaje"] = self._mensaje
        return base_dict
