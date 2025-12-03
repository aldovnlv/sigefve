import '../styles/Vehiculos.css';

const VehiculosList = ({ vehiculos }) => {
    const getEstadoClass = (estado) => {
        switch (estado) {
            case 'DISPONIBLE':
                return 'estado-disponible';
            case 'EN_RUTA':
                return 'estado-en-ruta';
            case 'MANTENIMIENTO':
                return 'estado-mantenimiento';
            case 'CARGANDO':
                return 'estado-cargando';
            default:
                return '';
        }
    };

    const getBateriaClass = (nivel) => {
        if (nivel < 20) return 'bateria-baja';
        if (nivel < 50) return 'bateria-media';
        return 'bateria-alta';
    };

    if (!vehiculos || vehiculos.length === 0) {
        return <p className="empty-message">No hay vehículos registrados</p>;
    }

    return (
        <div className="vehiculos-grid">
            {vehiculos.map((vehiculo) => (
                <div key={vehiculo.id} className="vehiculo-card">
                    <div className="vehiculo-header">
                        <h3>{vehiculo.placa}</h3>
                        <span className={`estado-badge ${getEstadoClass(vehiculo.estado)}`}>
                            {vehiculo.estado}
                        </span>
                    </div>

                    <div className="vehiculo-info">
                        <p className="vehiculo-tipo">{vehiculo.tipo}</p>
                        <p className="vehiculo-modelo">{vehiculo.modelo}</p>
                    </div>

                    <div className="vehiculo-stats">
                        <div className="stat-item">
                            <span className="stat-label">Batería</span>
                            <div className={`bateria-bar ${getBateriaClass(vehiculo.capacidadBateria)}`}>
                                <div
                                    className="bateria-fill"
                                    style={{ width: `${vehiculo.capacidadBateria}%` }}
                                ></div>
                            </div>
                            <span className="stat-value">{vehiculo.capacidadBateria}%</span>
                        </div>

                        <div className="stat-item">
                            <span className="stat-label">Kilometraje</span>
                            <span className="stat-value">{vehiculo.kilometrajeTotal?.toFixed(2)} km</span>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default VehiculosList;
