import React, { useEffect, useState } from "react";
import { apiFetch } from "../auth/api";

export default function ListaVehiculos({ onSelect }) {
  const [lista, setLista] = useState([]);

  useEffect(() => {
    async function load() {
      const v = await apiFetch("/java/vehiculos");
      if (v) setLista(v);
    }
    load();
  }, []);

  return (
    <div className="card">
      <h3>Lista de vehículos</h3>

      {lista.map((v, i) => (
        <div
          key={i}
          className="vehicle-item"
          onClick={() => onSelect(v)}
        >
          <strong>{v.placa}</strong> ({v.tipo})
          <br />
          <small>Estado: {v.estado}</small>
          <br />
          <small>Batería: {v.bateria}%</small>
        </div>
      ))}
    </div>
  );
}
