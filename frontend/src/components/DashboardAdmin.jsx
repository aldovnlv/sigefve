import { useState, useEffect } from 'react';
import vehiculoService from '../services/vehiculoService';
import alertaService from '../services/alertaService';
import estadisticaService from '../services/estadisticaService';
import Navbar from './Navbar';
import VehiculosList from './VehiculosList';
import AlertasList from './AlertasList';
import '../styles/Dashboard.css';

const DashboardAdmin = () => {
    const [vehiculos, setVehiculos] = useState([]);
    const [alertas, setAlertas] = useState([]);
    const [estadisticas, setEstadisticas] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        try {
            setLoading(true);
            const [vehiculosData, alertasData, estadisticasData] = await Promise.all([
                vehiculoService.getVehiculos(),
                alertaService.getAlertas(),
                estadisticaService.getEstadisticas()
            ]);

            setVehiculos(vehiculosData);
            setAlertas(alertasData);
            setEstadisticas(estadisticasData);
        } catch (error) {
            console.error('Error al cargar datos:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDescargarReporte = async () => {
        try {
            await estadisticaService.downloadReporteCSV();
        } catch (error) {
            console.error('Error al descargar reporte:', error);
            alert('Error al descargar el reporte');
        }
    };

    // Calcular estadísticas generales
    const vehiculosDisponibles = vehiculos.filter(v => v.estado === 'DISPONIBLE').length;
    const totalKm = estadisticas.reduce((sum, e) => sum + (e.kilometros_totales || 0), 0);
    const totalEntregas = estadisticas.reduce((sum, e) => sum + (e.entregas_completadas || 0), 0);

    if (loading) {
        return (
            <>
                <Navbar />
                <div className="loading">Cargando...</div>
            </>
        );
    }

    return (
        <>
            <Navbar />
            <div className="dashboard">
                <div className="dashboard-header-admin">
                    <div className="header-left">
                        <h1>Dashboard General</h1>
                        <p>Vista general de la flota</p>
                    </div>
                    <div className="header-right">
                        <button onClick={cargarDatos} className="btn-secondary">
                            Actualizar
                        </button>
                        <button onClick={handleDescargarReporte} className="btn-secondary">
                            Descargar Reporte CSV
                        </button>
                    </div>
                </div>

                <div className="stats-grid">
                    <div className="stat-card">
                        <h3>Total Vehículos</h3>
                        <p className="stat-value">{vehiculos.length}</p>
                    </div>
                    <div className="stat-card">
                        <h3>Disponibles</h3>
                        <p className="stat-value">{vehiculosDisponibles}</p>
                    </div>
                    <div className="stat-card">
                        <h3>Kilómetros Totales</h3>
                        <p className="stat-value">{totalKm.toFixed(2)} km</p>
                    </div>
                    <div className="stat-card">
                        <h3>Entregas Completadas</h3>
                        <p className="stat-value">{totalEntregas}</p>
                    </div>
                </div>

                <div className="dashboard-content">
                    <div className="dashboard-section">
                        <h2>Alertas Activas</h2>
                        <AlertasList alertas={alertas} onRefresh={cargarDatos} />
                    </div>

                    <div className="dashboard-section">
                        <h2>Vehículos de la Flota</h2>
                        <VehiculosList vehiculos={vehiculos} />
                    </div>

                    <div className="dashboard-section">
                        <h2>Estadísticas por Vehículo</h2>
                        <div className="table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>ID Vehículo</th>
                                        <th>Kilómetros</th>
                                        <th>Eficiencia Batería (%)</th>
                                        <th>Entregas</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {estadisticas.map((stat) => (
                                        <tr key={stat.id_vehiculo}>
                                            <td>{stat.id_vehiculo}</td>
                                            <td>{stat.kilometros_totales?.toFixed(2)} km</td>
                                            <td>{stat.eficiencia_bateria?.toFixed(2)}%</td>
                                            <td>{stat.entregas_completadas}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default DashboardAdmin;
