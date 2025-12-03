import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import vehiculoService from '../services/vehiculoService';
import telemetriaService from '../services/telemetriaService';
import Navbar from './Navbar';
import '../styles/VehiculoDetalle.css';

const VehiculoDetalle = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [vehiculo, setVehiculo] = useState(null);
    const [telemetria, setTelemetria] = useState([]);
    const [ultimaTelemetria, setUltimaTelemetria] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        cargarDatos();
    }, [id]);

    const cargarDatos = async () => {
        try {
            setLoading(true);
            const [vehiculoData, telemetriaData] = await Promise.all([
                vehiculoService.getVehiculo(id),
                telemetriaService.getTelemetriaVehiculo(id)
            ]);

            setVehiculo(vehiculoData);
            setTelemetria(telemetriaData);

            if (telemetriaData.length > 0) {
                setUltimaTelemetria(telemetriaData[0]);
            }
        } catch (err) {
            setError('Error al cargar datos del vehículo');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <>
                <Navbar />
                <div className="loading">Cargando...</div>
            </>
        );
    }

    if (error || !vehiculo) {
        return (
            <>
                <Navbar />
                <div className="error-container">
                    <p>{error || 'Vehículo no encontrado'}</p>
                    <button onClick={() => navigate('/admin')} className="btn-primary">
                        Volver al Dashboard
                    </button>
                </div>
            </>
        );
    }

    const getBateriaClass = (nivel) => {
        if (nivel < 20) return 'bateria-baja';
        if (nivel < 50) return 'bateria-media';
        return 'bateria-alta';
    };

    return (
        <>
            <Navbar />
            <div className="vehiculo-detalle-container">
                <div className="detalle-header">
                    <button onClick={() => navigate('/admin')} className="btn-back">
                        ← Volver
                    </button>
                    <h1>Detalle del Vehículo</h1>
                </div>

                <div className="detalle-grid">
                    {/* Información General */}
                    <div className="detalle-card">
                        <h2>Información General</h2>
                        <div className="info-grid">
                            <div className="info-item">
                                <span className="label">Placa:</span>
                                <span className="value">{vehiculo.placa}</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Tipo:</span>
                                <span className="value">{vehiculo.tipo}</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Modelo:</span>
                                <span className="value">{vehiculo.modelo}</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Año:</span>
                                <span className="value">{vehiculo.anio}</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Estado:</span>
                                <span className={`badge badge-${vehiculo.estado.toLowerCase()}`}>
                                    {vehiculo.estado}
                                </span>
                            </div>
                            <div className="info-item">
                                <span className="label">Kilometraje Total:</span>
                                <span className="value">{vehiculo.kilometrajeTotal?.toFixed(2)} km</span>
                            </div>
                        </div>
                    </div>

                    {/* Especificaciones */}
                    <div className="detalle-card">
                        <h2>Especificaciones</h2>
                        <div className="info-grid">
                            <div className="info-item">
                                <span className="label">Capacidad Batería:</span>
                                <span className="value">{vehiculo.capacidadBateria} kWh</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Autonomía Máxima:</span>
                                <span className="value">{vehiculo.autonomiaMaxima} km</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Capacidad de Carga:</span>
                                <span className="value">{vehiculo.capacidadCarga} kg</span>
                            </div>
                            <div className="info-item">
                                <span className="label">N° Asientos:</span>
                                <span className="value">{vehiculo.numeroAsientos}</span>
                            </div>
                            <div className="info-item">
                                <span className="label">Consumo Promedio:</span>
                                <span className="value">{vehiculo.consumoPromedio} kWh/km</span>
                            </div>
                        </div>
                    </div>

                    {/* Telemetría Actual */}
                    {ultimaTelemetria && (
                        <div className="detalle-card">
                            <h2>Telemetría Actual</h2>
                            <div className="telemetria-grid">
                                <div className="telemetria-card">
                                    <span className="tele-label">Batería</span>
                                    <div className={`bateria-circle ${getBateriaClass(ultimaTelemetria.nivelBateria)}`}>
                                        <span className="tele-value">{ultimaTelemetria.nivelBateria}%</span>
                                    </div>
                                </div>
                                <div className="telemetria-card">
                                    <span className="tele-label">Velocidad</span>
                                    <span className="tele-value">{ultimaTelemetria.velocidadActual} km/h</span>
                                </div>
                                <div className="telemetria-card">
                                    <span className="tele-label">Temperatura</span>
                                    <span className="tele-value">{ultimaTelemetria.temperaturaMotor}°C</span>
                                </div>
                                <div className="telemetria-card">
                                    <span className="tele-label">Ubicación</span>
                                    <span className="tele-value-small">
                                        {ultimaTelemetria.latitud?.toFixed(4)}, {ultimaTelemetria.longitud?.toFixed(4)}
                                    </span>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Historial de Telemetría */}
                    <div className="detalle-card full-width">
                        <h2>Historial de Telemetría (Últimas 10 lecturas)</h2>
                        <div className="table-scroll">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Fecha/Hora</th>
                                        <th>Batería (%)</th>
                                        <th>Velocidad (km/h)</th>
                                        <th>Temperatura (°C)</th>
                                        <th>Ubicación</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {telemetria.slice(0, 10).map((t, index) => (
                                        <tr key={t.id || index}>
                                            <td>{t.timestamp}</td>
                                            <td>
                                                <span className={getBateriaClass(t.nivelBateria)}>
                                                    {t.nivelBateria}%
                                                </span>
                                            </td>
                                            <td>{t.velocidadActual}</td>
                                            <td>{t.temperaturaMotor}</td>
                                            <td className="ubicacion-cell">
                                                {t.latitud?.toFixed(4)}, {t.longitud?.toFixed(4)}
                                            </td>
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

export default VehiculoDetalle;
