import api from './api';

const alertaService = {
    // Obtener todas las alertas
    async getAlertas() {
        const response = await api.get('/python/alertas');
        return response.data.alertas || [];
    },

    // Desactivar alertas
    async desactivarAlertas(params = {}) {
        const queryParams = new URLSearchParams();
        if (params.id) queryParams.append('id', params.id);
        if (params.id_vehiculo) queryParams.append('id_vehiculo', params.id_vehiculo);

        const url = `/python/alertas/desactivar${queryParams.toString() ? '?' + queryParams.toString() : ''}`;
        const response = await api.patch(url);
        return response.data;
    }
};

export default alertaService;
