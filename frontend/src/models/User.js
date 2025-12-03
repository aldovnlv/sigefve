import BaseModel from './BaseModel';

export class UserModel extends BaseModel {
  constructor(id, name, role) {
    super();
    if (new.target === UserModel) {
      throw new Error('UserModel es abstracto. Usa una subclase concreta.');
    }
    this.id = id;
    this.name = name;
    this.role = role;
  }

  getRoleLabel() {
    return 'Usuario genérico';
  }
}

export class AdminUser extends UserModel {
  constructor(id, name) {
    super(id, name, 'admin');
  }

  getRoleLabel() {
    return 'Administrador de flota';
  }
}

export class DriverUser extends UserModel {
  constructor(id, name) {
    super(id, name, 'driver');
  }

  getRoleLabel() {
    return 'Conductor';
  }
}
