import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import BarraNavegacion from '../components/layout/BarraNavegacion';
import MapaWidget from '../components/dashboard/MapaWidget';
import { vehicleController } from '../controllers/VehicleController';
import { dashboardController } from '../controllers/DashboardController';

const VehicleDetailView = () => {
  const { id } = useParams();
  const [vehicle, setVehicle] = useState(null);
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    const load = async () => {
      const [v, dashboardData] = await Promise.all([
        vehicleController.getVehicleById(id),
        dashboardController.cargarDatosDelDashboard()
      ]);
      setVehicle(v);
      setAlerts(dashboardData.alertas);
    };
    load();
  }, [id]);

  const vehicleAlerts = alerts.filter((a) => a.vehicleId === id);

  if (!vehicle) {
    return (
      <div className="app-shell">
        <BarraNavegacion alerts={alerts} />
        <div style={{ padding: '2rem', textAlign: 'center' }}>Cargando vehículo...</div>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <BarraNavegacion alerts={alerts} />
      <div className="detail-layout">
        <div className="card">
          <div className="card-header">
            <div className="card-title">Detalle de vehículo</div>
            <Link to="/" style={{ fontSize: '0.75rem', color: 'var(--primary-soft)' }}>
              Volver al dashboard
            </Link>
          </div>
          <div className="card-body">
            <h2 style={{ marginTop: 0 }}>
              {vehicle.getIcon()} {vehicle.name}
            </h2>
            <p style={{ fontSize: '0.9rem', color: 'var(--muted)' }}>
              ID: <strong>{vehicle.id}</strong>
            </p>
            <p>
              Estado:{' '}
              <span
                className={
                  'chip ' +
                  (vehicle.status === 'DISPONIBLE' || vehicle.status === 'online'
                    ? 'chip-success'
                    : vehicle.status === 'EN_RUTA' || vehicle.status === 'en_ruta'
                      ? 'chip-warning'
                      : 'chip-muted')
                }
              >
                {vehicle.getStatusLabel()}
              </span>
            </p>
            <p>Batería: {vehicle.battery}%</p>
            <p>Ubicación actual: {vehicle.lastLocation?.label}</p>
            <p>KM totales: {vehicle.kmTotal.toLocaleString()} km</p>
            <p>Entregas hoy: {vehicle.deliveriesToday}</p>

            <h3 style={{ marginTop: '1.2rem', fontSize: '0.95rem' }}>Alertas de este vehículo</h3>
            {vehicleAlerts.length === 0 && (
              <p style={{ fontSize: '0.85rem', color: 'var(--muted)' }}>
                No hay alertas activas para esta unidad.
              </p>
            )}
            {vehicleAlerts.map((a) => (
              <div key={a.id} style={{ marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                <strong>{a.title}</strong>
                <br />
                <span style={{ color: 'var(--muted)' }}>{a.description}</span>
              </div>
            ))}
          </div>
        </div>

        <MapaWidget focusedVehicle={vehicle} />
      </div>
    </div>
  );
};

export default VehicleDetailView;
