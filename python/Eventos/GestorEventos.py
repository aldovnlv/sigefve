# Eventos/GestorEventos.py

from queue import Queue
import threading
from threading import Thread
import time

from Alertas.AlertaUrgente import AlertaUrgente
from Config import Config
from Alertas.AlertaMantenimiento import AlertaMantenimiento


class GestorEventos(Thread):
    """
    Gestiona los eventos de telemetría de forma asincrónica utilizando un hilo
    dedicado y una cola de eventos. Actúa como el observador (Observer) que
    consume los datos para el análisis y generación de alertas.
    """

    def __init__(self):
        """
        Inicializa el gestor, la cola de eventos (Queue) y los estados del hilo.
        Hereda de Thread para la ejecución asíncrona.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        super().__init__()
        self._hilo_activo = False
        self._cola_eventos = Queue()
        self._hilo = None

    def iniciar_hilo(self):
        """
        Crea y arranca el hilo dedicado a procesar la cola de eventos.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        if not self._hilo_activo:
            self._hilo_activo = True
            self._hilo = threading.Thread(target=self._procesar_cola)
            self._hilo.daemon = True
            self._hilo.start()
            print("[GESTOR] Hilo de eventos iniciado")

    def detener_hilo(self):
        """
        Detiene el hilo de procesamiento de forma segura y espera a que finalice.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        self._hilo_activo = False
        if self._hilo:
            self._hilo.join()
        print("[GESTOR] Hilo de eventos detenido")

    def actualizar(self, evento):
        """
        Método de la interfaz Observer. Recibe un evento del ObservadorTelemetria
        y lo encola para su procesamiento asíncrono posterior.
        
        Parámetros:
            evento: Diccionario que contiene la telemetría del vehículo.
        
        Returns:
            None
        """
        self._cola_eventos.put(evento)

    def procesar_evento(self, evento):
        """
        Extrae los datos clave de la telemetría y evalúa los umbrales de alerta.
        Genera y notifica las alertas Urgentes o de Mantenimiento según corresponda.
        
        Parámetros:
            evento: Diccionario que contiene la telemetría a analizar.
        
        Returns:
            None
        """
        telemetria = evento.get("telemetria", {})
        id_vehiculo = telemetria.get("id_vehiculo")
        nivel_bateria = telemetria.get("nivel_bateria", 100)
        temperatura = telemetria.get("temperatura_motor", 0)
        kilometros_totales = telemetria.get("kilometros_totales", 0)

        # Generar alertas urgentes: Batería baja
        if nivel_bateria < Config.BATERIA_BAJA_UMBRAL:
            alerta = AlertaUrgente(
                id_vehiculo,
                f"Vehículo {id_vehiculo}: Batería crítica ({nivel_bateria}%)",
                1,
            )
            alerta.notificar_inmediatamente()

        # Generar alertas urgentes: Temperatura alta
        if temperatura > Config.TEMPERATURA_ALTA_UMBRAL:
            alerta = AlertaUrgente(
                id_vehiculo,
                f"Vehículo {id_vehiculo}: Temperatura del motor alta ({temperatura}°C)",
                2,
            )
            alerta.notificar_inmediatamente()

        # Generar alertas de mantenimiento
        mantenimiento = AlertaMantenimiento(id_vehiculo, kilometros_totales)
        mantenimiento.verificar_kilometraje()

    def _procesar_cola(self):
        """
        Función objetivo del hilo asíncrono. Consume continuamente los eventos
        de la cola mientras el hilo esté activo.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        while self._hilo_activo:
            if not self._cola_eventos.empty():
                evento = self._cola_eventos.get()
                self.procesar_evento(evento)
            # Pausa breve para evitar consumo excesivo de CPU
            time.sleep(0.1)