import React, { useEffect, useState } from 'react';
import Navbar from '../components/layout/Navbar';
import MapWidget from '../components/dashboard/MapWidget';
import VehicleList from '../components/dashboard/VehicleList';
import StatsCards from '../components/dashboard/StatsCards';
import { dashboardController } from '../controllers/DashboardController';
import { useNavigate } from 'react-router-dom';

const DashboardView = () => {
  const [vehicles, setVehicles] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const { vehicles, alerts, stats } = await dashboardController.loadDashboardData();
        setVehicles(vehicles);
        setAlerts(alerts);
        setStats(stats);
      } catch (err) {
        console.error('Error al cargar dashboard', err);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  const focusedVehicle = vehicles[0] || null;

  return (
    <div className="app-shell">
      <Navbar alerts={alerts} />

      <main className="dashboard-layout">
        <section className="grid-stack-vertical">
          <MapWidget focusedVehicle={focusedVehicle} />
          <VehicleList vehicles={vehicles} />
        </section>

        <section className="grid-stack-vertical">
          <StatsCards stats={stats} />

          <div className="card">
            <div className="card-header">
              <div className="card-title">Rutas</div>
              <button
                className="btn btn-secondary"
                onClick={() => navigate('/rutas/nueva')}
                style={{ fontSize: '0.75rem', padding: '0.4rem 0.8rem' }}
              >
                Crear o asignar ruta
              </button>
            </div>
            <div className="card-body" style={{ fontSize: '0.85rem', color: 'var(--muted)' }}>
              Desde aquí puedes crear y asignar rutas a los vehículos de la flota.
            </div>
          </div>
        </section>
      </main>

      {loading && (
        <div style={{ textAlign: 'center', fontSize: '0.8rem', color: 'var(--muted)' }}>
          Cargando información del dashboard...
        </div>
      )}
    </div>
  );
};

export default DashboardView;
