import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import DashboardAdmin from './components/DashboardAdmin';
import DashboardConductor from './components/DashboardConductor';
import VehiculoDetalle from './components/VehiculoDetalle';
import MapaVehiculos from './components/MapaVehiculos';
import RutaForm from './components/RutaForm';
import AlertasManagement from './components/AlertasManagement';
import ProtectedRoute from './components/ProtectedRoute';
import authService from './services/authService';
import './index.css';

function App() {
    return (
        <Router>
            <Routes>
                {/* Ruta raíz - Redirigir según autenticación */}
                <Route path="/" element={<Home />} />

                {/* Ruta de login */}
                <Route path="/login" element={<Login />} />

                {/* Rutas protegidas para Administrador */}
                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute requiredRole="Administrador">
                            <DashboardAdmin />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/vehiculos"
                    element={
                        <ProtectedRoute requiredRole="Administrador">
                            <MapaVehiculos />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/vehiculos/:id"
                    element={
                        <ProtectedRoute requiredRole="Administrador">
                            <VehiculoDetalle />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/rutas"
                    element={
                        <ProtectedRoute requiredRole="Administrador">
                            <RutaForm />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/alertas"
                    element={
                        <ProtectedRoute requiredRole="Administrador">
                            <AlertasManagement />
                        </ProtectedRoute>
                    }
                />

                {/* Rutas protegidas para Conductor */}
                <Route
                    path="/conductor"
                    element={
                        <ProtectedRoute requiredRole="Conductor">
                            <DashboardConductor />
                        </ProtectedRoute>
                    }
                />

                {/* Ruta 404 */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    );
}

// Componente Home que redirige según autenticación
const Home = () => {
    const isAuthenticated = authService.isAuthenticated();
    const user = authService.getCurrentUser();

    if (isAuthenticated && user) {
        if (user.rol === 'Administrador') {
            return <Navigate to="/admin" replace />;
        } else if (user.rol === 'Conductor') {
            return <Navigate to="/conductor" replace />;
        }
    }

    return <Navigate to="/login" replace />;
};

export default App;
