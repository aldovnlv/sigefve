import { Navigate } from 'react-router-dom';
import authService from '../services/authService';

const ProtectedRoute = ({ children, requiredRole }) => {
    const isAuthenticated = authService.isAuthenticated();
    const user = authService.getCurrentUser();

    // Si no está autenticado, redirigir a login
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // Si se requiere un rol específico y el usuario no lo tiene
    if (requiredRole && user?.rol !== requiredRole) {
        return (
            <div style={{ padding: '20px', textAlign: 'center' }}>
                <h2>Acceso Denegado</h2>
                <p>No tienes permisos para acceder a esta página.</p>
            </div>
        );
    }

    return children;
};

export default ProtectedRoute;
