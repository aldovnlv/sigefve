import React from 'react';

const TarjetaEstadisticas = ({ stats }) => {
  if (!stats) return null;

  return (
    <div className="card">
      <div className="card-header">
        <div className="card-title">Estadísticas de la flota</div>
        <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>
          Datos agregados
        </span>
      </div>
      <div className="card-body">
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-label">KM totales</div>
            <div className="stat-value">{stats.totalKm.toLocaleString()} km</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Entregas hoy</div>
            <div className="stat-value">{stats.deliveriesToday}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Vehículos disponibles</div>
            <div className="stat-value">{stats.availableVehicles}</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TarjetaEstadisticas;
