import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import { routeController } from '../controllers/RouteController';
import { dashboardController } from '../controllers/DashboardController';

const RouteFormView = () => {
  const [vehicleId, setVehicleId] = useState('');
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [plannedKm, setPlannedKm] = useState('');
  const [notes, setNotes] = useState('');
  const [alerts, setAlerts] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const load = async () => {
      const { vehicles, alerts } = await dashboardController.loadDashboardData();
      setVehicles(vehicles);
      setAlerts(alerts);
      if (vehicles.length > 0) {
        setVehicleId(vehicles[0].id);
      }
    };
    load();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    try {
      const route = await routeController.createRoute({
        vehicleId,
        origin,
        destination,
        plannedKm: Number(plannedKm),
        notes
      });

      setMessage(`Ruta ${route.id} creada correctamente para el vehículo ${vehicleId}.`);
      setOrigin('');
      setDestination('');
      setPlannedKm('');
      setNotes('');
    } catch (err) {
      setMessage('Error al crear la ruta.');
    }
  };

  return (
    <div className="app-shell">
      <Navbar alerts={alerts} />

      <div className="route-form">
        <div className="card">
          <div className="card-header">
            <div className="card-title">Crear o asignar ruta</div>
            <Link to="/" style={{ fontSize: '0.75rem', color: 'var(--primary-soft)' }}>
              Volver al dashboard
            </Link>
          </div>
          <div className="card-body">
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Vehículo</label>
                <select
                  className="form-input"
                  value={vehicleId}
                  onChange={(e) => setVehicleId(e.target.value)}
                >
                  {vehicles.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.id} - {v.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Origen</label>
                  <input
                    className="form-input"
                    value={origin}
                    onChange={(e) => setOrigin(e.target.value)}
                    placeholder="Bodega central"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Destino</label>
                  <input
                    className="form-input"
                    value={destination}
                    onChange={(e) => setDestination(e.target.value)}
                    placeholder="Cliente final"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">KM planeados</label>
                  <input
                    className="form-input"
                    type="number"
                    value={plannedKm}
                    onChange={(e) => setPlannedKm(e.target.value)}
                    placeholder="25"
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Notas</label>
                <textarea
                  className="form-input"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Instrucciones especiales, ventanas horarias, etc."
                />
              </div>

              {message && (
                <div style={{ fontSize: '0.8rem', marginBottom: '0.6rem' }}>{message}</div>
              )}

              <button type="submit" className="btn btn-primary">
                Guardar ruta
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RouteFormView;
