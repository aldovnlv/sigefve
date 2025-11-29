# Alertas/Alerta.py

import Config
import datetime


class Alerta:
    """
    Clase base para las alertas generadas por el sistema.
    """

    def __init__(
        self,
        descripcion,
        id_vehiculo,
        prioridad,
        id=-1,
        estado=True,
        fecha_generacion=datetime.datetime.now(),
    ):
        self._id = id
        self._descripcion = descripcion
        self._prioridad = prioridad
        self._fecha_generacion = fecha_generacion
        self._estado = estado
        self._id_vehiculo = id_vehiculo

    @property
    def id(self):
        return self._id

    @id.setter
    def id(self, id):
        self._id = id

    @property
    def descripcion(self):
        return self._descripcion

    @descripcion.setter
    def descripcion(self, descripcion):
        self._descripcion = descripcion

    @property
    def prioridad(self):
        return self._prioridad

    @prioridad.setter
    def prioridad(self, prioridad):
        self._prioridad = prioridad

    @property
    def fecha_generacion(self):
        return self._fecha_generacion

    @fecha_generacion.setter
    def fecha_generacion(self, fecha_generacion):
        self._fecha_generacion = fecha_generacion

    @property
    def estado(self):
        return self._estado

    @property
    def id_vehiculo(self):
        return self._id_vehiculo

    @id_vehiculo.setter
    def id_vehiculo(self, id_vehiculo):
        self._id_vehiculo = id_vehiculo

    def guardar(self):
        conn = Config.obtener_conexion()
        cursor = conn.cursor()
        cursor.execute(
            """
                       INSERT INTO alerta (descripcion, prioridad, fecha_generacion, estado, id_vehiculo)
                       VALUES (?, ?, ?, ?, ?)
                       """,
            (
                self._descripcion,
                self._prioridad,
                self._fecha_generacion,
                self._estado,
                self._id_vehiculo,
            ),
        )
        self._id = cursor.lastrowid
        conn.commit()
        conn.close()
        return self._id

    def to_dict(self):
        """Convierte la alerta a un diccionario serializable."""
        return {
            "id": self._id,
            "descripcion": self._descripcion,
            "prioridad": self._prioridad,
            "fecha_generacion": (
                self._fecha_generacion.isoformat()
                if isinstance(self._fecha_generacion, datetime.datetime)
                else str(self._fecha_generacion)
            ),
            "estado": self._estado,
            "id_vehiculo": self._id_vehiculo,
            "tipo": "general",
        }
