import { fleetService } from '../services/FleetService';

class VehicleController {
  async getVehicleById(id) {
    return fleetService.getVehicleById(id);
  }
}

export const vehicleController = new VehicleController();
