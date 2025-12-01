import React, { useEffect, useState } from "react";
import { apiFetch } from "../auth/api";

export default function Alertas() {
  const [alertas, setAlertas] = useState([]);

  useEffect(() => {
    async function load() {
      const data = await apiFetch("/python/alertas");
      if (data) setAlertas(data);
    }
    load();
  }, []);

  return (
    <div className="card">
      <h3>Alertas activas</h3>

      {alertas.map((a, i) => (
        <div
          key={i}
          className={
            a.prioridad === "URGENTE"
              ? "alert-box"
              : "alert-medium alert-box"
          }
        >
          <strong>{a.prioridad}</strong> — {a.vehiculo} {a.mensaje}
        </div>
      ))}
    </div>
  );
}
