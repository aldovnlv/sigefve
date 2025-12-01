import React from "react";

export default function Navbar({ onLogout }) {
  return (
    <div style={{
      background: "#2a3b8f",
      padding: "15px 25px",
      color: "white",
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center"
    }}>
      <h2 style={{ margin: 0 }}>SIGEFVE - Dashboard</h2>

      <div style={{ display: "flex", alignItems: "center", gap: "15px" }}>
        <span>jessica</span>
        <button
          onClick={onLogout}
          style={{
            background: "#c62828",
            border: "none",
            padding: "8px 15px",
            borderRadius: "6px",
            color: "white",
            cursor: "pointer"
          }}
        >
          Cerrar sesión
        </button>
      </div>
    </div>
  );
}
