// validateToken.js
// Valida el token intentando llamar a un endpoint REAL protegido.

const TEST_URL = "/users"; 
// Usa un endpoint que SÍ esté protegido y requiera token

export async function validateToken() {
  const token = localStorage.getItem("token");

  if (!token) return false;

  try {
    const response = await fetch(TEST_URL, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`, // API usa JWT
      },
    });

    // 200 → Token válido
    if (response.status === 200) return true;

    // 401 o 403 → Token expiró o inválido
    if (response.status === 401 || response.status === 403) return false;

    return false;

  } catch (error) {
    console.error("Error al validar token:", error);
    return false;
  }
}
