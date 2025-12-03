# SIGEFVE Frontend - React

Frontend React con patrón MVC para el Sistema de Gestión de Flota de Vehículos Eléctricos (SIGEFVE).

## Características

- ✅ Login con autenticación JWT
- ✅ Rutas protegidas con control de acceso basado en roles
- ✅ Dashboard para Administrador con:
  - Lista de vehículos con estado y batería
  - Alertas activas ordenadas por prioridad
  - Estadísticas de la flota
  - Descarga de reportes en CSV
- ✅ Dashboard para Conductor con:
  - Vista de vehículo asignado
  - Telemetría en tiempo real
  - Rutas y entregas asignadas
- ✅ Integración completa con API Gateway

## Tecnologías

- React 18
- React Router v6
- Axios
- Vite
- CSS Vanilla (sin frameworks)

## Instalación

```bash
cd frontend
npm install
```

## Configuración

El archivo `.env` ya está configurado con la URL del API Gateway:

```
VITE_API_URL=https://apisigefve.xipatlani.tk
```

## Ejecución

### Modo Desarrollo

```bash
npm run dev
```

El servidor se inicia en `http://localhost:5173`

### Build para Producción

```bash
npm run build
npm run preview
```

## Estructura del Proyecto

```
frontend/
├── src/
│   ├── components/         # Componentes React (Vistas)
│   │   ├── Login.jsx
│   │   ├── DashboardAdmin.jsx
│   │   ├── DashboardConductor.jsx
│   │   ├── VehiculosList.jsx
│   │   ├── AlertasList.jsx
│   │   └── ProtectedRoute.jsx
│   ├── services/           # Servicios API (Modelos)
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── vehiculoService.js
│   │   ├── telemetriaService.js
│   │   ├── rutaService.js
│   │   ├── alertaService.js
│   │   └── estadisticaService.js
│   ├── hooks/              # Hooks personalizados (Controladores)
│   │   └── useAuth.js
│   ├── utils/              # Utilidades
│   │   └── tokenUtils.js
│   ├── styles/             # Estilos CSS
│   │   ├── Login.css
│   │   ├── Dashboard.css
│   │   ├── Vehiculos.css
│   │   └── Alertas.css
│   ├── App.jsx             # Componente raíz con rutas
│   ├── main.jsx            # Punto de entrada
│   └── index.css           # Estilos globales
├── index.html
├── package.json
├── vite.config.js
└── .env
```

## Rutas de la Aplicación

- `/` - Redirige a `/login` o al dashboard correspondiente según autenticación
- `/login` - Pantalla de inicio de sesión
- `/admin` - Dashboard del Administrador (requiere rol "Administrador")
- `/conductor` - Dashboard del Conductor (requiere rol "Conductor")

## Uso

### Login

1. Acceder a `http://localhost:5173/login`
2. Ingresar credenciales:
   - Usuario Administrador o Conductor
   - Contraseña
3. El sistema redirige automáticamente según el rol

### Dashboard Administrador

- Ver todos los vehículos de la flota
- Monitorear alertas activas
- Consultar estadísticas generales
- Descargar reportes CSV
- Desactivar alertas

### Dashboard Conductor

- Ver vehículo asignado
- Consultar telemetría actual
- Revisar rutas asignadas
- Ver entregas pendientes/completadas

## API Endpoints Utilizados

### Autenticación
- `POST /login` - Inicio de sesión
- `POST /logout` - Cierre de sesión

### Vehículos (Java)
- `GET /java/vehiculos` - Listar vehículos
- `GET /java/vehiculos/:id` - Obtener vehículo
- `POST /java/vehiculos` - Crear vehículo
- `PUT /java/vehiculos/:id` - Actualizar vehículo
- `DELETE /java/vehiculos/:id` - Eliminar vehículo
- `PUT /java/vehiculos/:id/estado` - Actualizar estado

### Telemetría (Java)
- `GET /java/telemetria/vehiculo/:id` - Historial de telemetría
- `GET /java/telemetria/vehiculo/:id/ultima` - Última telemetría

### Rutas (Java)
- `GET /java/rutas` - Listar rutas
- `POST /java/rutas` - Crear ruta
- `GET /java/rutas/:id/entregas` - Entregas de una ruta
- `PUT /java/rutas/:id/asignar` - Asignar vehículo a ruta

### Alertas (Python)
- `GET /python/alertas` - Listar alertas
- `PATCH /python/alertas/desactivar` - Desactivar alertas

### Estadísticas (Python)
- `GET /python/estadisticas` - Estadísticas de la flota
- `GET /python/reporte/csv` - Descargar reporte CSV

## Seguridad

- Autenticación mediante JWT
- Token almacenado en localStorage
- Interceptor de Axios para agregar token a todas las peticiones
- Redirección automática a login en caso de sesión expirada (401)
- Rutas protegidas con verificación de rol

## Patrón MVC Implementado

- **Modelos**: Servicios en `src/services/` que manejan la lógica de datos y comunicación con API
- **Vistas**: Componentes React en `src/components/` que renderizan la interfaz
- **Controladores**: Hooks personalizados en `src/hooks/` que gestionan el estado y la lógica de negocio
