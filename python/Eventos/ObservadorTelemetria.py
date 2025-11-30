# Eventos/ObservadorTelemetria.py


class ObservadorTelemetria:
    """
    Implementa el sujeto (subject) del patrón Observer. 
    Actúa como el punto de entrada para recibir los datos de telemetría 
    periódicos desde el microservicio Java.
    """

    def __init__(self):
        """
        Inicializa la lista de suscriptores (observadores) que esperan
        ser notificados cuando lleguen nuevos datos.
        
        Parámetros:
            Ninguno
        
        Returns:
            None
        """
        self._suscriptores = []

    def recibir_datos(self, telemetria):
        """
        Método invocado al recibir un nuevo conjunto de datos de telemetría.
        Empaqueta los datos en un evento y notifica a todos los suscriptores.
        
        Parámetros:
            telemetria: Diccionario o estructura de datos que contiene
                        el Nivel de batería, ubicación GPS, temperatura del motor, etc.
        
        Returns:
            None
        """
        # Notificar al gestor para generar alertas
        evento = {'telemetria': telemetria}
        self.notificar_suscriptores(evento)

    def suscribir(self, gestor):
        """
        Registra un nuevo gestor (suscriptor) en la lista para que reciba
        notificaciones de nuevos eventos.
        
        Parámetros:
            gestor: Instancia del objeto suscriptor que implementa un método 'actualizar'.
        
        Returns:
            None
        """
        if gestor not in self._suscriptores:
            self._suscriptores.append(gestor)

    def desuscribir(self, gestor):
        """
        Elimina un suscriptor de la lista de notificaciones.
        
        Parámetros:
            gestor: Instancia del objeto suscriptor a eliminar.
        
        Returns:
            None
        """
        if gestor in self._suscriptores:
            self._suscriptores.remove(gestor)

    def notificar_suscriptores(self, evento):
        """
        Recorre la lista de suscriptores y llama al método 'actualizar'
        de cada uno, enviando el nuevo evento de telemetría.
        
        Parámetros:
            evento: Diccionario que contiene los datos de la telemetría recibida.
        
        Returns:
            None
        """
        for suscriptor in self._suscriptores:
            suscriptor.actualizar(evento)