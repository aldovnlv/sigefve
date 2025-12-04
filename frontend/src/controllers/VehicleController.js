import { fleetService } from '../services/FleetService';

class VehicleController {
  async getVehicleById(id) {
    return fleetService.getVehicleById(id);
  }

  async getUltimaTelemetria(id) {
    return fleetService.getUltimaTelemetria(id);
  }
}

export const vehicleController = new VehicleController();
