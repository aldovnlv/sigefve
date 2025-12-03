import { useState } from 'react';
import alertaService from '../services/alertaService';
import '../styles/Alertas.css';

const AlertasList = ({ alertas, onRefresh }) => {
    const [loading, setLoading] = useState(false);

    const getPrioridadClass = (prioridad) => {
        if (prioridad === 1) return 'prioridad-alta';
        if (prioridad === 2) return 'prioridad-media';
        return 'prioridad-baja';
    };

    const handleDesactivar = async (id) => {
        try {
            setLoading(true);
            await alertaService.desactivarAlertas({ id });
            if (onRefresh) onRefresh();
        } catch (error) {
            console.error('Error al desactivar alerta:', error);
            alert('Error al desactivar la alerta');
        } finally {
            setLoading(false);
        }
    };

    const handleDesactivarTodas = async () => {
        if (!window.confirm('¿Deseas desactivar todas las alertas?')) return;

        try {
            setLoading(true);
            await alertaService.desactivarAlertas({});
            if (onRefresh) onRefresh();
        } catch (error) {
            console.error('Error al desactivar alertas:', error);
            alert('Error al desactivar las alertas');
        } finally {
            setLoading(false);
        }
    };

    if (!alertas || alertas.length === 0) {
        return <p className="empty-message">No hay alertas activas</p>;
    }

    // Ordenar por prioridad (1 = más alta)
    const alertasOrdenadas = [...alertas].sort((a, b) => a.prioridad - b.prioridad);

    return (
        <div className="alertas-container">
            <div className="alertas-header">
                <h3>Total: {alertas.length} alertas</h3>
                <button
                    onClick={handleDesactivarTodas}
                    className="btn-danger"
                    disabled={loading}
                >
                    Desactivar Todas
                </button>
            </div>

            <div className="alertas-list">
                {alertasOrdenadas.map((alerta) => (
                    <div key={alerta.id} className={`alerta-card ${getPrioridadClass(alerta.prioridad)}`}>
                        <div className="alerta-header">
                            <span className="alerta-tipo">{alerta.tipo}</span>
                            <span className="alerta-prioridad">Prioridad {alerta.prioridad}</span>
                        </div>

                        <p className="alerta-mensaje">{alerta.mensaje}</p>
                        <p className="alerta-descripcion">{alerta.descripcion}</p>

                        <div className="alerta-footer">
                            <span className="alerta-fecha">{alerta.fecha_generacion}</span>
                            <button
                                onClick={() => handleDesactivar(alerta.id)}
                                className="btn-small"
                                disabled={loading}
                            >
                                Desactivar
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default AlertasList;
