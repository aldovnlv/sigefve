# SIGEFVE - Sistema de Gestión de Flota de Vehículos Eléctricos

## Módulo Java

### Descripción

Módulo Java del SIGEFVE responsable de la gestión de vehículos, telemetría y rutas de entrega. Implementa un servidor HTTP básico con JDBC para conectarse a PostgreSQL.

### Arquitectura

```
com.sigefve/
├── config/
│   └── ConfiguracionBaseDatos.java     # Configuración y conexión a PostgreSQL
├── controladores/
│   ├── ControladorVehiculos.java       # API REST para vehículos
│   ├── ControladorTelemetria.java      # API REST para telemetría
│   └── ControladorRutas.java           # API REST para rutas
├── dao/
│   ├── VehiculoDAO.java                # Acceso a datos de vehículos
│   ├── TelemetriaDAO.java              # Acceso a datos de telemetría
│   ├── RutaDAO.java                    # Acceso a datos de rutas
│   └── EntregaDAO.java                 # Acceso a datos de entregas
├── enums/
│   ├── EstadoVehiculo.java             # DISPONIBLE, EN_RUTA, MANTENIMIENTO, CARGANDO
│   ├── TipoVehiculo.java               # VAN, BICICLETA_ELECTRICA, MOTO_ELECTRICA
│   └── EstadoEntrega.java              # PENDIENTE, EN_CAMINO, COMPLETADA, FALLIDA
├── modelos/
│   ├── Vehiculo.java                   # Clase abstracta base
│   ├── VehiculoElectrico.java          # Clase abstracta con propiedades eléctricas
│   ├── Van.java                        # Vehículo tipo Van
│   ├── BicicletaElectrica.java         # Bicicleta eléctrica
│   ├── MotoElectrica.java              # Moto eléctrica
│   ├── Telemetria.java                 # Datos de sensores
│   ├── Ruta.java                       # Ruta de entregas
│   └── Entrega.java                    # Entrega individual
├── servicios/
│   ├── VehiculoServicio.java           # Lógica de negocio de vehículos
│   ├── TelemetriaServicio.java         # Lógica de negocio de telemetría
│   └── RutaServicio.java               # Lógica de negocio de rutas
├── simulador/
│   └── SimuladorTelemetria.java        # Simulador de telemetría cada 15s
├── utils/
│   └── InicializadorDatos.java         # Script para datos de prueba
└── ServidorHTTP.java                   # Servidor HTTP principal
```

### 🚀 Tecnologías

- **Java 17** (LTS)
- **Maven** para gestión de dependencias
- **PostgreSQL 15** como base de datos
- **JDBC** para acceso a datos
- **Gson** para serialización JSON
- **HttpServer** (java.net) para servidor HTTP
- **Docker** para containerización

### Dependencias

```xml
<!-- PostgreSQL JDBC Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.0</version>
</dependency>

<!-- Gson para JSON -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### Modelo de Datos

#### Tabla: vehiculos

```sql
CREATE TABLE vehiculos (
    id SERIAL PRIMARY KEY,
    placa VARCHAR(20) UNIQUE NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    capacidad_bateria DOUBLE PRECISION,
    autonomia_maxima DOUBLE PRECISION,
    consumo_promedio DOUBLE PRECISION,
    capacidad_carga DOUBLE PRECISION,
    numero_asientos INTEGER,
    tiene_canasta_extra BOOLEAN,
    tiene_top_case BOOLEAN,
    kilometraje_total DOUBLE PRECISION DEFAULT 0,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabla: telemetria

```sql
CREATE TABLE telemetria (
    id SERIAL PRIMARY KEY,
    vehiculo_id INTEGER NOT NULL,
    nivel_bateria DOUBLE PRECISION NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    temperatura_motor DOUBLE PRECISION NOT NULL,
    velocidad_actual DOUBLE PRECISION NOT NULL,
    kilometraje_actual DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id) ON DELETE CASCADE
);
```

#### Tabla: rutas

```sql
CREATE TABLE rutas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    vehiculo_id INTEGER,
    distancia_total DOUBLE PRECISION NOT NULL,
    numero_entregas INTEGER DEFAULT 0,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP,
    completada BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id) ON DELETE SET NULL
);
```

#### Tabla: entregas

```sql
CREATE TABLE entregas (
    id SERIAL PRIMARY KEY,
    ruta_id INTEGER NOT NULL,
    direccion_destino VARCHAR(300) NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    descripcion_paquete TEXT,
    peso_kg DOUBLE PRECISION NOT NULL,
    estado VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_completada TIMESTAMP,
    notas_entrega TEXT,
    FOREIGN KEY (ruta_id) REFERENCES rutas(id) ON DELETE CASCADE
);
```

### API REST Endpoints

#### Vehículos

**GET /vehiculos**

- Obtener todos los vehículos
- Response: `200 OK` con array de vehículos

**GET /vehiculos/:id**

- Obtener un vehículo por ID
- Response: `200 OK` con vehículo o `404 Not Found`

**GET /vehiculos?estado=DISPONIBLE**

- Filtrar vehículos por estado
- Response: `200 OK` con array de vehículos

**POST /vehiculos**

- Crear un nuevo vehículo
- Body ejemplo:

```json
{
  "tipo": "VAN",
  "placa": "VAN-001",
  "modelo": "Mercedes eSprinter",
  "anio": 2023,
  "capacidadBateria": 90.0,
  "autonomiaMaxima": 150.0,
  "capacidadCarga": 1500.0,
  "numeroAsientos": 3
}
```

- Response: `201 Created` con ID del vehículo

**PUT /vehiculos/:id**

- Actualizar un vehículo completo
- Body: Mismo formato que POST
- Response: `200 OK`

**PUT /vehiculos/:id/estado**

- Cambiar solo el estado de un vehículo
- Body:

```json
{
  "estado": "EN_RUTA"
}
```

- Response: `200 OK`

**DELETE /vehiculos/:id**

- Eliminar un vehículo
- Response: `200 OK` o `404 Not Found`

#### Telemetría

**GET /telemetria/vehiculo/:id**

- Obtener historial de telemetría de un vehículo
- Query params: `?limite=100` (opcional, default: 100)
- Response: `200 OK` con array de telemetría

**GET /telemetria/vehiculo/:id/ultima**

- Obtener la última telemetría de un vehículo
- Response: `200 OK` con telemetría o `404 Not Found`

**POST /telemetria**

- Registrar nueva telemetría (normalmente usado por el simulador)
- Body:

```json
{
  "vehiculoId": 1,
  "nivelBateria": 75.5,
  "latitud": 20.5288,
  "longitud": -100.8157,
  "temperaturaMotor": 45.2,
  "velocidadActual": 60.0,
  "kilometrajeActual": 1250.5
}
```

- Response: `201 Created` con ID

#### Rutas

**GET /rutas**

- Obtener todas las rutas activas (no completadas)
- Response: `200 OK` con array de rutas

**GET /rutas/:id**

- Obtener una ruta por ID (incluye entregas)
- Response: `200 OK` con ruta o `404 Not Found`

**GET /rutas/:id/entregas**

- Obtener entregas de una ruta específica
- Response: `200 OK` con array de entregas

**POST /rutas**

- Crear una nueva ruta
- Body:

```json
{
  "nombre": "Entregas Zona Centro",
  "distanciaTotal": 15.5,
  "vehiculoId": 1
}
```

- Response: `201 Created` con ID

**POST /rutas/:id/entregas**

- Agregar una entrega a una ruta
- Body:

```json
{
  "direccionDestino": "Av. Juárez 123",
  "latitud": 20.5288,
  "longitud": -100.8157,
  "descripcionPaquete": "Documentos importantes",
  "pesoKg": 2.5
}
```

- Response: `201 Created` con ID

**PUT /rutas/:id/asignar**

- Asignar un vehículo a una ruta
- Body:

```json
{
  "vehiculoId": 1
}
```

- Response: `200 OK`
- Nota: Cambia automáticamente el estado del vehículo a EN_RUTA

**PUT /rutas/:id/completar**

- Marcar una ruta como completada
- Response: `200 OK`
- Nota: Cambia automáticamente el estado del vehículo a DISPONIBLE

#### Health Check

**GET /health**

- Verificar estado del servicio
- Response: `200 OK`

```json
{
  "status": "OK",
  "servicio": "SIGEFVE-Java"
}
```

### Simulador de Telemetría

El simulador genera automáticamente datos de telemetría cada 15 segundos para todos los vehículos:

- **Vehículos EN_RUTA**: Consumen batería, aumentan kilometraje, se mueven en el mapa
- **Vehículos CARGANDO**: Recuperan batería, permanecen estáticos
- **Vehículos MANTENIMIENTO**: Datos estáticos
- **Vehículos DISPONIBLE**: Datos estables con carga lenta

El simulador considera:

- Velocidad máxima según tipo de vehículo
- Consumo de batería realista
- Temperatura del motor variable
- Movimiento geográfico dentro de Celaya, GTO

### Instalación y Ejecución

#### Opción 1: Con Docker (Recomendado)

1. **Construir y ejecutar con Docker Compose**:

```bash
docker-compose up --build
```

El servicio estará disponible en `http://localhost:8585`

#### Opción 2: Ejecución Local

1. **Requisitos previos**:

   - Java 17 o superior
   - Maven 3.6+
   - PostgreSQL 15+ corriendo en localhost:54302

2. **Configurar PostgreSQL**:

```bash
psql -U postgres
CREATE DATABASE sigefve;
```

3. **Configurar variables de entorno** (opcional):

```bash
export DB_URL=jdbc:postgresql://localhost:54302/sigefve
export DB_USER=postgres
export DB_PASSWORD=postgres
export JAVA_PORT=8585
```

4. **Compilar el proyecto**:

```bash
mvn clean package
```

5. **Ejecutar**:

```bash
java -jar target/sigefve-java-1.0.0.jar
```

#### Opción 3: Desde IDE

1. Importar el proyecto como proyecto Maven
2. Configurar variables de entorno en la configuración de ejecución
3. Ejecutar la clase `ServidorHTTP.java`

### Inicializar Datos de Prueba

Para cargar datos de prueba (15 vehículos y 3 rutas):

```bash
# Si usas Maven:
mvn exec:java -Dexec.mainClass="com.sigefve.utils.InicializadorDatos"

# Si ya compilaste el JAR:
java -cp target/sigefve-java-1.0.0.jar com.sigefve.utils.InicializadorDatos
```

Esto creará:

- 5 Vans (Mercedes eSprinter)
- 5 Bicicletas Eléctricas (Cargo Bikes)
- 5 Motos Eléctricas (Super Soco)
- 3 Rutas con múltiples entregas

### Pruebas con cURL

**Listar todos los vehículos**:

```bash
curl http://localhost:8585/vehiculos
```

**Obtener vehículo específico**:

```bash
curl http://localhost:8585/vehiculos/1
```

**Crear un nuevo vehículo**:

```bash
curl -X POST http://localhost:8585/vehiculos \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "MOTO_ELECTRICA",
    "placa": "MOTO-999",
    "modelo": "Super Soco TC Max",
    "anio": 2024,
    "capacidadBateria": 3.0,
    "autonomiaMaxima": 80.0,
    "capacidadCarga": 50.0,
    "tieneTopCase": true
  }'
```

**Cambiar estado de vehículo**:

```bash
curl -X PUT http://localhost:8585/vehiculos/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "CARGANDO"}'
```

**Obtener telemetría actual**:

```bash
curl http://localhost:8585/telemetria/vehiculo/1/ultima
```

**Asignar vehículo a ruta**:

```bash
curl -X PUT http://localhost:8585/rutas/1/asignar \
  -H "Content-Type: application/json" \
  -d '{"vehiculoId": 1}'
```

### Integración con Otros Módulos

#### Para el Módulo Python (Análisis y Alertas):

El módulo Python puede consultar directamente los endpoints del módulo Java:

```python
import requests

# Obtener todos los vehículos
response = requests.get('http://java-service:8585/vehiculos')
vehiculos = response.json()

# Obtener telemetría de un vehículo
response = requests.get('http://java-service:8585/telemetria/vehiculo/1')
telemetria = response.json()
```

**Endpoints útiles para Python**:

- `GET /vehiculos` - Analizar flota completa
- `GET /telemetria/vehiculo/:id?limite=1000` - Análisis histórico
- `GET /rutas/:id/entregas` - Estadísticas de entregas

#### Para el Módulo Go (API Gateway):

El API Gateway debe enrutar las peticiones al módulo Java:

```go
// Ejemplo de proxy en Go (Gin)
router.Any("/api/vehiculos/*path", func(c *gin.Context) {
    targetURL := "http://java-service:8585" + c.Request.URL.Path
    // ... implementar proxy reverso
})
```

**Rutas a proxear desde Go**:

- `/api/vehiculos/*` → `http://java-service:8585/vehiculos/*`
- `/api/telemetria/*` → `http://java-service:8585/telemetria/*`
- `/api/rutas/*` → `http://java-service:8585/rutas/*`

### Estructura de Directorios del Proyecto

```
sigefve-java/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── sigefve/
│                   ├── config/
│                   ├── controladores/
│                   ├── dao/
│                   ├── enums/
│                   ├── modelos/
│                   ├── servicios/
│                   ├── simulador/
│                   ├── utils/
│                   └── ServidorHTTP.java
├── pom.xml
├── Dockerfile
├── .dockerignore
└── README.md
```

### Variables de Entorno

| Variable      | Descripción                  | Valor por Defecto                           |
| ------------- | ---------------------------- | ------------------------------------------- |
| `DB_URL`      | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:54302/sigefve` |
| `DB_USER`     | Usuario de PostgreSQL        | `postgres`                                  |
| `DB_PASSWORD` | Contraseña de PostgreSQL     | `postgres`                                  |
| `JAVA_PORT`   | Puerto del servidor HTTP     | `8585`                                      |

### Solución de Problemas

**Error: "No se pudo conectar a PostgreSQL"**

- Verificar que PostgreSQL esté corriendo
- Verificar las credenciales en las variables de entorno
- Si usas Docker, verificar que el servicio `postgres` esté healthy

**Error: "Puerto 8585 ya está en uso"**

- Cambiar el puerto usando la variable `JAVA_PORT`
- O detener el proceso que está usando el puerto 8585

**El simulador no genera telemetría**

- Verificar que haya vehículos en la base de datos
- Revisar los logs del servidor para errores

**Los endpoints retornan 500 Internal Server Error**

- Revisar los logs del servidor
- Verificar que las tablas existan en PostgreSQL
- Verificar que los datos sean válidos

### Logs y Monitoreo

El servidor muestra logs en consola:

```
Iniciando SIGEFVE - Módulo Java
═══════════════════════════════════════
Conectando a PostgreSQL...
Esquema de base de datos inicializado correctamente
Servidor HTTP iniciado en puerto 8585
Iniciando simulador de telemetría...
Simulador iniciado. Generando telemetría cada 15 segundos...
═══════════════════════════════════════
SIGEFVE - Módulo Java en ejecución
API disponible en: http://localhost:8585
═══════════════════════════════════════

[VAN-001] Batería: 85.5% | Temp: 42.3°C | Km: 1250.45
[BICI-001] Batería: 92.1% | Temp: 35.8°C | Km: 450.12
...
```

### Consideraciones de Seguridad

- Las contraseñas de base de datos deben configurarse mediante variables de entorno
- No incluir credenciales en el código
- En producción, usar HTTPS en lugar de HTTP
- Implementar autenticación mediante el API Gateway (módulo Go)
- Validar todos los datos de entrada

### Próximos Pasos

1. Completar el módulo Python para análisis y alertas
2. Completar el módulo Go para API Gateway y autenticación
3. Crear el frontend web
4. Implementar comunicación entre microservicios
5. Agregar más pruebas unitarias
6. Implementar sistema de logging más robusto

### Licencia

Este proyecto es parte del SIGEFVE desarrollado como proyecto educativo.

### Contacto

Para dudas sobre el módulo Java del SIGEFVE, consultar la documentación del proyecto principal.

---

**Versión**: 1.0.0  
**Última actualización**: Octubre 2025
