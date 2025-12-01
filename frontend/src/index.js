// Punto de entrada principal del proyecto con React + Vite

import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css"; // estilos globales (Tailwind o CSS propio)

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
