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
    Gestiona los eventos de telemetría y alertas de forma asincrónica.
    """

    def __init__(self):
        super().__init__()
        self._hilo_activo = False
        self._cola_eventos = Queue()
        self._hilo = None

    def iniciar_hilo(self):
        if not self._hilo_activo:
            self._hilo_activo = True
            self._hilo = threading.Thread(target=self._procesar_cola)
            self._hilo.daemon = True
            self._hilo.start()
            print("[GESTOR] Hilo de eventos iniciado")

    def detener_hilo(self):
        self._hilo_activo = False
        if self._hilo:
            self._hilo.join()
        print("[GESTOR] Hilo de eventos detenido")

    def actualizar(self, evento):
        self._cola_eventos.put(evento)

    def procesar_evento(self, evento):
        telemetria = evento.get("telemetria", {})
        id_vehiculo = telemetria.get("id_vehiculo")
        nivel_bateria = telemetria.get("nivel_bateria", 100)
        temperatura = telemetria.get("temperatura_motor", 0)
        kilometros_totales = telemetria.get("kilometros_totales", 0)

        # Generar alertas según condiciones (usando umbrales)
        if nivel_bateria < Config.BATERIA_BAJA_UMBRAL:
            alerta = AlertaUrgente(
                id_vehiculo,
                f"Vehículo {id_vehiculo}: Batería crítica ({nivel_bateria}%)",
                1,
            )
            alerta.notificar_inmediatamente()

        if temperatura > Config.TEMPERATURA_ALTA_UMBRAL:
            alerta = AlertaUrgente(
                id_vehiculo,
                f"Vehículo {id_vehiculo}: Temperatura del motor alta ({temperatura}°C)",
                2,
            )
            alerta.notificar_inmediatamente()

        mantenimiento = AlertaMantenimiento(id_vehiculo, kilometros_totales)
        mantenimiento.verificar_kilometraje()

    def _procesar_cola(self):
        while self._hilo_activo:
            if not self._cola_eventos.empty():
                evento = self._cola_eventos.get()
                self.procesar_evento(evento)
            time.sleep(0.1)
