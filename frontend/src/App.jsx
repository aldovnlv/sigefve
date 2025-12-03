import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginView from './views/LoginView';
import DashboardView from './views/DashboardView';
import VehicleDetailView from './views/VehicleDetailView';
import RouteFormView from './views/RouteFormView';
import ProtectedRoute from './components/common/ProtectedRoute';
import { authController } from './controllers/AuthController';

const App = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginView />} />

      <Route
        path="/"
        element={
          <ProtectedRoute isAuthenticated={authController.isAuthenticated()}>
            <DashboardView />
          </ProtectedRoute>
        }
      />

      <Route
        path="java/vehiculos/:id"
        element={
          <ProtectedRoute isAuthenticated={authController.isAuthenticated()}>
            <VehicleDetailView />
          </ProtectedRoute>
        }
      />

      <Route
        path="/rutas/nueva"
        element={
          <ProtectedRoute isAuthenticated={authController.isAuthenticated()}>
            <RouteFormView />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
