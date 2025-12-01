const API_URL = "http://localhost:8080"; // tu Gateway

export async function apiFetch(endpoint, options = {}) {
  try {
    const res = await fetch(`${API_URL}${endpoint}`, {
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      },
      method: options.method || "GET",
      body: options.body ? JSON.stringify(options.body) : undefined,
    });

    // Si la API responde error (400, 401, 500…)
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      throw new Error(errorData.error || "Error en la petición");
    }

    return await res.json();
  } catch (err) {
    console.error(" Error en apiFetch:", err);
    throw err;
  }
}
