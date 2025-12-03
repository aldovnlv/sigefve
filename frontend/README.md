# SIGEFVE Fleet Dashboard MVC (mejorado)

Proyecto en React con patrón MVC en el front, JWT y consumo de endpoints:

- POST /login
- GET  /vehiculos
- GET  /vehiculos/:id
- GET  /alertas
- GET  /estadisticas

Incluye:
- Vehículos, bicicletas eléctricas y motocicletas eléctricas.
- Polimorfismo con clases ElectricBike y ElectricMotorcycle.
- Iconos por tipo de vehículo en la tabla.
- Preparado para despliegue en Nginx.

La URL base del backend se configura en:

  src/services/apiClient.js

Cambia:

  const API_BASE_URL = 'http://localhost:8080';

por la URL de tu API en producción, por ejemplo:

  const API_BASE_URL = 'https://api.midominio.com';
