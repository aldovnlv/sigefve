import { fleetService } from '../services/FleetService';

class DashboardController {
  async loadDashboardData() {
    const [vehicles, alerts, stats] = await Promise.all([
      fleetService.getVehicles(),
      fleetService.getAlertsSortedByPriority(),
      fleetService.getStats()
    ]);

    return { vehicles, alerts, stats };
  }
}

export const dashboardController = new DashboardController();
