import { authService } from '../services/AuthService';

class AuthController {
  async login(username, password) {
    return authService.login(username, password);
  }

  logout() {
    authService.logout();
  }

  isAuthenticated() {
    return authService.isAuthenticated();
  }

  getCurrentUserPayload() {
    return authService.decodeToken();
  }
}

export const authController = new AuthController();
