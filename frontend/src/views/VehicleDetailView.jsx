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
  const [telemetriaVehiculo, setTelemetriaVehiculo] = useState(null);

  useEffect(() => {
    const load = async () => {
      const [v, dashboardData, telemetria] = await Promise.all([
        vehicleController.getVehicleById(id),
        dashboardController.cargarDatosDelDashboard(),
        vehicleController.getUltimaTelemetria(id)
      ]);
      setVehicle(v);
      setAlerts(dashboardData.alertas);
      setTelemetriaVehiculo(telemetria);
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
            <p>Ubicación actual: {telemetriaVehiculo ? `${telemetriaVehiculo.latitud}, ${telemetriaVehiculo.longitud}` : vehicle.lastLocation?.label}</p>
            <p>KM totales: {vehicle.kmTotal.toLocaleString()} km</p>
            <p>Entregas hoy: {vehicle.deliveriesToday}</p>

          </div>
        </div>

        <MapaWidget focusedVehicle={vehicle} />
      </div>
    </div>
  );
};

export default VehicleDetailView;
