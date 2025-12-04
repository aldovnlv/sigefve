import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { vehicleController } from '../../controllers/VehicleController';

const ListaVehiculos = ({ vehicles }) => {
  const navigate = useNavigate();
  const [telemetriaData, setTelemetriaData] = useState({});

  useEffect(() => {
    const fetchTelemetry = async () => {
      const data = {};
      for (const v of vehicles) {
        const t = await vehicleController.getUltimaTelemetria(v.id);
        if (t) {
          data[v.id] = t;
        }
      }
      setTelemetriaData(data);
    };

    if (vehicles.length > 0) {
      fetchTelemetry();
    }
  }, [vehicles]);

  const goToDetail = (id) => {
    navigate(`/vehiculos/${id}`);
  };

  return (
    <div className="card">
      <div className="card-header">
        <div className="card-title">Vehículos</div>
        <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>
          Estado general de la flota
        </span>
      </div>
      <div className="card-body">
        <table className="table">
          <thead>
            <tr>
              <th>Tipo</th>
              <th>ID</th>
              <th>Nombre</th>
              <th>Estado</th>
              <th>Batería</th>
              <th>Última ubicación</th>
            </tr>
          </thead>
          <tbody>
            {vehicles.map((v) => {
              const t = telemetriaData[v.id];
              return (
                <tr
                  key={v.id}
                  onClick={() => goToDetail(v.id)}
                  style={{ cursor: 'pointer' }}
                >
                  <td style={{ fontSize: '1.3rem' }}>
                    {v.getIcon()}
                  </td>
                  <td>{v.id}</td>
                  <td>{v.name}</td>
                  <td>
                    <span
                      className={
                        'chip ' +
                        (v.status === 'DISPONIBLE' || v.status === 'online'
                          ? 'chip-success'
                          : v.status === 'EN_RUTA' || v.status === 'en_ruta'
                            ? 'chip-warning'
                            : 'chip-muted')
                      }
                    >
                      {v.getStatusLabel()}
                    </span>
                  </td>
                  <td>{v.battery}%</td>
                  <td>
                    {t ? (
                      <span>
                        {t.latitud}, {t.longitud}
                      </span>
                    ) : (
                      v.lastLocation?.label
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ListaVehiculos;
