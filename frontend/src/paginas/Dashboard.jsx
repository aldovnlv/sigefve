import React, { useState } from "react";
import Navbar from "../components/Navbar";
import SimpleMap from "../components/SimpleMap";
import Estadisticas from "../components/Estadisticas";
import Alertas from "../components/Alertas";
import ListaVehiculos from "../components/ListaVehiculos";
import DetalleVehiculo from "../components/DetalleVehiculo";

export default function Dashboard() {
  const [selected, setSelected] = useState(null);

  if (selected) {
    return (
      <>
        <Navbar />
        <div style={{ padding: 20 }}>
          <DetalleVehiculo vehiculo={selected} onBack={() => setSelected(null)} />
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <div style={{
        display: "grid",
        gridTemplateColumns: "2fr 1fr",
        gap: "20px",
        padding: 20
      }}>

        <div>
          <Estadisticas />
          <SimpleMap />
          <ListaVehiculos onSelect={setSelected} />
        </div>

        <div>
          <Alertas />
        </div>

      </div>
    </>
  );
}
