import sqlite3

class Config:
    """Configuración básica del sistema"""
    # Base de datos SQLite
    RUTA_DB = 'alertas.db'

    # Flask
    FLASK_HOST = '0.0.0.0'
    FLASK_PORT = 5001

    # Umbrales de alertas
    BATERIA_BAJA_UMBRAL = 20
    TEMPERATURA_ALTA_UMBRAL = 70
    KILOMETRAJE_MANTENIMIENTO = 5000

    # Constantes
    JAVA_URL = "https://tajava.xipatlani.tk"


def obtener_conexion():
    """Retorna una conexión a SQLite"""
    return sqlite3.connect(Config.RUTA_DB, check_same_thread=False)


def init_db():
    conn = obtener_conexion()
    cursor = conn.cursor()

    # Tabla de alertas
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS alertas (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo TEXT NOT NULL,
            descripcion TEXT NOT NULL,
            prioridad INTEGER NOT NULL,
            fecha_generacion TEXT NOT NULL,
            estado INTEGER NOT NULL,
            id_vehiculo INTEGER NOT NULL
        )
    ''')

    # Tabla de estadísticas
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS estadisticas (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_vehiculo INTEGER NOT NULL,
            kilometros_totales REAL DEFAULT 0,
            eficiencia_bateria REAL DEFAULT 0,
            entregas_completadas INTEGER DEFAULT 0,
            ultima_actualizacion TEXT
        )
    ''')

    conn.commit()
    conn.close()


init_db()