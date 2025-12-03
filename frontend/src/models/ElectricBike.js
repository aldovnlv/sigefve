import Vehicle from './Vehicle';

class ElectricBike extends Vehicle {
  constructor(data = {}) {
    super({ ...data, type: 'bicicleta' });
  }

  getMaxSpeed() {
    return 35;
  }
}

export default ElectricBike;
