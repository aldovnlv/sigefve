import api from './api';

const telemetriaService = {
    // Obtener telemetría de un vehículo
    async getTelemetriaVehiculo(id) {
        const response = await api.get(`/java/telemetria/vehiculo/${id}`);
        return response.data;
    },

    // Obtener última telemetría de un vehículo
    async getUltimaTelemetria(id) {
        const response = await api.get(`/java/telemetria/vehiculo/${id}/ultima`);
        return response.data;
    },

    // Registrar nueva telemetría
    async registrarTelemetria(data) {
        const response = await api.post('/java/telemetria', data);
        return response.data;
    }
};

export default telemetriaService;
