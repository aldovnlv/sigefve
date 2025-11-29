package com.sigefve;

import com.sigefve.config.ConfiguracionBaseDatos;
import com.sigefve.controladores.ControladorInicio;
import com.sigefve.controladores.ControladorRutas;
import com.sigefve.controladores.ControladorTelemetria;
import com.sigefve.controladores.ControladorVehiculos;
import com.sigefve.simulador.SimuladorTelemetria;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServidorHTTP {
    private static final int PUERTO = Integer.parseInt(
        System.getenv().getOrDefault("JAVA_PORT", "8585")
    );
    
    private HttpServer servidor;
    private SimuladorTelemetria simulador;

    public void iniciar() throws Exception {
        System.out.println("Iniciando SIGEFVE - Modulo Java");
        System.out.println("=======================================");
        
        // Inicializar base de datos
        System.out.println("Conectando a PostgreSQL...");
        ConfiguracionBaseDatos.obtenerInstancia();
        
        // Crear servidor HTTP
        servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.setExecutor(Executors.newFixedThreadPool(10));
        
        // Registrar controladores
        servidor.createContext("/", new ControladorInicio());
        servidor.createContext("/vehiculos", new ControladorVehiculos());
        servidor.createContext("/telemetria", new ControladorTelemetria());
        servidor.createContext("/rutas", new ControladorRutas());
        
        // Health check
        servidor.createContext("/health", exchange -> {
            String respuesta = "{\"status\":\"OK\",\"servicio\":\"SIGEFVE-Java\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, respuesta.length());
            exchange.getResponseBody().write(respuesta.getBytes());
            exchange.getResponseBody().close();
        });
        

        servidor.createContext("/test", exchange -> {
            String respuesta = """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8" />
<title>SIGEFVE API Documentation</title>
<meta name=&quot;description&quot; content=&quot;SIGEFVE: SIstema de GEsti&oacute;n de Flota de Veh&iacute;culos El&eacute;ctricos (SIGEFVE) - Documentaci&oacute;n de API REST&quot;>
<meta property=&quot;og:title&quot; content=&quot;SIGEFVE API&quot;>
<meta property=&quot;og:description&quot; content=&quot;SIstema de GEsti&oacute;n de Flota de Veh&iacute;culos El&eacute;ctricos (SIGEFVE) - Documentaci&oacute;n de API REST&quot;>
<meta property=&quot;og:image&quot; content=&quot;/mnt/data/SIGEFVE.png&quot;>
<style>
body{font-family:Arial,Helvetica,sans-serif;background:#f6f7fb;margin:0;padding-top:60px}
.fixedtoc{position:fixed;top:0;left:0;right:0;background:#000;color:#fff;padding:10px;z-index:999;display:flex;gap:20px}
.fixedtoc a{color:#fff;text-decoration:none;font-weight:bold;font-size:14px}
h1{display:none}
.section{border-bottom:1px solid #ddd;padding:20px}
.endpoint{margin:10px 0;border-radius:6px;overflow:hidden;border-left:8px solid #888;background:#fff}
.endpoint summary{cursor:pointer;padding:12px 15px;font-weight:bold;font-size:15px}
.method{display:inline-block;padding:2px 6px;border-radius:4px;color:#fff;font-size:11px;margin-right:6px}
.GET{background:#2ecc71}.POST{background:#3498db}.PUT{background:#f39c12}.DELETE{background:#e74c3c}
.endpoint.GET{border-left-color:#2ecc71}.endpoint.POST{border-left-color:#3498db}.endpoint.PUT{border-left-color:#f39c12}.endpoint.DELETE{border-left-color:#e74c3c}
.desc{padding:0 15px 15px 15px}
pre{background:#222;color:#0f0;padding:10px;border-radius:6px;font-size:12px;overflow:auto;position:relative}
.copybtn{position:absolute;top:5px;right:5px;font-size:10px;background:#555;color:#fff;border:none;padding:3px 5px;cursor:pointer;border-radius:3px}
.baseurlbox{background:#fff;padding:10px;border-bottom:1px solid #ccc}
</style>
<script>
function cp(t){navigator.clipboard.writeText(t.innerText)}
</script>
</head>
<body>
<div class="fixedtoc">
<a href="#vehiculos">Veh&iacute;culos</a>
<a href="#telemetria">Telemetr&iacute;a</a>
<a href="#rutas">Rutas</a>
</div>
<div class="baseurlbox">Base URL: <input value="https://tajava.xipatlani.tk" style="width:250px"></div>
<div class="section" id="vehiculos">
<h2>Veh&iacute;culos</h2>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/vehiculos</summary><div class="desc">Obtener todos los veh&iacute;culos</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/vehiculos/:id</summary><div class="desc">Obtener un veh&iacute;culo por ID</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/vehiculos?estado=DISPONIBLE</summary><div class="desc">Filtrar veh&iacute;culos por estado</div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/vehiculos</summary><div class="desc">Crear veh&iacute;culo<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;tipo&quot;:&quot;VAN&quot;,
  &quot;placa&quot;:&quot;VAN-001&quot;,
  &quot;modelo&quot;:&quot;Mercedes eSprinter&quot;,
  &quot;anio&quot;:2023,
  &quot;capacidadBateria&quot;:90.0,
  &quot;autonomiaMaxima&quot;:150.0,
  &quot;capacidadCarga&quot;:1500.0,
  &quot;numeroAsientos&quot;:3
}</pre></div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/vehiculos/:id</summary><div class="desc">Actualizar un veh&iacute;culo (mismo body que POST)</div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/vehiculos/:id/estado</summary><div class="desc">Cambiar estado<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{&quot;estado&quot;:&quot;EN_RUTA&quot;}</pre></div></div>
<div class="endpoint DELETE"><summary><span class="method DELETE">DELETE</span>/vehiculos/:id</summary><div class="desc">Eliminar un veh&iacute;culo</div></div>
</div>
<div class="section" id="telemetria">
<h2>Telemetr&iacute;a</h2>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/telemetria/vehiculo/:id</summary><div class="desc">Historial (limite param opcional)</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/telemetria/vehiculo/:id/ultima</summary><div class="desc">&Uacute;ltima telemetr&iacute;a</div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/telemetria</summary><div class="desc">Registrar telemetr&iacute;a<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;vehiculoId&quot;:1,
  &quot;nivelBateria&quot;:75.5,
  &quot;latitud&quot;:20.5288,
  &quot;longitud&quot;:-100.8157,
  &quot;temperaturaMotor&quot;:45.2,
  &quot;velocidadActual&quot;:60.0,
  &quot;kilometrajeActual&quot;:1250.5
}</pre></div></div>
</div>
<div class="section" id="rutas">
<h2>Rutas</h2>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/rutas</summary><div class="desc">Rutas activas</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/rutas/:id</summary><div class="desc">Ruta por ID</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/rutas/:id/entregas</summary><div class="desc">Entregas de la ruta</div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/rutas</summary><div class="desc">Crear ruta<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;nombre&quot;:&quot;Entregas Zona Centro&quot;,
  &quot;distanciaTotal&quot;:15.5,
  &quot;vehiculoId&quot;:1
}</pre></div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/rutas/:id/entregas</summary><div class="desc">Agregar entrega<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;direccionDestino&quot;:&quot;Av. Ju&aacute;rez 123&quot;,
  &quot;latitud&quot;:20.5288,
  &quot;longitud&quot;:-100.8157,
  &quot;descripcionPaquete&quot;:&quot;Documentos importantes&quot;,
  &quot;pesoKg&quot;:2.5
}</pre></div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/rutas/:id/asignar</summary><div class="desc"><pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{&quot;vehiculoId&quot;:1}</pre></div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/rutas/:id/completar</summary><div class="desc">Completar ruta</div></div>
</div>
</body>
</html>

            """;
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, respuesta.length());
            exchange.getResponseBody().write(respuesta.getBytes());
            exchange.getResponseBody().close();
        });
        servidor.createContext("/api-cliente", exchange -> {
            String respuesta = """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8" />
<title>SIGEFVE API Documentation</title>
<meta name=&quot;description&quot; content=&quot;SIGEFVE: SIstema de GEsti&oacute;n de Flota de Veh&iacute;culos El&eacute;ctricos (SIGEFVE) - Documentaci&oacute;n de API REST&quot;>
<meta property=&quot;og:title&quot; content=&quot;SIGEFVE API&quot;>
<meta property=&quot;og:description&quot; content=&quot;SIstema de GEsti&oacute;n de Flota de Veh&iacute;culos El&eacute;ctricos (SIGEFVE) - Documentaci&oacute;n de API REST&quot;>
<meta property=&quot;og:image&quot; content=&quot;/mnt/data/SIGEFVE.png&quot;>
<style>
body{font-family:Arial,Helvetica,sans-serif;background:#f6f7fb;margin:0;padding-top:60px}
.fixedtoc{position:fixed;top:0;left:0;right:0;background:#000;color:#fff;padding:10px;z-index:999;display:flex;gap:20px}
.fixedtoc a{color:#fff;text-decoration:none;font-weight:bold;font-size:14px}
h1{display:none}
.section{border-bottom:1px solid #ddd;padding:20px}
.endpoint{margin:10px 0;border-radius:6px;overflow:hidden;border-left:8px solid #888;background:#fff}
.endpoint summary{cursor:pointer;padding:12px 15px;font-weight:bold;font-size:15px}
.method{display:inline-block;padding:2px 6px;border-radius:4px;color:#fff;font-size:11px;margin-right:6px}
.GET{background:#2ecc71}.POST{background:#3498db}.PUT{background:#f39c12}.DELETE{background:#e74c3c}
.endpoint.GET{border-left-color:#2ecc71}.endpoint.POST{border-left-color:#3498db}.endpoint.PUT{border-left-color:#f39c12}.endpoint.DELETE{border-left-color:#e74c3c}
.desc{padding:0 15px 15px 15px}
pre{background:#222;color:#0f0;padding:10px;border-radius:6px;font-size:12px;overflow:auto;position:relative}
.copybtn{position:absolute;top:5px;right:5px;font-size:10px;background:#555;color:#fff;border:none;padding:3px 5px;cursor:pointer;border-radius:3px}
.baseurlbox{background:#fff;padding:10px;border-bottom:1px solid #ccc}
</style>
<script>
function cp(t){navigator.clipboard.writeText(t.innerText)}
</script>
</head>
<body>
<div class="fixedtoc"><img src=&quot;/mnt/data/SIGEFVE.png&quot; style=&quot;height:28px;vertical-align:middle;margin-right:12px;&quot;>
<a href="#vehiculos">Veh&iacute;culos</a>
<a href="#telemetria">Telemetr&iacute;a</a>
<a href="#rutas">Rutas</a>
</div>
<div class="baseurlbox">Base URL: <input value="https://tajava.xipatlani.tk" style="width:250px"></div>
<div class="section" id="vehiculos">
<h2>Veh&iacute;culos</h2>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/vehiculos</summary><div class="desc">Obtener todos los veh&iacute;culos</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/vehiculos/:id</summary><div class="desc">Obtener un veh&iacute;culo por ID</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/vehiculos?estado=DISPONIBLE</summary><div class="desc">Filtrar veh&iacute;culos por estado</div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/vehiculos</summary><div class="desc">Crear veh&iacute;culo<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;tipo&quot;:&quot;VAN&quot;,
  &quot;placa&quot;:&quot;VAN-001&quot;,
  &quot;modelo&quot;:&quot;Mercedes eSprinter&quot;,
  &quot;anio&quot;:2023,
  &quot;capacidadBateria&quot;:90.0,
  &quot;autonomiaMaxima&quot;:150.0,
  &quot;capacidadCarga&quot;:1500.0,
  &quot;numeroAsientos&quot;:3
}</pre></div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/vehiculos/:id</summary><div class="desc">Actualizar un veh&iacute;culo (mismo body que POST)</div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/vehiculos/:id/estado</summary><div class="desc">Cambiar estado<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{&quot;estado&quot;:&quot;EN_RUTA&quot;}</pre></div></div>
<div class="endpoint DELETE"><summary><span class="method DELETE">DELETE</span>/vehiculos/:id</summary><div class="desc">Eliminar un veh&iacute;culo</div></div>
</div>
<div class="section" id="telemetria">
<h2>Telemetr&iacute;a</h2>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/telemetria/vehiculo/:id</summary><div class="desc">Historial (limite param opcional)</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/telemetria/vehiculo/:id/ultima</summary><div class="desc">&Uacute;ltima telemetr&iacute;a</div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/telemetria</summary><div class="desc">Registrar telemetr&iacute;a<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;vehiculoId&quot;:1,
  &quot;nivelBateria&quot;:75.5,
  &quot;latitud&quot;:20.5288,
  &quot;longitud&quot;:-100.8157,
  &quot;temperaturaMotor&quot;:45.2,
  &quot;velocidadActual&quot;:60.0,
  &quot;kilometrajeActual&quot;:1250.5
}</pre></div></div>
</div>
<div class="section" id="rutas">
<h2>Rutas</h2>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/rutas</summary><div class="desc">Rutas activas</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/rutas/:id</summary><div class="desc">Ruta por ID</div></div>
<div class="endpoint GET"><summary><span class="method GET">GET</span>/rutas/:id/entregas</summary><div class="desc">Entregas de la ruta</div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/rutas</summary><div class="desc">Crear ruta<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;nombre&quot;:&quot;Entregas Zona Centro&quot;,
  &quot;distanciaTotal&quot;:15.5,
  &quot;vehiculoId&quot;:1
}</pre></div></div>
<div class="endpoint POST"><summary><span class="method POST">POST</span>/rutas/:id/entregas</summary><div class="desc">Agregar entrega<pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{
  &quot;direccionDestino&quot;:&quot;Av. Ju&aacute;rez 123&quot;,
  &quot;latitud&quot;:20.5288,
  &quot;longitud&quot;:-100.8157,
  &quot;descripcionPaquete&quot;:&quot;Documentos importantes&quot;,
  &quot;pesoKg&quot;:2.5
}</pre></div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/rutas/:id/asignar</summary><div class="desc"><pre><button class="copybtn" onclick="cp(this.parentNode)">copiar</button>{&quot;vehiculoId&quot;:1}</pre></div></div>
<div class="endpoint PUT"><summary><span class="method PUT">PUT</span>/rutas/:id/completar</summary><div class="desc">Completar ruta</div></div>
</div>
</body>
</html>
            """;
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, respuesta.length());
            exchange.getResponseBody().write(respuesta.getBytes());
            exchange.getResponseBody().close();
        });
        
        servidor.start();
        System.out.println("Servidor HTTP iniciado en puerto " + PUERTO);
        
        // Iniciar simulador de telemetria
        simulador = new SimuladorTelemetria();
        simulador.iniciar();
        
        System.out.println("=======================================");
        System.out.println("SIGEFVE - Modulo Java en ejecucion");
        System.out.println("API disponible en: http://localhost:" + PUERTO);
        System.out.println("=======================================\n");
    }

    public void detener() {
        System.out.println("\n Deteniendo servidor...");
        if (simulador != null) {
            simulador.detener();
        }
        if (servidor != null) {
            servidor.stop(0);
        }
        System.out.println("Servidor detenido correctamente");
    }

    public static void main(String[] args) {
        ServidorHTTP servidor = new ServidorHTTP();
        
        // Manejar cierre graceful
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
        
        try {
            servidor.iniciar();
            Thread.currentThread().join(); // Mantener el servidor ejecutandose
        } catch (Exception e) {
            System.err.println(" Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}