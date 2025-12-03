import api from './api';

const rutaService = {
    // Obtener todas las rutas
    async getRutas() {
        const response = await api.get('/java/rutas');
        return response.data;
    },

    // Crear nueva ruta
    async createRuta(data) {
        const response = await api.post('/java/rutas', data);
        return response.data;
    },

    // Obtener entregas de una ruta
    async getEntregas(rutaId) {
        const response = await api.get(`/java/rutas/${rutaId}/entregas`);
        return response.data;
    },

    // Agregar entrega a una ruta
    async addEntrega(rutaId, entregaId) {
        const response = await api.post(`/java/rutas/${rutaId}/entregas`, { id: entregaId });
        return response.data;
    },

    // Asignar vehículo a ruta
    async asignarVehiculo(rutaId, vehiculoId) {
        const response = await api.put(`/java/rutas/${rutaId}/asignar`, { vehiculoId });
        return response.data;
    },

    // Completar ruta
    async completarRuta(rutaId) {
        const response = await api.put(`/java/rutas/${rutaId}/completar`, {});
        return response.data;
    }
};

export default rutaService;
