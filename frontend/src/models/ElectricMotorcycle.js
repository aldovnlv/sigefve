import Vehicle from './Vehicle';

class ElectricMotorcycle extends Vehicle {
  constructor(data = {}) {
    super({ ...data, type: 'motocicleta' });
  }

  getMaxSpeed() {
    return 90;
  }
}

export default ElectricMotorcycle;
