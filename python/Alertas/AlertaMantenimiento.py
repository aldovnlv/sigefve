# Alertas/AlertaMantenimiento.py

import Config
from Abstractas.Notificable import Notificable
from Abstractas.Registrable import Registrable
from Alertas.Alerta import Alerta
from datetime import datetime, timedelta


class AlertaMantenimiento(Alerta, Notificable, Registrable):
    """
    Subclase que gestiona las alertas de mantenimiento preventivo.
    Implementa herencia múltiple (Alerta + Notificable + Registrable) según
    la arquitectura del sistema SIGEFVE.
    """

    def __init__(
        self, id_vehiculo, kilometraje_actual, descripcion="MANTENIMIENTO", prioridad=3
    ):
        """
        Inicializa una alerta de mantenimiento calculando la fecha programada.
        
        Parámetros:
            id_vehiculo: Identificador del vehículo a evaluar.
            kilometraje_actual: Lectura actual del odómetro del vehículo.
            descripcion: Etiqueta de la alerta (por defecto "MANTENIMIENTO").
            prioridad: Nivel de importancia (por defecto 3, prioridad baja).
        
        Returns:
            None
        """
        super().__init__(descripcion, id_vehiculo, prioridad)
        self._kilometraje_actual = kilometraje_actual
        # Se programa el mantenimiento tentativamente para 10 días después de la generación
        self._fecha_mantenimiento = self.fecha_generacion + timedelta(days=10)

    def verificar_kilometraje(self):
        """
        Evalúa si el vehículo ha superado el umbral de kilometraje definido
        en la configuración. Si es así, dispara la notificación y el registro.
        
        Parámetros:
            Ninguno
        
        Returns:
            bool: True si requiere mantenimiento, False en caso contrario.
        """
        mantenimiento = (
            self._kilometraje_actual >= Config.Config.KILOMETRAJE_MANTENIMIENTO
        )

        if mantenimiento:
            self.enviar_notificacion()
            self.registrar_evento()

        return mantenimiento

    def guardar(self):
        """
        Persiste la alerta base y los detalles específicos de mantenimiento
        en la base de datos (tabla alerta_mantenimiento).
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        # Primero se guarda la parte genérica de la Alerta para obtener el ID
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
        """
        Implementación de la interfaz Notificable.
        Simula el envío de un aviso sobre la fecha programada de mantenimiento.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        print(f"[NOTIFICACIÓN MANTENIMIENTO] Mantenimiento en {self._fecha_mantenimiento}")

    def registrar_evento(self):
        """
        Implementación de la interfaz Registrable.
        Guarda el evento en la base de datos llamando al método guardar.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        self.guardar()

    def to_dict(self):
        """
        Convierte la alerta de mantenimiento a un diccionario serializable,
        incluyendo la fecha programada y el tipo específico.
        
        Parámetros:
            Ninguno
        
        Returns:
            dict: Diccionario con datos de la alerta y fecha de mantenimiento.
        """
        base_dict = super().to_dict()
        base_dict["tipo"] = "mantenimiento"
        base_dict["mantenimiento"] = (
            self._fecha_mantenimiento.isoformat()
            if isinstance(self._fecha_mantenimiento, datetime)
            else str(self._fecha_mantenimiento)
        )
        return base_dict