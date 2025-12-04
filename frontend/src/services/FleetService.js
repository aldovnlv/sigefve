import Vehicle from '../models/Vehicle';
import ElectricBike from '../models/ElectricBike';
import ElectricMotorcycle from '../models/ElectricMotorcycle';
import Alert from '../models/Alert';
import FleetStats from '../models/FleetStats';
import Route from '../models/Route';
import { apiGet } from './apiClient';

class FleetService {
  constructor() {
    this._vehiclesMock = [
      new Vehicle({
        id: 'V-001',
        name: 'Unidad 001',
        status: 'EN_RUTA',
        battery: 82,
        lastLocation: { lat: 19.4326, lng: -99.1332, label: 'CDMX Centro' },
        kmTotal: 124500,
        deliveriesToday: 12,
        type: 'carro'
      }),
      new ElectricBike({
        id: 'B-002',
        name: 'Bicicleta 01',
        status: 'DISPONIBLE',
        battery: 90,
        lastLocation: { lat: 19.427, lng: -99.1677, label: 'CDMX Poniente' },
        kmTotal: 3500,
        deliveriesToday: 4,
        type: 'bicicleta'
      }),
      new ElectricMotorcycle({
        id: 'M-003',
        name: 'Moto Eléctrica 01',
        status: 'FUERA_SERVICIO',
        battery: 15,
        lastLocation: { lat: 19.39, lng: -99.12, label: 'Taller' },
        kmTotal: 22000,
        deliveriesToday: 0,
        type: 'motocicleta'
      })
    ];

    this._routes = [];
  }

  /**
   * 
    {
      capacidadCarga: v.capacidadCarga,
      numeroAsientos: v.numeroAsientos,
      capacidadBateria: v.capacidadBateria,
      autonomiaMaxima: v.autonomiaMaxima,
      consumoPromedio: v.consumoPromedio,
      placa: v.placa,
      modelo: v.modelo,
      anio: v.anio,
      estado: v.estado,
      tipo: v.tipo,
      kilometrajeTotal: v.kilometrajeTotal,
      fechaRegistro: v.fechaRegistro,
      ultimaActualizacion: v.ultimaActualizacion,
    }
   */

  async getVehiculos() {
    try {
      const data = await apiGet('/java/vehiculos');

      return data.map((v) => {
        const mappedData = {
          id: v.id || 'SIN_ID',
          name: v.modelo ? `${v.modelo} - ${v.placa}` : (v.placa || `Vehiculo ${v.id}`),
          status: v.estado || 'DISPONIBLE',
          battery: v.capacidadBateria || 100,
          lastLocation: {
            lat: null,
            lng: null,
            label: 'Sin ubicación'
          },
          kmTotal: v.kilometrajeTotal || 0,
          deliveriesToday: 0,
          type: v.tipo || 'carro',
          loadCapacity: v.capacidadCarga,
          seats: v.numeroAsientos,
          maxRange: v.autonomiaMaxima,
          avgConsumption: v.consumoPromedio,
          year: v.anio,
          registrationDate: v.fechaRegistro,
          lastUpdate: v.ultimaActualizacion,
          placa: v.placa || 'SIN_PLACA',
          modelo: v.modelo || 'SIN_MODELO'
        };

        if (mappedData.type && mappedData.type.toLowerCase() === 'bicicleta') {
          return new ElectricBike(mappedData);
        }

        if (mappedData.type && mappedData.type.toLowerCase() === 'motocicleta') {
          return new ElectricMotorcycle(mappedData);
        }

        return new Vehicle(mappedData);
      });
    } catch (err) {
      console.error('Error cargando vehículos desde API, usando datos simulados.', err);
      return this._vehiclesMock;
    }
  }

  async getVehicleById(id) {
    try {
      const v = await apiGet(`/java/vehiculos/${id}`);

      const mappedData = {
        id: v.id || v.id_vehiculo || id,
        name: v.nombre || v.name || `Vehiculo ${id}`,
        status: v.estado || 'CARGANDO',
        battery: v.bateria || v.battery || 100,
        lastLocation: {
          lat: v.latitud ?? null,
          lng: v.longitud ?? null,
          label: v.ultima_ubicacion || v.ubicacion || 'Sin ubicación'
        },
        kmTotal: v.km_total || v.kmTotal || 0,
        deliveriesToday: v.entregas_hoy || v.deliveriesToday || 0,
        type: v.tipo || v.type || 'carro',
        loadCapacity: v.capacidadCarga,
        seats: v.numeroAsientos,
        maxRange: v.autonomiaMaxima,
        avgConsumption: v.consumoPromedio,
        year: v.anio,
        registrationDate: v.fechaRegistro,
        lastUpdate: v.ultimaActualizacion,
        placa: v.placa || 'SIN_PLACA',
        modelo: v.modelo || 'SIN_MODELO'
      };

      if (mappedData.type && mappedData.type.toLowerCase() === 'bicicleta') {
        return new ElectricBike(mappedData);
      }

      if (mappedData.type && mappedData.type.toLowerCase() === 'motocicleta') {
        return new ElectricMotorcycle(mappedData);
      }
      
      return new Vehicle(mappedData);
    } catch (err) {
      console.error('Error cargando vehículo desde API, buscando en datos simulados.', err);
      return this._vehiclesMock.find((v) => v.id === id) || null;
    }
  }

  async getAlertas() {
    try {
      const alertas = await apiGet('/python/alertas');
      // alert(alertas["alertas"][0].descripcion)
      return alertas["alertas"].map((a) => new Alert({
        titulo: 'Alerta',
        descripcion: a.descripcion || '',
        estado: a.estado || false,
        fecha_generacion: a.fecha_generacion || new Date().toISOString(),
        id: a.id || 'SIN_ID',
        id_vehiculo: a.id_vehiculo || a.vehicle_id || a.vehiculo || 'SIN_VEHICULO',
        mensaje: a.mensaje || '',
        prioridad: a.prioridad || 0,
        tipo: a.tipo || 'media',
      }));
    } catch (err) {
      console.error('Error cargando alertas desde API, creando alerta simulada.', err);
      return [
        new Alert({
          id: 'A-MOCK',
          id_vehiculo: 'V-001',
          titulo: 'Alerta simulada',
          descripcion: 'No se pudo obtener la lista real de alertas.',
          tipo: 'baja',
          fecha_generacion: new Date().toISOString()
        })
      ];
    }
  }

  async getAlertasOrdenadasPorPrioridad() {
    const alerts = await this.getAlertas();
    return [...alerts].sort((a, b) => b.getClasePrioridad() - a.getClasePrioridad());
  }

  async getEstadisticas() {
    try {
      const s = await apiGet('/python/estadisticas');
      const v = await this.getVehiculos();
      // obten la cantidad de vehiculos disponibles
      const vehiculosDisponibles = v.filter((v) => v.status === 'DISPONIBLE').length;
      // s["estadisticas"]  contiene las estadisticas de cada vehiculo (s["estadisticas"][n]) las cuales son eficiencia_bateria, entregas_completadas, id_vehiculo, kilometros_totales, registros_procesados
      // suma cada una de las estadisticas de los vehiculos s["estadisticas"][n] para obtener las estadisticas totales

      const totalKm = s.estadisticas.reduce((sum, v) => sum + (v.kilometros_totales || 0), 0);
      const entregasCompletadas = s.estadisticas.reduce((sum, v) => sum + (v.entregas_completadas || 0), 0);
      const availableVehicles = s.estadisticas.reduce((sum, v) => sum + (v.eficiencia_bateria || 0), 0);
      return new FleetStats({
        totalKm,
        entregasCompletadas,
        vehiculosDisponibles
      });
    } catch (err) {
      console.error('Error cargando estadísticas desde API, calculando estadísticas locales.', err);
      const vehicles = await this.getVehiculos();
      const totalKm = vehicles.reduce((sum, v) => sum + (v.kmTotal || 0), 0);
      const deliveriesToday = vehicles.reduce((sum, v) => sum + (v.deliveriesToday || 0), 0);
      const availableVehicles = vehicles.filter((v) => v.isAvailable()).length;

      return new FleetStats({ totalKm, deliveriesToday, availableVehicles });
    }
  }

  async createRoute(routeData) {
    const newRoute = new Route({
      id: `R-${this._routes.length + 1}`,
      ...routeData
    });
    this._routes.push(newRoute);
    return newRoute;
  }

  async getRoutes() {
    return this._routes;
  }
}

export const fleetService = new FleetService();
