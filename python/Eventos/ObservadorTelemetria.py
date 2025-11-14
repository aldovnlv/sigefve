# Eventos/ObservadorTelemetria.py

from Eventos.GestorEventos import GestorEventos


class ObservadorTelemetria:
    """
    Observador que recibe datos de telemetría desde el microservicio Java.
    """

    def __init__(self, gestor = GestorEventos()):
        self._suscriptores = []
        self._gestor = gestor

    def recibir_datos(self, telemetria):
        #self._analizador.procesar_datos(telemetria)

        # Notificar al gestor para generar alertas
        evento = {'telemetria': telemetria}
        self._gestor.actualizar(evento)

    def suscribir(self, gestor):
        if gestor not in self._suscriptores:
            self._suscriptores.append(gestor)

    def desuscribir(self, gestor):
        if gestor in self._suscriptores:
            self._suscriptores.remove(gestor)

    def notificar_suscriptores(self, evento):
        for suscriptor in self._suscriptores:
            suscriptor.actualizar(evento)