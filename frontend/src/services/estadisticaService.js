import api from './api';

const estadisticaService = {
    // Obtener estadísticas de la flota
    async getEstadisticas() {
        const response = await api.get('/python/estadisticas');
        return response.data.estadisticas || [];
    },

    // Obtener reporte en formato CSV
    async getReporteCSV() {
        const response = await api.get('/python/reporte/csv', {
            responseType: 'text'
        });
        return response.data;
    },

    // Descargar reporte CSV
    async downloadReporteCSV() {
        const csvData = await this.getReporteCSV();
        const blob = new Blob([csvData], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `reporte_flota_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    }
};

export default estadisticaService;
