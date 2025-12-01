import React, { useState } from "react";
import { apiFetch } from "../auth/api";

export default function FormRutas() {
  const [origen, setOrigen] = useState("");
  const [destino, setDestino] = useState("");
  const [fecha, setFecha] = useState("");
  const [mensaje, setMensaje] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje("");

    if (!origen.trim() || !destino.trim() || !fecha) {
      setMensaje("Todos los campos son obligatorios.");
      return;
    }

    const body = {
      origen,
      destino,
      fecha
    };

    const data = await apiFetch("/python/rutas", {
      method: "POST",
      body: JSON.stringify(body)
    });

    if (data?.ok) {
      setMensaje("Ruta registrada correctamente.");
      setOrigen("");
      setDestino("");
      setFecha("");
    } else {
      setMensaje("Error al guardar la ruta.");
    }
  };

  return (
    <div className="card" style={{ marginTop: 20 }}>
      <h3 style={{ marginBottom: 15 }}>Registrar Ruta</h3>

      <form onSubmit={handleSubmit} style={{ display: "grid", gap: 12 }}>
        <div>
          <label>Origen</label>
          <input
            type="text"
            className="input-text"
            style={{
              width: "100%",
              padding: 10,
              borderRadius: 6,
              border: "1px solid #ccc"
            }}
            value={origen}
            onChange={(e) => setOrigen(e.target.value)}
          />
        </div>

        <div>
          <label>Destino</label>
          <input
            type="text"
            style={{
              width: "100%",
              padding: 10,
              borderRadius: 6,
              border: "1px solid #ccc"
            }}
            value={destino}
            onChange={(e) => setDestino(e.target.value)}
          />
        </div>

        <div>
          <label>Fecha</label>
          <input
            type="date"
            style={{
              width: "100%",
              padding: 10,
              borderRadius: 6,
              border: "1px solid #ccc"
            }}
            value={fecha}
            onChange={(e) => setFecha(e.target.value)}
          />
        </div>

        <button
          type="submit"
          style={{
            background: "#2a3b8f",
            color: "white",
            padding: "10px",
            borderRadius: 6,
            border: "none",
            cursor: "pointer",
            marginTop: 10
          }}
        >
          Guardar ruta
        </button>

        {mensaje && (
          <p
            style={{
              color: mensaje.includes("correctamente") ? "green" : "red"
            }}
          >
            {mensaje}
          </p>
        )}
      </form>
    </div>
  );
}
