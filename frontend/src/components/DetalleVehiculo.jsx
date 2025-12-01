import React, { useEffect, useState } from "react";
import { apiFetch } from "../auth/api";

export default function DetalleVehiculo({ vehiculo, onBack }) {
  const [detalle, setDetalle] = useState(null);

  useEffect(() => {
    async function loadDetail() {
      const data = await apiFetch(`/java/vehiculos/${vehiculo.id}`);
      if (data) setDetalle(data);
    }
    loadDetail();
  }, [vehiculo]);

  return (
    <div style={{ marginTop: 20 }}>
      <button
        onClick={onBack}
        style={{
          background: "#2a3b8f",
          color: "white",
          border: "none",
          padding: "8px 14px",
          borderRadius: 6,
          cursor: "pointer",
          marginBottom: 15
        }}
      >
        ← Volver a lista
      </button>

      <div
        className="card"
        style={{ padding: 25, borderTop: "5px solid #2a3b8f" }}
      >
        <h2 style={{ margin: "0 0 10px" }}>
          {vehiculo.placa} — {vehiculo.tipo}
        </h2>

        {!detalle && <p>Cargando información…</p>}

        {detalle && (
          <>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 20,
                marginTop: 20
              }}
            >
              <div>
                <h4 style={{ marginBottom: 10 }}>Información General</h4>
                <p><strong>Marca:</strong> {detalle.marca}</p>
                <p><strong>Modelo:</strong> {detalle.modelo}</p>
                <p><strong>Año:</strong> {detalle.anio}</p>
                <p><strong>Tipo:</strong> {detalle.tipo}</p>
                <p><strong>Batería:</strong> {detalle.bateria}%</p>
              </div>

              <div>
                <h4 style={{ marginBottom: 10 }}>Estado</h4>
                <p><strong>Estado:</strong> {detalle.estado}</p>
                <p><strong>Ult. ubicación:</strong> {detalle.ubicacion}</p>
                <p><strong>Velocidad:</strong> {detalle.velocidad} km/h</p>
                <p><strong>Temperatura:</strong> {detalle.temperatura}°C</p>
              </div>
            </div>

            <div style={{ marginTop: 25 }}>
              <h4>Observaciones</h4>
              <p style={{ background: "#f5f6fa", padding: 15, borderRadius: 8 }}>
                {detalle.observaciones || "Sin observaciones registradas."}
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
