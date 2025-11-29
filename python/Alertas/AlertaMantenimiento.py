# Alertas/AlertaMantenimiento.py

import Config
from Abstractas.Notificable import Notificable
from Abstractas.Registrable import Registrable
from Alertas.Alerta import Alerta
from datetime import datetime, timedelta


class AlertaMantenimiento(Alerta, Notificable, Registrable):
    """
    Alerta para mantenimiento preventivo.
    """

    def __init__(
        self, id_vehiculo, kilometraje_actual, descripcion="MANTENIMIENTO", prioridad=3
    ):
        super().__init__(descripcion, id_vehiculo, prioridad)
        self._kilometraje_actual = kilometraje_actual
        self._fecha_mantenimiento = self.fecha_generacion + timedelta(days=10)

    def verificar_kilometraje(self):
        mantenimiento = (
            self._kilometraje_actual >= Config.Config.KILOMETRAJE_MANTENIMIENTO
        )

        if mantenimiento:
            self.enviar_notificacion()
            self.registrar_evento()

        return mantenimiento

    def guardar(self):
        id_alerta = super().guardar()

        conn = Config.obtener_conexion()
        cursor = conn.cursor()
        cursor.execute(
            """
                       INSERT INTO alerta_mantenimiento (id_alerta, mantenimiento)
                       VALUES (?, ?)
                       """,
            (id_alerta, self._fecha_mantenimiento),
        )
        self._id = cursor.lastrowid
        conn.commit()
        conn.close()

    def enviar_notificacion(self):
        print(f"[NOTIFICACIÓN MANTENIMIENTO] Mantenimiento en {self._fecha_mantenimiento}")

    def registrar_evento(self):
        self.guardar()

    def to_dict(self):
        """Convierte la alerta de mantenimiento a un diccionario serializable."""
        base_dict = super().to_dict()
        base_dict["tipo"] = "mantenimiento"
        base_dict["mantenimiento"] = (
            self._fecha_mantenimiento.isoformat()
            if isinstance(self._fecha_mantenimiento, datetime)
            else str(self._fecha_mantenimiento)
        )
        return base_dict
