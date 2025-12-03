import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import rutaService from '../services/rutaService';
import vehiculoService from '../services/vehiculoService';
import telemetriaService from '../services/telemetriaService';
import '../styles/Dashboard.css';

const DashboardConductor = () => {
    const [rutas, setRutas] = useState([]);
    const [vehiculo, setVehiculo] = useState(null);
    const [telemetria, setTelemetria] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const user = authService.getCurrentUser();

    useEffect(() => {
        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        try {
            setLoading(true);
            // Obtener rutas
            const rutasData = await rutaService.getRutas();
            setRutas(rutasData);

            // Para un conductor, mostrar el primer vehículo como ejemplo
            const vehiculos = await vehiculoService.getVehiculos();
            if (vehiculos.length > 0) {
                const primerVehiculo = vehiculos[0];
                setVehiculo(primerVehiculo);

                // Obtener telemetría del vehículo
                try {
                    const telemetriaData = await telemetriaService.getUltimaTelemetria(primerVehiculo.id);
                    setTelemetria(telemetriaData);
                } catch (error) {
                    console.error('Error al cargar telemetría:', error);
                }
            }
        } catch (error) {
            console.error('Error al cargar datos:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        await authService.logout();
        navigate('/login');
    };

    if (loading) {
        return <div className="loading">Cargando...</div>;
    }

    return (
        <div className="dashboard">
            <header className="dashboard-header">
                <div className="header-left">
                    <h1>Dashboard Conductor</h1>
                    <p>Bienvenido, {user?.username}</p>
                </div>
                <div className="header-right">
                    <button onClick={cargarDatos} className="btn-secondary">
                        Actualizar
                    </button>
                    <button onClick={handleLogout} className="btn-logout">
                        Cerrar Sesión
                    </button>
                </div>
            </header>

            <div className="dashboard-content">
                {vehiculo && (
                    <div className="dashboard-section">
                        <h2>Mi Vehículo</h2>
                        <div className="vehiculo-card large">
                            <div className="vehiculo-header">
                                <h3>{vehiculo.placa}</h3>
                                <span className={`badge badge-${vehiculo.estado.toLowerCase()}`}>
                                    {vehiculo.estado}
                                </span>
                            </div>
                            <div className="vehiculo-details">
                                <p><strong>Modelo:</strong> {vehiculo.modelo}</p>
                                <p><strong>Tipo:</strong> {vehiculo.tipo}</p>
                                <p><strong>Año:</strong> {vehiculo.anio}</p>
                            </div>

                            {telemetria && (
                                <div className="telemetria-info">
                                    <h4>Telemetría Actual</h4>
                                    <div className="telemetria-grid">
                                        <div className="telemetria-item">
                                            <span className="label">Batería:</span>
                                            <span className="value">{telemetria.nivelBateria}%</span>
                                        </div>
                                        <div className="telemetria-item">
                                            <span className="label">Velocidad:</span>
                                            <span className="value">{telemetria.velocidadActual} km/h</span>
                                        </div>
                                        <div className="telemetria-item">
                                            <span className="label">Temperatura:</span>
                                            <span className="value">{telemetria.temperaturaMotor}°C</span>
                                        </div>
                                        <div className="telemetria-item">
                                            <span className="label">Kilometraje:</span>
                                            <span className="value">{telemetria.kilometrajeActual} km</span>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                <div className="dashboard-section">
                    <h2>Rutas Asignadas</h2>
                    <div className="rutas-list">
                        {rutas.length === 0 ? (
                            <p className="empty-message">No hay rutas asignadas</p>
                        ) : (
                            rutas.map((ruta) => (
                                <div key={ruta.id} className="ruta-card">
                                    <div className="ruta-header">
                                        <h3>{ruta.nombre}</h3>
                                        {ruta.completada ? (
                                            <span className="badge badge-success">Completada</span>
                                        ) : (
                                            <span className="badge badge-pending">Pendiente</span>
                                        )}
                                    </div>
                                    <div className="ruta-details">
                                        <p><strong>Distancia:</strong> {ruta.distanciaTotal} km</p>
                                        <p><strong>Entregas:</strong> {ruta.numeroEntregas}</p>
                                        <p><strong>Fecha inicio:</strong> {ruta.fechaInicio}</p>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DashboardConductor;
