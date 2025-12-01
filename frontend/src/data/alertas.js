// src/data/alertas.js
// Servicio para consumir los endpoints de alertas del backend
// TODO: Cambia API_URL cuando conectes tu API Java/Python
const API_URL = "http://localhost:8080";

// Obtener todas las alertas activas
export async function getAlertas() {
  try {
    const res = await fetch(`${API_URL}/alertas`);
    if (!res.ok) throw new Error("Error al obtener alertas");
    return await res.json();
  } catch (err) {
    console.error("getAlertas error:", err);
    return [];
  }
}

// Desactivar alerta
export async function desactivarAlertaPorId(idAlerta) {
  try {
    const res = await fetch(`${API_URL}/alertas/desactivar/id/${idAlerta}`);
    if (!res.ok) throw new Error("Error al desactivar la alerta por ID");
    return await res.json();
  } catch (err) {
    console.error("desactivarAlertaPorId error:", err);
    return null;
  }
}
