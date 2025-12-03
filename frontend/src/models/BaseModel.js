export default class BaseModel {
  constructor() {
    if (new.target === BaseModel) {
      throw new Error('BaseModel es una clase abstracta y no puede instanciarse directamente.');
    }
    this.id = null;
  }
}
