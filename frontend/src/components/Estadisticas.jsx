import React, { useEffect, useState } from "react";
import { apiFetch } from "../auth/api";

export default function Estadisticas() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    async function cargar() {
      const data = await apiFetch("/python/estadisticas");
      if (data) setStats(data);
    }
    cargar();
  }, []);

  return (
    <div style={{ display: "flex", gap: "20px", marginBottom: 20 }}>
      <div className="metric-box">
        <div className="metric-number">{stats?.totalKm || "-"}</div>
        <div className="metric-label">Total km (hoy)</div>
      </div>

      <div className="metric-box">
        <div className="metric-number">{stats?.entregas || "-"}</div>
        <div className="metric-label">Entregas hoy</div>
      </div>

      <div className="metric-box">
        <div className="metric-number">{stats?.disponibles || "-"}</div>
        <div className="metric-label">Vehículos disponibles</div>
      </div>
    </div>
  );
}
