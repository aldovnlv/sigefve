import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authController } from '../controllers/AuthController';

const LoginView = () => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('1234');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await authController.login(username, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">SIGEFVE</div>
        <div className="login-title">Inicio de sesión</div>
        <div className="login-subtitle">
          Accede al panel de monitoreo de vehículos eléctricos.
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Usuario</label>
            <input
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="admin"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contraseña</label>
            <input
              className="form-input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="1234"
            />
          </div>

          {error && <div className="error-text">{error}</div>}

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
            <span aria-hidden="true">→</span>
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginView;
