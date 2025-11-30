# Alertas/AlertaUrgente.py

import Config
from Abstractas.Notificable import Notificable
from Abstractas.Registrable import Registrable
from Alertas.Alerta import Alerta


class AlertaUrgente(Alerta, Notificable, Registrable):
    """
    Subclase para alertas de alta prioridad (Batería baja, Temperatura alta).
    Hereda de Alerta, Notificable y Registrable.
    """

    def __init__(self, id_vehiculo, mensaje, prioridad, descripcion="URGENTE"):
        """
        Inicializa una alerta urgente con un mensaje específico del problema.
        
        Parámetros:
            id_vehiculo: Identificador del vehículo con la incidencia.
            mensaje: Detalle técnico del problema.
            prioridad: Nivel de urgencia (1 o 2, prioridad alta y media, respectivamente).
            descripcion: Categoría de la alerta (por defecto "URGENTE").
        
        Returns:
            None
        """
        super().__init__(descripcion, id_vehiculo, prioridad)
        self._mensaje = mensaje

    def guardar(self):
        """
        Persiste la alerta base y el mensaje urgente específico en la 
        base de datos (tabla alerta_urgente).
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
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
        """
        Método compuesto que fuerza la notificación y el registro inmediatos.
        Utilizado cuando se detectan valores críticos en la telemetría.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        self.enviar_notificacion()
        self.registrar_evento()

    def enviar_notificacion(self):
        """
        Implementación de la interfaz Notificable.
        Emite el mensaje urgente a la consola.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        print(f"[NOTIFICACIÓN URGENTE] {self._mensaje}")

    def registrar_evento(self):
        """
        Implementación de la interfaz Registrable.
        Asegura que la alerta urgente quede guardada en el historial.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        self.guardar()

    def to_dict(self):
        """
        Convierte la alerta urgente a un diccionario serializable,
        añadiendo el mensaje específico de la alerta.
        
        Parámetros:
            Ninguno
        
        Returns:
            dict: Diccionario con datos de la alerta y mensaje de alerta.
        """
        base_dict = super().to_dict()
        base_dict["tipo"] = "urgente"
        base_dict["mensaje"] = self._mensaje
        return base_dict