import { fleetService } from '../services/FleetService';

class DashboardController {
  async cargarDatosDelDashboard() {
    const [vehiculos, alertas, estadisticas] = await Promise.all([
      fleetService.getVehicles(),
      fleetService.getAlertasOrdenadasPorPrioridad(),
      fleetService.getStats()
    ]);

    return { vehiculos, alertas, estadisticas };
  }
}

export const dashboardController = new DashboardController();
