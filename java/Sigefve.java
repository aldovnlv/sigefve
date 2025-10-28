// ============================================================================
// ENUMERACIONES
// ============================================================================

/**
 * Estados posibles de un vehiculo en el sistema
 */
enum EstadoVehiculo {
    DISPONIBLE,
    EN_RUTA,
    MANTENIMIENTO,
    CARGANDO
}

// ============================================================================
// CLASES DE MODELO
// ============================================================================

/**
 * Clase abstracta base para todos los vehiculos
 */
abstract class Vehiculo {
    protected int id;
    protected String modelo;
    protected EstadoVehiculo estado;
    
    public Vehiculo(int id, String modelo) {
        this.id = id;
        this.modelo = modelo;
        this.estado = EstadoVehiculo.DISPONIBLE;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public EstadoVehiculo getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }
    
    /**
     * Metodo abstracto para asignar ruta al vehiculo
     */
    public abstract void asignarRuta(Ruta ruta);
}

/**
 * Clase abstracta para vehiculos electricos
 */
abstract class VehiculoElectrico extends Vehiculo {
    protected float capacidadBateria;
    protected float nivelBateria;
    
    public VehiculoElectrico(int id, String modelo, float capacidadBateria) {
        super(id, modelo);
        this.capacidadBateria = capacidadBateria;
        this.nivelBateria = 100.0f; // Inicia con bateria completa
    }
    
    public float getCapacidadBateria() {
        return capacidadBateria;
    }
    
    public void setCapacidadBateria(float capacidadBateria) {
        this.capacidadBateria = capacidadBateria;
    }
    
    public float getNivelBateria() {
        return nivelBateria;
    }
    
    public void setNivelBateria(float nivelBateria) {
        this.nivelBateria = Math.max(0, Math.min(100, nivelBateria));
    }
    
    /**
     * Metodo abstracto para cargar el vehiculo
     */
    public abstract void cargar();
}

/**
 * Clase concreta para vanes electricas
 */
class Van extends VehiculoElectrico {
    
    public Van(int id, String modelo, float capacidadBateria) {
        super(id, modelo, capacidadBateria);
    }
    
    @Override
    public void asignarRuta(Ruta ruta) {
        if (this.estado == EstadoVehiculo.DISPONIBLE) {
            this.estado = EstadoVehiculo.EN_RUTA;
            System.out.println("Van #" + id + " asignada a ruta: " + ruta.getId());
        } else {
            System.out.println("Van #" + id + " no esta disponible. Estado actual: " + estado);
        }
    }
    
    @Override
    public void cargar() {
        this.estado = EstadoVehiculo.CARGANDO;
        this.nivelBateria = 100.0f;
        System.out.println("Van #" + id + " cargandose...");
    }
}

/**
 * Clase concreta para bicicletas electricas
 */
class BicicletaElectrica extends VehiculoElectrico {
    
    public BicicletaElectrica(int id, String modelo, float capacidadBateria) {
        super(id, modelo, capacidadBateria);
    }
    
    @Override
    public void asignarRuta(Ruta ruta) {
        if (this.estado == EstadoVehiculo.DISPONIBLE) {
            this.estado = EstadoVehiculo.EN_RUTA;
            System.out.println("Bicicleta #" + id + " asignada a ruta: " + ruta.getId());
        } else {
            System.out.println("Bicicleta #" + id + " no esta disponible. Estado actual: " + estado);
        }
    }
    
    @Override
    public void cargar() {
        this.estado = EstadoVehiculo.CARGANDO;
        this.nivelBateria = 100.0f;
        System.out.println("Bicicleta #" + id + " cargandose...");
    }
}

/**
 * Clase concreta para motos electricas
 */
class MotoElectrica extends VehiculoElectrico {
    
    public MotoElectrica(int id, String modelo, float capacidadBateria) {
        super(id, modelo, capacidadBateria);
    }
    
    @Override
    public void asignarRuta(Ruta ruta) {
        if (this.estado == EstadoVehiculo.DISPONIBLE) {
            this.estado = EstadoVehiculo.EN_RUTA;
            System.out.println("Moto #" + id + " asignada a ruta: " + ruta.getId());
        } else {
            System.out.println("Moto #" + id + " no esta disponible. Estado actual: " + estado);
        }
    }
    
    @Override
    public void cargar() {
        this.estado = EstadoVehiculo.CARGANDO;
        this.nivelBateria = 100.0f;
        System.out.println("Moto #" + id + " cargandose...");
    }
}

/**
 * Clase para almacenar datos de telemetria
 */
class Telemetria {
    private int id;
    private java.time.LocalDateTime fechaHora;
    private float velocidad;
    private float temperaturaMotor;
    private String ubicacionGps;
    private float nivelBateria;
    private int vehiculoId;
    
    public Telemetria(int id, float velocidad, float temperaturaMotor, 
                      String ubicacionGps, float nivelBateria, int vehiculoId) {
        this.id = id;
        this.fechaHora = java.time.LocalDateTime.now();
        this.velocidad = velocidad;
        this.temperaturaMotor = temperaturaMotor;
        this.ubicacionGps = ubicacionGps;
        this.nivelBateria = nivelBateria;
        this.vehiculoId = vehiculoId;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public java.time.LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(java.time.LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public float getVelocidad() { return velocidad; }
    public void setVelocidad(float velocidad) { this.velocidad = velocidad; }
    
    public float getTemperaturaMotor() { return temperaturaMotor; }
    public void setTemperaturaMotor(float temperaturaMotor) { this.temperaturaMotor = temperaturaMotor; }
    
    public String getUbicacionGps() { return ubicacionGps; }
    public void setUbicacionGps(String ubicacionGps) { this.ubicacionGps = ubicacionGps; }
    
    public float getNivelBateria() { return nivelBateria; }
    public void setNivelBateria(float nivelBateria) { this.nivelBateria = nivelBateria; }
    
    public int getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(int vehiculoId) { this.vehiculoId = vehiculoId; }
    
    @Override
    public String toString() {
        return String.format("Telemetria[id=%d, vehiculo=%d, bateria=%.1f%%, temp=%.1f°C, vel=%.1fkm/h, gps=%s]",
                           id, vehiculoId, nivelBateria, temperaturaMotor, velocidad, ubicacionGps);
    }
}

/**
 * Clase para representar una entrega
 */
class Entrega {
    private int id;
    private String direccion;
    private java.time.LocalDateTime horaEstimada;
    private String estado; // "pendiente", "completada", "fallida"
    
    public Entrega(int id, String direccion, java.time.LocalDateTime horaEstimada) {
        this.id = id;
        this.direccion = direccion;
        this.horaEstimada = horaEstimada;
        this.estado = "pendiente";
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public java.time.LocalDateTime getHoraEstimada() { return horaEstimada; }
    public void setHoraEstimada(java.time.LocalDateTime horaEstimada) { this.horaEstimada = horaEstimada; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

/**
 * Clase para representar una ruta de entregas
 */
class Ruta {
    private int id;
    private String origen;
    private String destino;
    private float distancia;
    private java.util.List<Entrega> entregas;
    
    public Ruta(int id, String origen, String destino, float distancia) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.entregas = new java.util.ArrayList<>();
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    
    public float getDistancia() { return distancia; }
    public void setDistancia(float distancia) { this.distancia = distancia; }
    
    public java.util.List<Entrega> getEntregas() { return entregas; }
    
    public void agregarEntrega(Entrega entrega) {
        this.entregas.add(entrega);
    }
}

// ============================================================================
// GESTORES Y SERVICIOS
// ============================================================================

/**
 * Gestor principal de la flota de vehiculos
 */
class GestorFlota {
    private java.util.Map<Integer, VehiculoElectrico> vehiculos;
    private java.util.Map<Integer, Ruta> rutas;
    private java.util.List<Telemetria> historialTelemetria;
    private int contadorVehiculos;
    private int contadorRutas;
    private int contadorTelemetria;
    
    public GestorFlota() {
        this.vehiculos = new java.util.HashMap<>();
        this.rutas = new java.util.HashMap<>();
        this.historialTelemetria = new java.util.ArrayList<>();
        this.contadorVehiculos = 1;
        this.contadorRutas = 1;
        this.contadorTelemetria = 1;
    }
    
    // ====== CRUD de Vehiculos ======
    
    public VehiculoElectrico crearVehiculo(String tipo, String modelo, float capacidadBateria) {
        VehiculoElectrico vehiculo;
        int id = contadorVehiculos++;
        
        switch (tipo.toLowerCase()) {
            case "van":
                vehiculo = new Van(id, modelo, capacidadBateria);
                break;
            case "bicicleta":
                vehiculo = new BicicletaElectrica(id, modelo, capacidadBateria);
                break;
            case "moto":
                vehiculo = new MotoElectrica(id, modelo, capacidadBateria);
                break;
            default:
                throw new IllegalArgumentException("Tipo de vehiculo no valido: " + tipo);
        }
        
        vehiculos.put(id, vehiculo);
        System.out.println("Vehiculo creado: " + tipo + " #" + id);
        return vehiculo;
    }
    
    public VehiculoElectrico consultarVehiculo(int id) {
        return vehiculos.get(id);
    }
    
    public java.util.Collection<VehiculoElectrico> listarVehiculos() {
        return vehiculos.values();
    }
    
    public boolean actualizarVehiculo(int id, String nuevoModelo) {
        VehiculoElectrico vehiculo = vehiculos.get(id);
        if (vehiculo != null) {
            vehiculo.setModelo(nuevoModelo);
            System.out.println("Vehiculo #" + id + " actualizado");
            return true;
        }
        return false;
    }
    
    public boolean eliminarVehiculo(int id) {
        if (vehiculos.remove(id) != null) {
            System.out.println("Vehiculo #" + id + " eliminado");
            return true;
        }
        return false;
    }
    
    // ====== Gestion de Telemetria ======
    
    public Telemetria registrarTelemetria(int vehiculoId, float velocidad, 
                                         float temperaturaMotor, String ubicacionGps) {
        VehiculoElectrico vehiculo = vehiculos.get(vehiculoId);
        if (vehiculo == null) {
            throw new IllegalArgumentException("Vehiculo no encontrado: " + vehiculoId);
        }
        
        Telemetria telemetria = new Telemetria(
            contadorTelemetria++,
            velocidad,
            temperaturaMotor,
            ubicacionGps,
            vehiculo.getNivelBateria(),
            vehiculoId
        );
        
        historialTelemetria.add(telemetria);
        
        // Simular desgaste de bateria basado en velocidad
        if (vehiculo.getEstado() == EstadoVehiculo.EN_RUTA) {
            float desgaste = velocidad * 0.01f;
            vehiculo.setNivelBateria(vehiculo.getNivelBateria() - desgaste);
        }
        
        return telemetria;
    }
    
    public java.util.List<Telemetria> consultarHistorialTelemetria(int vehiculoId) {
        java.util.List<Telemetria> historial = new java.util.ArrayList<>();
        for (Telemetria t : historialTelemetria) {
            if (t.getVehiculoId() == vehiculoId) {
                historial.add(t);
            }
        }
        return historial;
    }
    
    public Telemetria obtenerUltimaTelemetria(int vehiculoId) {
        Telemetria ultima = null;
        for (Telemetria t : historialTelemetria) {
            if (t.getVehiculoId() == vehiculoId) {
                ultima = t;
            }
        }
        return ultima;
    }
    
    // ====== Gestion de Rutas ======
    
    public Ruta crearRuta(String origen, String destino, float distancia) {
        int id = contadorRutas++;
        Ruta ruta = new Ruta(id, origen, destino, distancia);
        rutas.put(id, ruta);
        System.out.println("Ruta creada #" + id + ": " + origen + " -> " + destino);
        return ruta;
    }
    
    public boolean asignarRutaAVehiculo(int vehiculoId, int rutaId) {
        VehiculoElectrico vehiculo = vehiculos.get(vehiculoId);
        Ruta ruta = rutas.get(rutaId);
        
        if (vehiculo == null || ruta == null) {
            System.out.println("Vehiculo o ruta no encontrados");
            return false;
        }
        
        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
            System.out.println("Vehiculo no disponible");
            return false;
        }
        
        vehiculo.asignarRuta(ruta);
        return true;
    }
    
    // ====== Gestion de Estados ======
    
    public boolean cambiarEstadoVehiculo(int vehiculoId, EstadoVehiculo nuevoEstado) {
        VehiculoElectrico vehiculo = vehiculos.get(vehiculoId);
        if (vehiculo != null) {
            vehiculo.setEstado(nuevoEstado);
            System.out.println("Vehiculo #" + vehiculoId + " cambio a estado: " + nuevoEstado);
            return true;
        }
        return false;
    }
    
    public java.util.List<VehiculoElectrico> obtenerVehiculosDisponibles() {
        java.util.List<VehiculoElectrico> disponibles = new java.util.ArrayList<>();
        for (VehiculoElectrico v : vehiculos.values()) {
            if (v.getEstado() == EstadoVehiculo.DISPONIBLE) {
                disponibles.add(v);
            }
        }
        return disponibles;
    }
    
    // ====== Estadisticas ======
    
    public void mostrarEstadisticas() {
        System.out.println("\n=== ESTADiSTICAS DE LA FLOTA ===");
        System.out.println("Total de vehiculos: " + vehiculos.size());
        System.out.println("Vehiculos disponibles: " + obtenerVehiculosDisponibles().size());
        System.out.println("Rutas creadas: " + rutas.size());
        System.out.println("Registros de telemetria: " + historialTelemetria.size());
        System.out.println("================================\n");
    }
}

/**
 * Simulador de telemetria que genera datos periodicamente
 */
class SimuladorTelemetria implements Runnable {
    private GestorFlota gestorFlota;
    private boolean activo;
    private int intervaloSegundos;
    
    public SimuladorTelemetria(GestorFlota gestorFlota, int intervaloSegundos) {
        this.gestorFlota = gestorFlota;
        this.intervaloSegundos = intervaloSegundos;
        this.activo = false;
    }
    
    public void iniciar() {
        this.activo = true;
        Thread hilo = new Thread(this);
        hilo.start();
        System.out.println("Simulador de telemetria iniciado (intervalo: " + intervaloSegundos + "s)");
    }
    
    public void detener() {
        this.activo = false;
        System.out.println("Simulador de telemetria detenido");
    }
    
    @Override
    public void run() {
        while (activo) {
            try {
                // Generar telemetria para cada vehiculo
                for (VehiculoElectrico vehiculo : gestorFlota.listarVehiculos()) {
                    generarTelemetriaAleatoria(vehiculo);
                }
                
                Thread.sleep(intervaloSegundos * 1000);
            } catch (InterruptedException e) {
                System.out.println("Simulador interrumpido");
                break;
            }
        }
    }
    
    private void generarTelemetriaAleatoria(VehiculoElectrico vehiculo) {
        java.util.Random random = new java.util.Random();
        
        // Generar datos aleatorios pero realistas
        float velocidad = vehiculo.getEstado() == EstadoVehiculo.EN_RUTA ? 
                         20 + random.nextFloat() * 40 : 0;
        float temperatura = 30 + random.nextFloat() * 50;
        String gps = String.format("%.6f,%.6f", 
                                  20.5 + random.nextFloat() * 0.1,
                                  -100.3 + random.nextFloat() * 0.1);
        
        gestorFlota.registrarTelemetria(vehiculo.getId(), velocidad, temperatura, gps);
    }
}

// ============================================================================
// CLASE PRINCIPAL DE PRUEBA
// ============================================================================

public class Sigefve {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GESTIoN DE FLOTA DE VEHiCULOS ELeCTRICOS ===\n");
        
        // Crear gestor de flota
        GestorFlota gestor = new GestorFlota();
        
        // Crear vehiculos
        System.out.println("--- Creando vehiculos ---");
        gestor.crearVehiculo("van", "Ford E-Transit", 67.0f);
        gestor.crearVehiculo("van", "Mercedes eSprinter", 55.0f);
        gestor.crearVehiculo("bicicleta", "Specialized Turbo", 0.5f);
        gestor.crearVehiculo("bicicleta", "Trek Allant+", 0.6f);
        gestor.crearVehiculo("moto", "Zero SR/F", 14.4f);
        gestor.crearVehiculo("moto", "Energica Ego", 21.5f);
        
        // Crear rutas
        System.out.println("\n--- Creando rutas ---");
        Ruta ruta1 = gestor.crearRuta("Centro", "Zona Norte", 15.5f);
        ruta1.agregarEntrega(new Entrega(1, "Calle Principal 123", 
                           java.time.LocalDateTime.now().plusHours(1)));
        
        Ruta ruta2 = gestor.crearRuta("Centro", "Zona Sur", 12.3f);
        ruta2.agregarEntrega(new Entrega(2, "Avenida Reforma 456", 
                           java.time.LocalDateTime.now().plusHours(2)));
        
        // Asignar rutas
        System.out.println("\n--- Asignando rutas ---");
        gestor.asignarRutaAVehiculo(1, 1);
        gestor.asignarRutaAVehiculo(3, 2);
        
        // Registrar telemetria manual
        System.out.println("\n--- Registrando telemetria ---");
        gestor.registrarTelemetria(1, 45.5f, 55.2f, "20.523456,-100.345678");
        gestor.registrarTelemetria(3, 25.0f, 40.1f, "20.534567,-100.356789");
        
        // Mostrar estadisticas
        gestor.mostrarEstadisticas();
        
        // Listar vehiculos
        System.out.println("--- Estado de vehiculos ---");
        for (VehiculoElectrico v : gestor.listarVehiculos()) {
            System.out.printf("Vehiculo #%d - Modelo: %s, Estado: %s, Bateria: %.1f%%\n",
                            v.getId(), v.getModelo(), v.getEstado(), v.getNivelBateria());
        }
        
        // Iniciar simulador de telemetria (opcional)
        System.out.println("\n--- Iniciando simulador (5 segundos) ---");
        SimuladorTelemetria simulador = new SimuladorTelemetria(gestor, 5);
        simulador.iniciar();
        
        // Ejecutar por 20 segundos y luego detener
        try {
            Thread.sleep(20000);
            simulador.detener();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Mostrar historial de telemetria
        System.out.println("\n--- Historial de telemetria del vehiculo #1 ---");
        java.util.List<Telemetria> historial = gestor.consultarHistorialTelemetria(1);
        for (Telemetria t : historial) {
            System.out.println(t);
        }
        
        // Estadisticas finales
        gestor.mostrarEstadisticas();
        
        System.out.println("\n=== FIN DEL PROGRAMA ===");
    }
}
