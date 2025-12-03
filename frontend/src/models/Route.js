import BaseModel from './BaseModel';

export default class Route extends BaseModel {
  constructor({ id, vehicleId, origin, destination, plannedKm, notes }) {
    super();
    this.id = id;
    this.vehicleId = vehicleId;
    this.origin = origin;
    this.destination = destination;
    this.plannedKm = plannedKm;
    this.notes = notes;
  }
}
