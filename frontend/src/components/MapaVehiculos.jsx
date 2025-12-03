import { useState, useEffect } from 'react';
import vehiculoService from '../services/vehiculoService';
import telemetriaService from '../services/telemetriaService';
import Navbar from './Navbar';
import '../styles/MapaVehiculos.css';

const MapaVehiculos = () => {
    const [vehiculos, setVehiculos] = useState([]);
    const [posiciones, setPosiciones] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        cargarDatos();
        const interval = setInterval(cargarDatos, 15000); // Actualizar cada 15s
        return () => clearInterval(interval);
    }, []);

    const cargarDatos = async () => {
        try {
            const vehiculosData = await vehiculoService.getVehiculos();
            setVehiculos(vehiculosData);

            // Obtener última posición de cada vehículo
            const posicionesTemp = {};
            for (const vehiculo of vehiculosData) {
                try {
                    const telemetria = await telemetriaService.getUltimaTelemetria(vehiculo.id);
                    posicionesTemp[vehiculo.id] = {
                        lat: telemetria.latitud,
                        lng: telemetria.longitud,
                        bateria: telemetria.nivelBateria,
                        velocidad: telemetria.velocidadActual
                    };
                } catch (error) {
                    console.error(`Error al obtener telemetría del vehículo ${vehiculo.id}`);
                }
            }
            setPosiciones(posicionesTemp);
            setLoading(false);
        } catch (error) {
            console.error('Error al cargar datos:', error);
            setLoading(false);
        }
    };

    // Función para convertir coordenadas reales a posición en el mapa simulado
    const coordToPixel = (lat, lng) => {
        // Rango aproximado de coordenadas (México central)
        const minLat = 18.5, maxLat = 21.0;
        const minLng = -100.0, maxLng = -96.0;

        const x = ((lng - minLng) / (maxLng - minLng)) * 100;
        const y = ((maxLat - lat) / (maxLat - minLat)) * 100;

        return { x: Math.max(5, Math.min(95, x)), y: Math.max(5, Math.min(95, y)) };
    };

    const getEstadoColor = (estado) => {
        switch (estado) {
            case 'DISPONIBLE': return '#10b981';
            case 'EN_RUTA': return '#3b82f6';
            case 'MANTENIMIENTO': return '#f59e0b';
            case 'CARGANDO': return '#8b5cf6';
            default: return '#64748b';
        }
    };

    if (loading) {
        return (
            <>
                <Navbar />
                <div className="loading">Cargando mapa...</div>
            </>
        );
    }

    return (
        <>
            <Navbar />
            <div className="mapa-container">
                <div className="mapa-header">
                    <h1>Mapa de Vehículos</h1>
                    <p>Actualización automática cada 15 segundos</p>
                </div>

                <div className="mapa-content">
                    <div className="mapa-wrapper">
                        <div className="mapa-simulado">
                            <div className="mapa-grid"></div>
                            {vehiculos.map((vehiculo) => {
                                const pos = posiciones[vehiculo.id];
                                if (!pos) return null;

                                const pixel = coordToPixel(pos.lat, pos.lng);

                                return (
                                    <div
                                        key={vehiculo.id}
                                        className="vehiculo-marker"
                                        style={{
                                            left: `${pixel.x}%`,
                                            top: `${pixel.y}%`,
                                            backgroundColor: getEstadoColor(vehiculo.estado)
                                        }}
                                        title={`${vehiculo.placa} - ${vehiculo.estado}`}
                                    >
                                        <div className="marker-icon">🚗</div>
                                        <div className="marker-label">{vehiculo.placa}</div>
                                        <div className="marker-tooltip">
                                            <strong>{vehiculo.placa}</strong><br />
                                            Estado: {vehiculo.estado}<br />
                                            Batería: {pos.bateria}%<br />
                                            Velocidad: {pos.velocidad} km/h<br />
                                            Ubicación: {pos.lat.toFixed(4)}, {pos.lng.toFixed(4)}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>

                        <div className="mapa-leyenda">
                            <h3>Leyenda</h3>
                            <div className="leyenda-item">
                                <span className="leyenda-color" style={{ backgroundColor: '#10b981' }}></span>
                                <span>Disponible</span>
                            </div>
                            <div className="leyenda-item">
                                <span className="leyenda-color" style={{ backgroundColor: '#3b82f6' }}></span>
                                <span>En Ruta</span>
                            </div>
                            <div className="leyenda-item">
                                <span className="leyenda-color" style={{ backgroundColor: '#f59e0b' }}></span>
                                <span>Mantenimiento</span>
                            </div>
                            <div className="leyenda-item">
                                <span className="leyenda-color" style={{ backgroundColor: '#8b5cf6' }}></span>
                                <span>Cargando</span>
                            </div>
                        </div>
                    </div>

                    <div className="vehiculos-list-lateral">
                        <h3>Vehículos ({vehiculos.length})</h3>
                        <div className="vehiculos-scroll">
                            {vehiculos.map((vehiculo) => {
                                const pos = posiciones[vehiculo.id];
                                return (
                                    <div key={vehiculo.id} className="vehiculo-item-mapa">
                                        <div className="vehiculo-icon" style={{ backgroundColor: getEstadoColor(vehiculo.estado) }}>
                                            🚗
                                        </div>
                                        <div className="vehiculo-info-mapa">
                                            <strong>{vehiculo.placa}</strong>
                                            <span className="vehiculo-tipo">{vehiculo.tipo}</span>
                                            <span className="vehiculo-estado">{vehiculo.estado}</span>
                                            {pos && (
                                                <span className="vehiculo-bateria">Batería: {pos.bateria}%</span>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default MapaVehiculos;
