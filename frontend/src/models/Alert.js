import BaseModel from './BaseModel';

export default class Alert extends BaseModel {
  constructor({ id, vehicleId, title, description, priority, createdAt }) {
    super();
    this.id = id;
    this.vehicleId = vehicleId;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.createdAt = createdAt;
  }

  getPriorityScore() {
    switch (this.priority) {
      case 'alta':
      case 'ALTA':
        return 3;
      case 'media':
      case 'MEDIA':
        return 2;
      case 'baja':
      case 'BAJA':
      default:
        return 1;
    }
  }
}
