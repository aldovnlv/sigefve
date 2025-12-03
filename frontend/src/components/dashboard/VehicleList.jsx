import React from 'react';
import { useNavigate } from 'react-router-dom';

const VehicleList = ({ vehicles }) => {
  const navigate = useNavigate();

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
            {vehicles.map((v) => (
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
                <td>{v.lastLocation?.label}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default VehicleList;
