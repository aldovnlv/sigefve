import api from './api';

const authService = {
    // Iniciar sesión
    async login(username, password) {
        try {
            const response = await api.post('/login', { username, password });
            const { token, rol, user_id } = response.data;

            // Guardar token y datos de usuario en localStorage
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({
                id: user_id,
                username,
                rol
            }));

            return { token, rol, user_id };
        } catch (error) {
            throw error;
        }
    },

    // Cerrar sesión
    async logout() {
        try {
            await api.post('/logout');
        } catch (error) {
            console.error('Error al cerrar sesión:', error);
        } finally {
            // Limpiar localStorage
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        }
    },

    // Obtener usuario actual
    getCurrentUser() {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (error) {
                return null;
            }
        }
        return null;
    },

    // Obtener token
    getToken() {
        return localStorage.getItem('token');
    },

    // Verificar si está autenticado
    isAuthenticated() {
        return !!this.getToken();
    },

    // Verificar si el usuario tiene un rol específico
    hasRole(role) {
        const user = this.getCurrentUser();
        return user?.rol === role;
    }
};

export default authService;
