import React from 'react';

const priorityClass = (priority) => {
  if (priority === 'alta' || priority === 'ALTA') return 'alert-item-high';
  if (priority === 'media' || priority === 'MEDIA') return 'alert-item-medium';
  return 'alert-item-low';
};

const AlertsNavbarDropdown = ({ alerts }) => {
  return (
    <div className="alert-dropdown">
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '0.25rem'
        }}
      >
        <span style={{ fontSize: '0.8rem', fontWeight: 600 }}>Alertas activas</span>
        <span style={{ fontSize: '0.7rem', color: 'var(--muted)' }}>
          {alerts.length} notificaciones
        </span>
      </div>
      {alerts.length === 0 && (
        <div style={{ fontSize: '0.78rem', color: 'var(--muted)', padding: '0.5rem' }}>
          No hay alertas activas.
        </div>
      )}
      {alerts.map((alert) => (
        <div
          key={alert.id}
          className={`alert-item ${priorityClass(alert.priority)}`}
        >
          <div className="alert-title">{alert.title}</div>
          <div className="alert-meta">
            {alert.description}
            <br />
            <span>
              Vehículo: <strong>{alert.vehicleId}</strong> · Prioridad: {alert.priority}
            </span>
          </div>
        </div>
      ))}
    </div>
  );
};

export default AlertsNavbarDropdown;
