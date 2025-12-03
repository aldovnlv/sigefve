import { AdminUser } from '../models/User';
import { apiPost } from './apiClient';

class AuthService {
  constructor() {
    this.storageKey = 'token';
  }

  async login(username, password) {
    const res = await apiPost('/login', { username, password });

    const token = res.token;
    const user = res.user || {};

    if (!token) {
      throw new Error('La API de login no devolvió un token válido.');
    }

    localStorage.setItem(this.storageKey, token);

    const domainUser = new AdminUser(user.id || '1', user.nombre || user.name || 'Administrador');
    return domainUser;
  }

  logout() {
    localStorage.removeItem(this.storageKey);
  }

  getToken() {
    return localStorage.getItem(this.storageKey);
  }

  decodeToken() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;
      const payloadB64 = parts[1];
      const json = atob(payloadB64);
      return JSON.parse(json);
    } catch (err) {
      console.error('Error al decodificar token', err);
      return null;
    }
  }

  isAuthenticated() {
    const payload = this.decodeToken();
    if (!payload) return false;

    const now = Math.floor(Date.now() / 1000);
    if (payload.exp && payload.exp <= now) {
      return false;
    }
    return true;
  }
}

export const authService = new AuthService();
