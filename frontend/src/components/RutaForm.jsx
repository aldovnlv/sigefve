import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import vehiculoService from '../services/vehiculoService';
import rutaService from '../services/rutaService';
import Navbar from './Navbar';
import '../styles/RutaForm.css';

const RutaForm = () => {
    const navigate = useNavigate();
    const [vehiculos, setVehiculos] = useState([]);
    const [rutas, setRutas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Estado para crear nueva ruta
    const [nuevaRuta, setNuevaRuta] = useState({
        nombre: '',
        distanciaTotal: '',
        vehiculoId: ''
    });

    // Estado para asignar vehículo a ruta existente
    const [asignacion, setAsignacion] = useState({
        rutaId: '',
        vehiculoId: ''
    });

    useEffect(() => {
        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        try {
            const [vehiculosData, rutasData] = await Promise.all([
                vehiculoService.getVehiculos(),
                rutaService.getRutas()
            ]);
            setVehiculos(vehiculosData.filter(v => v.estado === 'DISPONIBLE'));
            setRutas(rutasData);
        } catch (err) {
            console.error('Error al cargar datos:', err);
        }
    };

    const handleCrearRuta = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            const data = {
                nombre: nuevaRuta.nombre,
                distanciaTotal: parseFloat(nuevaRuta.distanciaTotal),
                vehiculoId: parseInt(nuevaRuta.vehiculoId) || undefined
            };

            await rutaService.createRuta(data);
            setSuccess('Ruta creada exitosamente');
            setNuevaRuta({ nombre: '', distanciaTotal: '', vehiculoId: '' });
            cargarDatos();

            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.error || 'Error al crear ruta');
        } finally {
            setLoading(false);
        }
    };

    const handleAsignarVehiculo = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            await rutaService.asignarVehiculo(
                parseInt(asignacion.rutaId),
                parseInt(asignacion.vehiculoId)
            );
            setSuccess('Vehículo asignado exitosamente');
            setAsignacion({ rutaId: '', vehiculoId: '' });
            cargarDatos();

            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.error || 'Error al asignar vehículo');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <Navbar />
            <div className="ruta-form-container">
                <div className="form-header">
                    <button onClick={() => navigate('/admin')} className="btn-back">
                        ← Volver
                    </button>
                    <h1>Gestión de Rutas</h1>
                </div>

                {error && <div className="alert alert-error">{error}</div>}
                {success && <div className="alert alert-success">{success}</div>}

                <div className="forms-grid">
                    {/* Formulario para crear nueva ruta */}
                    <div className="form-card">
                        <h2>📝 Crear Nueva Ruta</h2>
                        <form onSubmit={handleCrearRuta}>
                            <div className="form-group">
                                <label htmlFor="nombre">Nombre de la Ruta *</label>
                                <input
                                    id="nombre"
                                    type="text"
                                    value={nuevaRuta.nombre}
                                    onChange={(e) => setNuevaRuta({ ...nuevaRuta, nombre: e.target.value })}
                                    required
                                    placeholder="Ej: Entregas Zona Centro"
                                    disabled={loading}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="distancia">Distancia Total (km) *</label>
                                <input
                                    id="distancia"
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    value={nuevaRuta.distanciaTotal}
                                    onChange={(e) => setNuevaRuta({ ...nuevaRuta, distanciaTotal: e.target.value })}
                                    required
                                    placeholder="Ej: 15.5"
                                    disabled={loading}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="vehiculo-crear">Vehículo (Opcional)</label>
                                <select
                                    id="vehiculo-crear"
                                    value={nuevaRuta.vehiculoId}
                                    onChange={(e) => setNuevaRuta({ ...nuevaRuta, vehiculoId: e.target.value })}
                                    disabled={loading}
                                >
                                    <option value="">Asignar vehículo después</option>
                                    {vehiculos.map((v) => (
                                        <option key={v.id} value={v.id}>
                                            {v.placa} - {v.tipo} ({v.capacidadBateria}% batería)
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <button type="submit" className="btn-primary" disabled={loading}>
                                {loading ? 'Creando...' : 'Crear Ruta'}
                            </button>
                        </form>
                    </div>

                    {/* Formulario para asignar vehículo a ruta */}
                    <div className="form-card">
                        <h2>🚗 Asignar Vehículo a Ruta</h2>
                        <form onSubmit={handleAsignarVehiculo}>
                            <div className="form-group">
                                <label htmlFor="ruta-asignar">Seleccionar Ruta *</label>
                                <select
                                    id="ruta-asignar"
                                    value={asignacion.rutaId}
                                    onChange={(e) => setAsignacion({ ...asignacion, rutaId: e.target.value })}
                                    required
                                    disabled={loading}
                                >
                                    <option value="">-- Seleccione una ruta --</option>
                                    {rutas.filter(r => !r.completada).map((r) => (
                                        <option key={r.id} value={r.id}>
                                            {r.nombre} ({r.distanciaTotal} km)
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="vehiculo-asignar">Seleccionar Vehículo *</label>
                                <select
                                    id="vehiculo-asignar"
                                    value={asignacion.vehiculoId}
                                    onChange={(e) => setAsignacion({ ...asignacion, vehiculoId: e.target.value })}
                                    required
                                    disabled={loading}
                                >
                                    <option value="">-- Seleccione un vehículo --</option>
                                    {vehiculos.map((v) => (
                                        <option key={v.id} value={v.id}>
                                            {v.placa} - {v.tipo} ({v.capacidadBateria}% batería)
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <button type="submit" className="btn-primary" disabled={loading}>
                                {loading ? 'Asignando...' : 'Asignar Vehículo'}
                            </button>
                        </form>
                    </div>
                </div>

                {/* Lista de rutas existentes */}
                <div className="rutas-list-card">
                    <h2>📋 Rutas Existentes</h2>
                    <div className="rutas-table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Distancia</th>
                                    <th>Entregas</th>
                                    <th>Estado</th>
                                    <th>Fecha Inicio</th>
                                </tr>
                            </thead>
                            <tbody>
                                {rutas.map((ruta) => (
                                    <tr key={ruta.id}>
                                        <td>{ruta.id}</td>
                                        <td>{ruta.nombre}</td>
                                        <td>{ruta.distanciaTotal} km</td>
                                        <td>{ruta.numeroEntregas}</td>
                                        <td>
                                            {ruta.completada ? (
                                                <span className="badge badge-success">Completada</span>
                                            ) : (
                                                <span className="badge badge-pending">Pendiente</span>
                                            )}
                                        </td>
                                        <td>{ruta.fechaInicio}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </>
    );
};

export default RutaForm;
