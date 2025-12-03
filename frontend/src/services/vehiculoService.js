import api from './api';

const vehiculoService = {
    // Obtener todos los vehículos
    async getVehiculos() {
        const response = await api.get('/java/vehiculos');
        return response.data;
    },

    // Obtener un vehículo por ID
    async getVehiculo(id) {
        const response = await api.get(`/java/vehiculos/${id}`);
        return response.data;
    },

    // Crear nuevo vehículo
    async createVehiculo(data) {
        const response = await api.post('/java/vehiculos', data);
        return response.data;
    },

    // Actualizar vehículo
    async updateVehiculo(id, data) {
        const response = await api.put(`/java/vehiculos/${id}`, data);
        return response.data;
    },

    // Eliminar vehículo
    async deleteVehiculo(id) {
        const response = await api.delete(`/java/vehiculos/${id}`);
        return response.data;
    },

    // Actualizar estado del vehículo
    async updateEstado(id, estado) {
        const response = await api.put(`/java/vehiculos/${id}/estado`, { estado });
        return response.data;
    }
};

export default vehiculoService;
