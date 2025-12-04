// gateway/router.go

package gateway

import (
	"go-gin-gateway/auth"
	"go-gin-gateway/database"
	"go-gin-gateway/middleware"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

// URL de los microservicios
const (
	pythonURL = "http://sigefve-python:5001"
	javaURL   = "http://sigefve_java:8585"
)

type Router struct{}

func (router Router) PrepararRutas() *gin.Engine {
	autenticador := auth.Autenticador{}

	r := gin.Default()

	// Configura rangos privados como proxies confiables para permitir
	// que ClientIP() extraiga correctamente la IP real desde headers X-Forwarded-For
	// en despliegues tras balanceadores de carga o proxies inversos
	r.SetTrustedProxies([]string{"172.16.0.0/12", "192.168.0.0/16"})

	// RateLimit primero para rechazar peticiones
	// abusivas antes de procesamiento costoso (logging, autenticación)
	r.Use(middleware.RateLimitMiddleware())
	r.Use(middleware.LogPeticiones())

	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"POST", "GET", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: false,
		MaxAge:           12 * time.Hour,
	}))

	r.POST("/register", func(c *gin.Context) {
		var nuevoUsuario struct {
			Nombre      string `json:"nombre" binding:"required"`
			Email       string `json:"email" binding:"required,email"`
			Contrasenia string `json:"password" binding:"required,min=6"`
			Rol         string `json:"rol"`
		}

		if err := c.ShouldBindJSON(&nuevoUsuario); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Datos de entrada inválidos", "detalle": err.Error()})
			return
		}

		if nuevoUsuario.Rol == "" {
			nuevoUsuario.Rol = "Conductor"
		}

		user, err := autenticador.RegistrarUsuario(
			nuevoUsuario.Nombre,
			nuevoUsuario.Email,
			nuevoUsuario.Contrasenia,
			nuevoUsuario.Rol,
		)

		if err != nil {
			// Diferenciación semántica de errores mediante códigos HTTP apropiados:
			// 409 Conflict para violación de unicidad vs 500 para fallas sistémicas
			status := http.StatusInternalServerError
			if err.Error() == "el email ya está registrado" {
				status = http.StatusConflict
			}
			c.JSON(status, gin.H{"error": "Error al registrar usuario", "detalle": err.Error()})
			return
		}

		c.JSON(http.StatusCreated, gin.H{
			"mensaje": "Usuario creado exitosamente",
			"id":      user.ID,
			"email":   user.Email,
			"rol":     user.Rol,
		})
	})

	r.POST("/login", func(c *gin.Context) {
		var credenciales struct {
			Usuario     string `json:"username"`
			Contrasenia string `json:"password"`
		}

		if err := c.ShouldBindJSON(&credenciales); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "JSON inválido"})
			return
		}

		user, err := autenticador.Login(credenciales.Usuario, credenciales.Contrasenia)

		// Respuesta genérica ante fallo de autenticación: evita enumeración
		// de usuarios (no revela si el usuario existe o si la contraseña es incorrecta)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Usuario o contraseña incorrectos"})
			return
		}

		jwt, tokenErr := autenticador.GenerarJWT(user.Email, user.ID, user.Rol)

		if tokenErr != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo generar el token"})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"token":   jwt.TokenJWT,
			"user_id": user.ID,
			"rol":     user.Rol,
		})
	})

	// Verificación de salud: Permite monitoreo de disponibilidad parcial
	// en arquitecturas distribuidas evitando falsos negativos tipo "todo o nada"
	r.GET("/health", func(c *gin.Context) {
		cliente := &http.Client{
			Timeout: 5 * time.Second,
		}

		goStatus := "ARRIBA"

		pythonSalud := revisarSalud(cliente, pythonURL+"/health")

		javaSalud := revisarSalud(cliente, javaURL+"/health")

		// Lógica de estado agregado con tres niveles de granularidad:
		// ARRIBA (todos funcionales), PARCIAL, CAÍDO (ninguno)
		overallStatus := "ARRIBA"
		if pythonSalud == "CAÍDO" || javaSalud == "CAÍDO" {
			overallStatus = "PARCIAL"
		}
		if pythonSalud == "CAÍDO" && javaSalud == "CAÍDO" {
			overallStatus = "CAÍDO"
		}

		c.JSON(http.StatusOK, gin.H{
			"status": overallStatus,
			"services": gin.H{
				"go": gin.H{
					"status": goStatus,
				},
				"python": gin.H{
					"status": pythonSalud,
				},
				"java": gin.H{
					"status": javaSalud,
				},
			},
			"timestamp": time.Now().Format(time.RFC3339),
		})
	})

	// Patrón de agrupación con middleware en cadena: todas las rutas bajo
	// protegidas usarán JWTMiddleware, implementando autorización centralizada
	protegidas := r.Group("/")
	protegidas.Use(autenticador.JWTMiddleware())

	{
		protegidas.POST("/logout", func(c *gin.Context) {
			// Extracción de token desde contexto de Gin: el middleware previo
			// almacena el token parseado evitando re-parseo y validación duplicada
			tokenAny, exists := c.Get("token")
			if !exists {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Token no encontrado en el contexto"})
				return
			}
			tokenString, ok := tokenAny.(string)
			if !ok {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error interno al procesar el token"})
				return
			}

			// Revocación mediante lista negra persistente: implementa invalidación
			// de tokens antes de expiración natural
			err := autenticador.RevocarToken(tokenString)

			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al revocar el token en la base de datos"})
				return
			}

			c.JSON(http.StatusOK, gin.H{"mensaje": "Sesión cerrada con éxito. Token revocado."})
		})

		// Proxy inverso: reescribe rutas preservando path original mediante
		// wildcard (*), delegando enrutamiento específico a microservicios
		protegidas.Any("/python/*path", ReverseProxy(pythonURL))
		protegidas.Any("/java/*path", ReverseProxy(javaURL))
	}

	// Control de acceso basado en roles con 2 middlewares:
	// autenticación (JWT) + autorización (rol específico)
	admin := r.Group("/admin")
	admin.Use(autenticador.JWTMiddleware())
	admin.Use(autenticador.AutorizarRol("Administrador"))

	{
		admin.GET("/rate-limit", func(c *gin.Context) {
			// Previene sobrecarga de memoria al transferir conjuntos masivos de datos
			limitStr := c.DefaultQuery("limit", "50")
			limit, err := strconv.Atoi(limitStr)
			if err != nil {
				limit = 50
			}

			limites, err := database.ObtenerLimitesIP(limit)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error consultando LimitesIP", "detalle": err.Error()})
				return
			}

			c.JSON(http.StatusOK, limites)
		})

		admin.GET("/logs", func(c *gin.Context) {
			limiteStr := c.DefaultQuery("limit", "100")
			limite, err := strconv.Atoi(limiteStr)
			if err != nil {
				limite = 100
			}

			logs, err := database.ObtenerLogsPeticion(limite)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error consultando LogsPeticion", "detalle": err.Error()})
				return
			}

			c.JSON(http.StatusOK, logs)
		})
	}

	return r
}

func revisarSalud(client *http.Client, url string) string {
	resp, err := client.Get(url)
	if err != nil {
		return "CAÍDO"
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusOK {
		return "ARRIBA"
	}
	return "CAÍDO"
}
