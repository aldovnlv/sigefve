import React, { useState } from "react";
import { apiFetch } from "../api/api";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    // ESTA LÍNEA ES LA QUE FALTABA — AHORA ESTÁ DEFINIDA
    const formBody = new URLSearchParams();
    formBody.append("username", username);
    formBody.append("password", password);

    try {
      const data = await apiFetch("/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(formBody)
      });

      if (!data || !data.token) {
        throw new Error("Credenciales incorrectas");
      }

      console.log("Token recibido:", data.token);

      localStorage.setItem("token", data.token);

      alert("Inicio de sesión exitoso");

      // Redirigir al dashboard
      window.location.href = "/dashboard";

    } catch (error) {
      console.error(error);
      setErrorMsg(error.message || "Error al iniciar sesión");
    }
  };

  return (
    <div style={{ padding: 30 }}>
      <h2>Iniciar sesión</h2>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Usuario"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />

        <input
          type="password"
          placeholder="Contraseña"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {errorMsg && (
          <p style={{ color: "red" }}>
            {errorMsg}
          </p>
        )}

        <button type="submit">Entrar</button>
      </form>
    </div>
  );
}
