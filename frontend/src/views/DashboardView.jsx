import React, { useEffect, useState } from 'react';
import Navbar from '../components/layout/Navbar';
import MapWidget from '../components/dashboard/MapWidget';
import VehicleList from '../components/dashboard/VehicleList';
import StatsCards from '../components/dashboard/StatsCards';
import { dashboardController } from '../controllers/DashboardController';
import { useNavigate } from 'react-router-dom';

const DashboardView = () => {
  const [vehiculos, setVehiculos] = useState([]);
  const [alertas, setAlertas] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);
  const [loading, setCargando] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const load = async () => {
      setCargando(true);
      try {
        const { vehiculos, alertas, estadisticas } = await dashboardController.cargarDatosDelDashboard();
        setVehiculos(vehiculos);
        setAlertas(alertas);
        setEstadisticas(estadisticas);
      } catch (err) {
        console.error('Error al cargar dashboard', err);
      } finally {
        setCargando(false);
      }
    };

    load();
  }, []);

  const focusedVehicle = vehiculos[0] || null;

  return (
    <div className="app-shell">
      <Navbar alerts={alertas} />

      <main className="dashboard-layout">
        <section className="grid-stack-vertical">
          <MapWidget focusedVehicle={focusedVehicle} />
          <VehicleList vehicles={vehiculos} />
        </section>

        <section className="grid-stack-vertical">
          <StatsCards stats={estadisticas} />

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
