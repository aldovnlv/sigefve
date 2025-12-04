import React, { useState } from 'react';
import AlertasBarraNavegacionDespliegue from '../dashboard/AlertasBarraNavegacionDespliegue';
import { authController } from '../../controllers/AuthController';

const BarraNavegacion = ({ alerts }) => {
  const [open, setOpen] = useState(false);
  const payload = authController.getCurrentUserPayload();
  const unreadCount = alerts.length;

  return (
    <header className="navbar">
      <div className="navbar-brand">
        <div className="brand-logo">SF</div>
        <div>
          <div style={{ fontSize: '0.9rem', fontWeight: 600 }}>SIGEFVE Fleet</div>
          <div style={{ fontSize: '0.7rem', color: 'var(--muted)' }}>
            Monitoreo de vehículos eléctricos
          </div>
        </div>
      </div>

      <div className="navbar-actions">
        <span className="badge-pill">Dashboard</span>

        <div className="alert-bell" onClick={() => setOpen((v) => !v)}>
          <span role="img" aria-label="Alertas">
            🔔
          </span>
          {unreadCount > 0 && <span className="alert-badge">{unreadCount}</span>}
          {open && <AlertasBarraNavegacionDespliegue alerts={alerts} />}
        </div>

        <div className="user-chip">
          <div className="user-avatar">
            {payload?.name?.[0]?.toUpperCase() || 'U'}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.78rem' }}>{payload?.name || 'Usuario'}</span>
            <span style={{ fontSize: '0.68rem', color: 'var(--muted)' }}>
              {payload?.role === 'admin' ? 'Administrador' : 'Operador'}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default BarraNavegacion;
