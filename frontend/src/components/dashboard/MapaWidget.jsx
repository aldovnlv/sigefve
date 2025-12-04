import React from 'react';

const MapaWidget = ({ focusedVehicle }) => {
  if (!focusedVehicle) {
    return (
      <div className="card">
        <div className="card-header">
          <div className="card-title">Mapa de flota</div>
        </div>
        <div className="card-body">No hay vehículos para mostrar en el mapa.</div>
      </div>
    );
  }

  const top = 40 + Math.random() * 20;
  const left = 40 + Math.random() * 20;

  let color = '#38bdf8';
  if (focusedVehicle.type && focusedVehicle.type.toLowerCase() === 'bicicleta') {
    color = '#22c55e';
  }
  if (focusedVehicle.type && focusedVehicle.type.toLowerCase() === 'motocicleta') {
    color = '#0ea5e9';
  }

  return (
    <div className="card">
      <div className="card-header">
        <div className="card-title">Mapa de flota (simulado)</div>
        <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>
          Mostrando ubicación de {focusedVehicle.name}
        </span>
      </div>
      <div className="card-body">
        <div className="simulated-map">
          <div className="simulated-map-grid" />
          <div
            className="vehicle-dot"
            style={{
              top: `${top}%`,
              left: `${left}%`,
              background: `radial-gradient(circle at 30% 30%, #f9fafb, ${color})`
            }}
          >
            {focusedVehicle.getIcon()}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MapaWidget;
