import BaseModel from './BaseModel';

export default class FleetStats extends BaseModel {
  constructor({ totalKm, entregasCompletadas, vehiculosDisponibles }) {
    super();
    /**
     * "eficiencia_bateria": 79.62,
      "entregas_completadas": 0,
      "id_vehiculo": 1,
      "kilometros_totales": 1250.5,
      "registros_procesados": 100
     */
    this.totalKm = totalKm;
    this.deliveriesToday = entregasCompletadas; 
    this.availableVehicles = vehiculosDisponibles;
  }
}
