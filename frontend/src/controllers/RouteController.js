import { fleetService } from '../services/FleetService';

class RouteController {
  async createRoute(routeData) {
    return fleetService.createRoute(routeData);
  }

  async getRoutes() {
    return fleetService.getRoutes();
  }
}

export const routeController = new RouteController();
