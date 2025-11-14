import Config
import datetime

class Alerta:
    """
    Clase base para las alertas generadas por el sistema.
    """
    def __init__(self, tipo, descripcion, prioridad, id_vehiculo, id = -1, estado=False, fecha_generacion = datetime.datetime.now()):
        self._id = id
        self._tipo = tipo
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
    def tipo(self):
        return self._tipo

    @tipo.setter
    def tipo(self, tipo):
        self._tipo = tipo

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

    def activar(self):
        self._estado = True

    def desactivar(self):
        self._estado = False

    def guardar(self):
        conn = Config.obtener_conexion()
        cursor = conn.cursor()
        cursor.execute('''
                       INSERT INTO alertas (tipo, descripcion, prioridad, fecha_generacion, estado, id_vehiculo)
                       VALUES (?, ?, ?, ?, ?, ?)
                       ''', (self._tipo, self._descripcion, self._prioridad, self._fecha_generacion, self._estado,
                             self._id_vehiculo))
        self._id = cursor.lastrowid
        conn.commit()
        conn.close()