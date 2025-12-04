import BaseModel from './BaseModel';

/**
 * titulo: 'Alerta',
        descripcion: a.descripcion || '',
        estado: a.estado || false,
        fecha_generacion: a.fecha_generacion || new Date().toISOString(),
        id: a.id || 'SIN_ID',
        id_vehiculo: a.id_vehiculo || a.vehicle_id || a.vehiculo || 'SIN_VEHICULO',
        mensaje: a.mensaje || '',
        prioridad: a.prioridad || 0,
        tipo: a.tipo || 'media',
 */

export default class Alert extends BaseModel {
  constructor({ id, id_vehiculo, titulo, descripcion, prioridad, fecha_generacion, estado, mensaje, tipo }) {
    super();
    this.id = id;
    this.id_vehiculo = id_vehiculo;
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.prioridad = prioridad;
    this.fecha_generacion = fecha_generacion;

    this.estado = estado;
    this.mensaje = mensaje;
    this.tipo = tipo;
  }

  getClasePrioridad() {
    switch (this.prioridad) {
      case '1':
        return "ALTA";
      case '2':
        return "MEDIA";
      case '3':
      case '0':
      default:
        return "BAJA";
    }
  }
}