import { fleetService } from '../services/FleetService';

class RouteController {
  async crearRuta(routeData) {
    return fleetService.crearRuta(routeData);
  }

  async getRoutes() {
    return fleetService.getRoutes();
  }
}

export const routeController = new RouteController();
