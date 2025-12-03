import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import authService from '../services/authService';
import alertaService from '../services/alertaService';
import '../styles/Navbar.css';

const Navbar = () => {
    const [numAlertas, setNumAlertas] = useState(0);
    const [menuOpen, setMenuOpen] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const user = authService.getCurrentUser();

    useEffect(() => {
        cargarAlertas();
        // Actualizar alertas cada 30 segundos
        const interval = setInterval(cargarAlertas, 30000);
        return () => clearInterval(interval);
    }, []);

    const cargarAlertas = async () => {
        try {
            const alertas = await alertaService.getAlertas();
            const alertasActivas = alertas.filter(a => a.estado);
            setNumAlertas(alertasActivas.length);
        } catch (error) {
            console.error('Error al cargar alertas:', error);
        }
    };

    const handleLogout = async () => {
        await authService.logout();
        navigate('/login');
    };

    const isActive = (path) => location.pathname === path;

    return (
        <nav className="navbar">
            <div className="navbar-container">
                <Link to="/admin" className="navbar-brand">
                    <span className="brand-icon">⚡</span>
                    SIGEFVE
                </Link>

                <button
                    className="navbar-toggle"
                    onClick={() => setMenuOpen(!menuOpen)}
                >
                    ☰
                </button>

                <div className={`navbar-menu ${menuOpen ? 'active' : ''}`}>
                    <Link
                        to="/admin"
                        className={`nav-link ${isActive('/admin') ? 'active' : ''}`}
                    >
                        📊 Dashboard
                    </Link>

                    <Link
                        to="/admin/vehiculos"
                        className={`nav-link ${isActive('/admin/vehiculos') ? 'active' : ''}`}
                    >
                        🚗 Vehículos
                    </Link>

                    <Link
                        to="/admin/rutas"
                        className={`nav-link ${isActive('/admin/rutas') ? 'active' : ''}`}
                    >
                        🗺️ Rutas
                    </Link>

                    <Link
                        to="/admin/alertas"
                        className={`nav-link ${isActive('/admin/alertas') ? 'active' : ''}`}
                    >
                        <span className="alert-badge-container">
                            🔔 Alertas
                            {numAlertas > 0 && (
                                <span className="alert-count">{numAlertas}</span>
                            )}
                        </span>
                    </Link>

                    <div className="navbar-user">
                        <span className="user-name">{user?.username}</span>
                        <button onClick={handleLogout} className="btn-logout-nav">
                            Cerrar Sesión
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
