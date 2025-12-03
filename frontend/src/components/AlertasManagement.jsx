import { useState, useEffect } from 'react';
import alertaService from '../services/alertaService';
import Navbar from './Navbar';
import '../styles/AlertasManagement.css';

const AlertasManagement = () => {
    const [alertas, setAlertas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('todas'); // todas, activas, urgentes
    const [success, setSuccess] = useState('');

    useEffect(() => {
        cargarAlertas();
        // Auto-reload cada 10 segundos
        const interval = setInterval(cargarAlertas, 10000);
        return () => clearInterval(interval);
    }, []);

    const cargarAlertas = async () => {
        try {
            const alertasData = await alertaService.getAlertas();
            setAlertas(alertasData);
            setLoading(false);
        } catch (error) {
            console.error('Error al cargar alertas:', error);
            setLoading(false);
        }
    };

    const handleDesactivar = async (id) => {
        try {
            await alertaService.desactivarAlertas({ id });
            setSuccess(`Alerta ${id} desactivada`);
            setTimeout(() => setSuccess(''), 2000);
            cargarAlertas();
        } catch (error) {
            console.error('Error al desactivar alerta:', error);
        }
    };

    const handleDesactivarTodas = async () => {
        if (!window.confirm('¿Deseas desactivar todas las alertas activas?')) return;

        try {
            await alertaService.desactivarAlertas({});
            setSuccess('Todas las alertas desactivadas');
            setTimeout(() => setSuccess(''), 2000);
            cargarAlertas();
        } catch (error) {
            console.error('Error al desactivar alertas:', error);
        }
    };

    const handleDesactivarVehiculo = async (idVehiculo) => {
        if (!window.confirm(`¿Deseas desactivar todas las alertas del vehículo ${idVehiculo}?`)) return;

        try {
            await alertaService.desactivarAlertas({ id_vehiculo: idVehiculo });
            setSuccess(`Alertas del vehículo ${idVehiculo} desactivadas`);
            setTimeout(() => setSuccess(''), 2000);
            cargarAlertas();
        } catch (error) {
            console.error('Error al desactivar alertas del vehículo:', error);
        }
    };

    // Filtrar alertas
    const alertasFiltradas = alertas.filter((a) => {
        if (filter === 'activas') return a.estado;
        if (filter === 'urgentes') return a.estado && a.prioridad === 1;
        return true; // todas
    }).sort((a, b) => a.prioridad - b.prioridad); // Ordenar por prioridad

    const getPrioridadClass = (prioridad) => {
        if (prioridad === 1) return 'prioridad-alta';
        if (prioridad === 2) return 'prioridad-media';
        return 'prioridad-baja';
    };

    const getPrioridadText = (prioridad) => {
        if (prioridad === 1) return 'URGENTE';
        if (prioridad === 2) return 'MEDIA';
        return 'BAJA';
    };

    if (loading) {
        return (
            <>
                <Navbar />
                <div className="loading">Cargando alertas...</div>
            </>
        );
    }

    const alertasActivas = alertas.filter(a => a.estado).length;
    const alertasUrgentes = alertas.filter(a => a.estado && a.prioridad === 1).length;

    return (
        <>
            <Navbar />
            <div className="alertas-management-container">
                <div className="alertas-header">
                    <div>
                        <h1>Gestión de Alertas</h1>
                        <p>Actualización automática cada 10 segundos</p>
                    </div>
                    <button onClick={handleDesactivarTodas} className="btn-danger">
                        Desactivar Todas las Alertas
                    </button>
                </div>

                {success && <div className="alert alert-success">{success}</div>}

                <div className="stats-alertas">
                    <div className="stat-card-alerta">
                        <span className="stat-icon">📊</span>
                        <div>
                            <p className="stat-label">Total Alertas</p>
                            <p className="stat-number">{alertas.length}</p>
                        </div>
                    </div>
                    <div className="stat-card-alerta">
                        <span className="stat-icon">🔔</span>
                        <div>
                            <p className="stat-label">Alertas Activas</p>
                            <p className="stat-number">{alertasActivas}</p>
                        </div>
                    </div>
                    <div className="stat-card-alerta urgente">
                        <span className="stat-icon">🚨</span>
                        <div>
                            <p className="stat-label">Alertas Urgentes</p>
                            <p className="stat-number">{alertasUrgentes}</p>
                        </div>
                    </div>
                </div>

                <div className="filter-bar">
                    <button
                        className={`filter-btn ${filter === 'todas' ? 'active' : ''}`}
                        onClick={() => setFilter('todas')}
                    >
                        Todas ({alertas.length})
                    </button>
                    <button
                        className={`filter-btn ${filter === 'activas' ? 'active' : ''}`}
                        onClick={() => setFilter('activas')}
                    >
                        Activas ({alertasActivas})
                    </button>
                    <button
                        className={`filter-btn ${filter === 'urgentes' ? 'active' : ''}`}
                        onClick={() => setFilter('urgentes')}
                    >
                        Urgentes ({alertasUrgentes})
                    </button>
                </div>

                <div className="alertas-grid">
                    {alertasFiltradas.length === 0 ? (
                        <p className="empty-message">No hay alertas para mostrar</p>
                    ) : (
                        alertasFiltradas.map((alerta) => (
                            <div key={alerta.id} className={`alerta-card-full ${getPrioridadClass(alerta.prioridad)}`}>
                                <div className="alerta-card-header">
                                    <div className="alerta-meta">
                                        <span className="alerta-id">#{alerta.id}</span>
                                        <span className={`alerta-priority-badge ${getPrioridadClass(alerta.prioridad)}`}>
                                            {getPrioridadText(alerta.prioridad)}
                                        </span>
                                        <span className="alerta-tipo-badge">{alerta.tipo}</span>
                                    </div>
                                    <span className={`estado-badge ${alerta.estado ? 'activa' : 'inactiva'}`}>
                                        {alerta.estado ? 'Activa' : 'Desactivada'}
                                    </span>
                                </div>

                                <div className="alerta-body">
                                    <p className="alerta-mensaje-full">{alerta.mensaje}</p>
                                    <p className="alerta-descripcion-full">{alerta.descripcion}</p>
                                </div>

                                <div className="alerta-footer-full">
                                    <div className="alerta-info-footer">
                                        <span>🚗 Vehículo: {alerta.id_vehiculo}</span>
                                        <span>📅 {alerta.fecha_generacion}</span>
                                    </div>
                                    {alerta.estado && (
                                        <div className="alerta-actions">
                                            <button
                                                onClick={() => handleDesactivarVehiculo(alerta.id_vehiculo)}
                                                className="btn-secondary-small"
                                            >
                                                Desactivar Vehículo
                                            </button>
                                            <button
                                                onClick={() => handleDesactivar(alerta.id)}
                                                className="btn-danger-small"
                                            >
                                                Desactivar
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </>
    );
};

export default AlertasManagement;
