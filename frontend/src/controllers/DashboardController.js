import { fleetService } from '../services/FleetService';

class DashboardController {
  async cargarDatosDelDashboard() {
    const [vehiculos, alertas, estadisticas] = await Promise.all([
      fleetService.getVehiculos(),
      fleetService.getAlertasOrdenadasPorPrioridad(),
      fleetService.getEstadisticas()
    ]);

    return { vehiculos, alertas, estadisticas };
  }
}

export const dashboardController = new DashboardController();
