// Servicio para consumir los endpoints relacionados con vehículos del backend

// URL base de la API (modifica si usas otro host o puerto)
const API_URL = "http://localhost:8080";

// ------------------------------------------------------------
// Obtener la lista de vehículos
// GET /vehiculos
// ------------------------------------------------------------
export async function getVehiculos() {
  try {
    const response = await fetch(`${API_URL}/vehiculos`);

    if (!response.ok) throw new Error("Error al obtener vehículos");

    return await response.json();
  } catch (error) {
    console.error(" Error en getVehiculos:", error);
    return [];
  }
}

// ------------------------------------------------------------
// Obtener detalle de un vehículo por ID
// GET /vehiculos/:id
// ------------------------------------------------------------
export async function getVehiculoById(idVehiculo) {
  try {
    const response = await fetch(`${API_URL}/vehiculos/${idVehiculo}`);

    if (!response.ok) throw new Error("Error al obtener vehículo por ID");

    return await response.json();
  } catch (error) {
    console.error(" Error en getVehiculoById:", error);
    return null;
  }
}

// ------------------------------------------------------------
// Obtener ubicaciones de vehículos (para el mapa)
// GET /vehiculos/ubicaciones
// ------------------------------------------------------------
export async function getUbicacionesVehiculos() {
  try {
    const response = await fetch(`${API_URL}/vehiculos/ubicaciones`);

    if (!response.ok) throw new Error("Error al obtener ubicaciones de vehículos");

    return await response.json();
  } catch (error) {
    console.error(" Error en getUbicacionesVehiculos:", error);
    return [];
  }
}
