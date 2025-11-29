# Eventos/ObservadorTelemetria.py

from Eventos.GestorEventos import GestorEventos

class ObservadorTelemetria:
    """
    Observador que recibe datos de telemetría desde el microservicio Java.
    """

    def __init__(self):
        self._suscriptores = []

    def recibir_datos(self, telemetria):
        # Notificar al gestor para generar alertas
        evento = {'telemetria': telemetria}
        self.notificar_suscriptores(evento)

    def suscribir(self, gestor):
        if gestor not in self._suscriptores:
            self._suscriptores.append(gestor)

    def desuscribir(self, gestor):
        if gestor in self._suscriptores:
            self._suscriptores.remove(gestor)

    def notificar_suscriptores(self, evento):
        for suscriptor in self._suscriptores:
            suscriptor.actualizar(evento)