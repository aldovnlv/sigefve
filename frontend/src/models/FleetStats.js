import BaseModel from './BaseModel';

export default class FleetStats extends BaseModel {
  constructor({ totalKm, deliveriesToday, availableVehicles }) {
    super();
    this.totalKm = totalKm;
    this.deliveriesToday = deliveriesToday;
    this.availableVehicles = availableVehicles;
  }
}
