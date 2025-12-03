import { authService } from './AuthService';

// const API_BASE_URL = 'http://localhost:8080';
const API_BASE_URL = 'https://apisigefve.xipatlani.tk';

export async function apiGet(path) {
  const token = authService.getToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  });

  if (!response.ok) {
    throw new Error(`Error en la petición GET ${path}`);
  }

  return response.json();
}

export async function apiPost(path, body) {
  const token = authService.getToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(`Error en la petición POST ${path}`);
  }

  return response.json();
}
