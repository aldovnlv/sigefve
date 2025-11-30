# Config.py

import sqlite3


class Config:
    """
    Clase que agrupa las constantes y parámetros de configuración básicos
    utilizados por el microservicio de Python (Análisis y Alertas) del sistema SIGEFVE.
    """

    """
    Configuración de base de datos
    La especificación del proyecto requiere usar SQLite para la persistencia local en Python. 
    """
    RUTA_DB = "alertas.db"

    """
    Configuración del Servidor Flask
    El microservicio de Python utiliza Flask para exponer endpoints REST. 
    """
    FLASK_HOST = "0.0.0.0"
    FLASK_PORT = 5001

    """
    Umbrales de alertas críticas
    Estos umbrales definen las condiciones para generar alertas urgentes y de mantenimiento.
    """
    BATERIA_BAJA_UMBRAL = 20  # Batería baja (<20%) genera una Alerta Urgente.
    TEMPERATURA_ALTA_UMBRAL = 70  # Temperatura alta del motor (>70°C) genera una Alerta Urgente.
    KILOMETRAJE_MANTENIMIENTO = 5000  # Mantenimiento requerido (cada 5000 km) genera una Alerta Mantenimiento.

    """
    Constantes de interconexión
    URL del microservicio de Java, usado para obtener datos de vehículos o rutas (comunicación REST).
    """
    JAVA_URL = "https://tajava.xipatlani.tk"


def obtener_conexion():
    """
    Establece y retorna una conexión al motor de base de datos SQLite.
    
    Parámetros:
        Ninguno
    
    Returns:
        sqlite3.Connection: Objeto de conexión a la base de datos.
    """
    # Se añade `check_same_thread=False` para permitir que Flask (que usa threading)
    # acceda a la misma conexión desde diferentes hilos.
    return sqlite3.connect(Config.RUTA_DB, check_same_thread=False)


def init_db():
    """
    Inicializa la base de datos SQLite creando las tablas necesarias
    para el registro de alertas y sus subclases (Mantenimiento y Urgente).
    
    Parámetros:
        Ninguno
    
    Returns:
        None
    """
    conn = obtener_conexion()
    cursor = conn.cursor()

    # Tabla de alertas (Tabla base para la herencia)
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS alerta (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            descripcion TEXT NOT NULL,
            prioridad INTEGER NOT NULL,
            fecha_generacion TEXT NOT NULL,
            estado INTEGER NOT NULL,
            id_vehiculo INTEGER NOT NULL
        )
    """
    )

    # Tabla de alertas de mantenimiento (Extensión de la tabla base)
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS alerta_mantenimiento (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_alerta INTEGER NOT NULL,
            mantenimiento DATETIME DEFAULT CURRENT_TIMESTAMP 
        )
    """
    )

    # Tabla de alertas urgentes (Extensión de la tabla base)
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS alerta_urgente (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_alerta INTEGER NOT NULL,
            mensaje TEXT NOT NULL
        )
    """
    )

    conn.commit()
    conn.close()


init_db()