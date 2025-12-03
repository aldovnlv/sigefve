import BaseModel from './BaseModel';

export default class Vehicle extends BaseModel {
  constructor({
    id,
    name,
    status,
    battery,
    lastLocation,
    kmTotal,
    deliveriesToday,
    type,
    loadCapacity,
    seats,
    maxRange,
    avgConsumption,
    year,
    registrationDate,
    lastUpdate
  }) {
    super();
    this.id = id;
    this.name = name;
    this.status = status;
    this.battery = battery;
    this.lastLocation = lastLocation;
    this.kmTotal = kmTotal;
    this.deliveriesToday = deliveriesToday;
    this.type = type || 'carro';
    this.loadCapacity = loadCapacity || 0;
    this.seats = seats || 0;
    this.maxRange = maxRange || 0;
    this.avgConsumption = avgConsumption || 0;
    this.year = year || 0;
    this.registrationDate = registrationDate || null;
    this.lastUpdate = lastUpdate || null;
  }

  getStatusLabel() {
    switch (this.status) {
      case 'online':
      case 'DISPONIBLE':
        return 'Disponible';
      case 'en_ruta':
      case 'EN_RUTA':
        return 'En ruta';
      case 'FUERA_SERVICIO':
      case 'offline':
      default:
        return 'Fuera de línea';
    }
  }

  getIcon() {
    if (!this.type) return '🚗';
    switch (this.type.toLowerCase()) {
      case 'bicicleta':
        return '🚲';
      case 'motocicleta':
        return '🏍';
      default:
        return '🚗';
    }
  }

  isAvailable() {
    return (this.status === 'online' || this.status === 'DISPONIBLE') && this.battery > 20;
  }
}
