# Alertas/Alerta.py

import Config
import datetime


class Alerta:
    """
    Clase base para las alertas generadas por el sistema SIGEFVE.
    Esta entidad es base para el módulo de análisis y reportes.
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
        """
        Inicializa una nueva instancia de Alerta.
        
        Parámetros:
            descripcion: Detalle textual de la causa de la alerta.
            id_vehiculo: Identificador único del vehículo asociado a la alerta.
            prioridad: Nivel de urgencia de la alerta para su ordenamiento (valores del 1 al 3, donde 1 es la prioridad más alta).
            id: Identificador en base de datos (por defecto -1 si es nueva).
            estado: Indica si la alerta está activa (True) o resuelta (False).
            fecha_generacion: Marca de tiempo de la creación de la alerta.
        
        Returns:
            None
        """
        self._id = id
        self._descripcion = descripcion
        self._prioridad = prioridad
        self._fecha_generacion = fecha_generacion
        self._estado = estado
        self._id_vehiculo = id_vehiculo

    @property
    def id(self):
        """
        Obtiene el identificador único de la alerta.
        
        Parámetros:
            Ninguno
        
        Returns:
            int: El ID de la alerta.
        """
        return self._id

    @id.setter
    def id(self, id):
        """
        Establece el identificador único de la alerta.
        
        Parámetros:
            id: Nuevo identificador entero.
        
        Returns:
            None
        """
        self._id = id

    @property
    def descripcion(self):
        """
        Obtiene la descripción de la alerta.
        
        Parámetros:
            Ninguno
        
        Returns:
            str: Texto descriptivo de la alerta.
        """
        return self._descripcion

    @descripcion.setter
    def descripcion(self, descripcion):
        """
        Actualiza la descripción de la alerta.
        
        Parámetros:
            descripcion: Nueva descripción textual.
        
        Returns:
            None
        """
        self._descripcion = descripcion

    @property
    def prioridad(self):
        """
        Obtiene la prioridad de la alerta para listados ordenados.
        
        Parámetros:
            Ninguno
        
        Returns:
            int: Nivel de prioridad.
        """
        return self._prioridad

    @prioridad.setter
    def prioridad(self, prioridad):
        """
        Establece el nivel de prioridad.
        
        Parámetros:
            prioridad: Nuevo nivel de prioridad.
        
        Returns:
            None
        """
        self._prioridad = prioridad

    @property
    def fecha_generacion(self):
        """
        Obtiene la fecha y hora de generación de la alerta.
        
        Parámetros:
            Ninguno
        
        Returns:
            datetime: Objeto datetime con la fecha de creación.
        """
        return self._fecha_generacion

    @fecha_generacion.setter
    def fecha_generacion(self, fecha_generacion):
        """
        Establece la fecha de generación manualmente.
        
        Parámetros:
            fecha_generacion: Nuevo objeto datetime.
        
        Returns:
            None
        """
        self._fecha_generacion = fecha_generacion

    @property
    def estado(self):
        """
        Verifica el estado activo o inactivo de la alerta.
        
        Parámetros:
            Ninguno
        
        Returns:
            bool: True si está activa, False si está resuelta.
        """
        return self._estado

    @property
    def id_vehiculo(self):
        """
        Obtiene el ID del vehículo que originó la alerta.
        
        Parámetros:
            Ninguno
        
        Returns:
            int: Identificador del vehículo.
        """
        return self._id_vehiculo

    @id_vehiculo.setter
    def id_vehiculo(self, id_vehiculo):
        """
        Asigna el ID del vehículo a la alerta.
        
        Parámetros:
            id_vehiculo: Identificador del vehículo.
        
        Returns:
            None
        """
        self._id_vehiculo = id_vehiculo

    def guardar(self):
        """
        Persiste la información de la alerta en la base de datos SQLite.
        Actualiza el atributo _id con el lastrowid generado tras la inserción.
        
        Parámetros:
            Ninguno
        
        Returns:
            int: El ID asignado por la base de datos a la nueva alerta.
        """
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
        """
        Convierte la instancia de la alerta a un diccionario serializable.
        
        Parámetros:
            Ninguno
        
        Returns:
            dict: Representación en diccionario de los atributos de la alerta.
        """
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