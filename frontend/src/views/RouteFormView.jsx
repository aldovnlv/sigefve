import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import { routeController } from '../controllers/RouteController';
import { dashboardController } from '../controllers/DashboardController';

const RouteFormView = () => {
  const [vehiculoId, setVehiculoId] = useState('');
  const [nombre, setNombre] = useState('');
  const [distanciaTotal, setDistanciaTotal] = useState('');
  const [alerts, setAlerts] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const load = async () => {
      const { vehiculos, alertas } = await dashboardController.cargarDatosDelDashboard();
      setVehicles(vehiculos);
      setAlerts(alertas);
      if (vehiculos.length > 0) {
        setVehiculoId(vehiculos[0].id);
      }
    };
    load();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    try {
      const route = await routeController.crearRuta({
        vehiculoId,
        nombre,
        distanciaTotal: Number(distanciaTotal)
      });

      setMessage(`Ruta ${route.id} creada correctamente para el vehículo ${vehiculoId}.`);
      setNombre('');
      setDistanciaTotal('');
    } catch (err) {
      setMessage('Error al crear la rutao.');
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
                  value={vehiculoId}
                  onChange={(e) => setVehiculoId(e.target.value)}
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
                  <label className="form-label">Nombre</label>
                  <input
                    required
                    className="form-input"
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    placeholder="Bodega central"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">KM planeados</label>
                  <input
                    required
                    className="form-input"
                    type="number"
                    value={distanciaTotal}
                    onChange={(e) => setDistanciaTotal(e.target.value)}
                    placeholder="25"
                  />
                </div>
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
