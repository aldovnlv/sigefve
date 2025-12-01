import React, { useEffect, useState } from "react";
import { apiFetch } from "../auth/api";

export default function SimpleMap() {
  const [vehiculos, setVehiculos] = useState([]);

  useEffect(() => {
    async function load() {
      const data = await apiFetch("/java/vehiculos");
      if (data) setVehiculos(data);
    }
    load();
  }, []);

  return (
    <div className="map-container">
      <strong style={{ marginLeft: 10 }}>Mapa (simulado)</strong>

      {vehiculos.map((v, i) => (
        <div
          key={i}
          className="map-point"
          style={{
            background: i % 3 === 0 ? "#ff3b3b" : "#2a57ff",
            top: 80 + i * 40,
            left: 100 + i * 60
          }}
          title={v.placa}
        ></div>
      ))}
    </div>
  );
}
