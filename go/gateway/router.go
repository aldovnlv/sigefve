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

	r.SetTrustedProxies([]string{"172.16.0.0/12", "192.168.0.0/16"})

	r.Use(middleware.RateLimitMiddleware())
	r.Use(middleware.LogPeticiones())

	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"POST", "GET", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))

	// Endpoint para registrar un nuevo usuario (público)
	r.POST("/register", func(c *gin.Context) {
		var nuevoUsuario struct {
			Nombre      string `json:"nombre" binding:"required"`
			Email       string `json:"email" binding:"required,email"`
			Contrasenia string `json:"password" binding:"required,min=6"`
			Rol         string `json:"rol"`
		}

		// 1. Validar y bindear el JSON de entrada
		if err := c.ShouldBindJSON(&nuevoUsuario); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Datos de entrada inválidos", "detalle": err.Error()})
			return
		}

		// Si el rol viene vacío, se asume que es 'Conductor' por defecto
		if nuevoUsuario.Rol == "" {
			nuevoUsuario.Rol = "Conductor"
		}

		// 2. Registrar el usuario en la BD
		user, err := autenticador.RegistrarUsuario(
			nuevoUsuario.Nombre,
			nuevoUsuario.Email,
			nuevoUsuario.Contrasenia,
			nuevoUsuario.Rol,
		)

		// 3. Manejar errores
		if err != nil {
			status := http.StatusInternalServerError
			if err.Error() == "el email ya está registrado" {
				status = http.StatusConflict // 409 Conflict
			}
			c.JSON(status, gin.H{"error": "Error al registrar usuario", "detalle": err.Error()})
			return
		}

		// 4. Respuesta exitosa
		c.JSON(http.StatusCreated, gin.H{
			"mensaje": "Usuario creado exitosamente",
			"id":      user.ID,
			"email":   user.Email,
			"rol":     user.Rol,
		})
	})

	// Endpoint para iniciar sesión
	r.POST("/login", func(c *gin.Context) {
		var credenciales struct {
			Usuario     string `json:"username"`
			Contrasenia string `json:"password"`
		}

		if err := c.ShouldBindJSON(&credenciales); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "JSON inválido"})
			return
		}

		// 1. Llamar a la función Login
		user, err := autenticador.Login(credenciales.Usuario, credenciales.Contrasenia)

		// 2. Manejar error de autenticación
		// Si el login falla (credenciales inválidas o no encontrado)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Usuario o contraseña incorrectos"})
			return
		}

		// Si el login es exitoso, generar token

		// 3. Generar el JWT
		jwt, tokenErr := autenticador.GenerarJWT(user.Email, user.ID, user.Rol)

		if tokenErr != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo generar el token"})
			return
		}

		// 3. Devolver la string del token
		c.JSON(http.StatusOK, gin.H{
			"token":   jwt.TokenJWT,
			"user_id": user.ID,
			"rol":     user.Rol,
		})
	})

	// Endpoint de health check
	r.GET("/health", func(c *gin.Context) {
		cliente := &http.Client{
			Timeout: 5 * time.Second,
		}

		// Verificar estado de Go (siempre ARRIBA si llegamos aquí)
		goStatus := "ARRIBA"

		// Verificar estado de Python
		pythonSalud := revisarSalud(cliente, pythonURL+"/health")

		// Verificar estado de Java
		javaSalud := revisarSalud(cliente, javaURL+"/health")

		// Determinar estado general
		overallStatus := "ARRIBA"
		if pythonSalud == "CAÍDO" || javaSalud == "CAÍDO" {
			overallStatus = "DEGRADED"
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

	// Rutas protegidas
	protegidas := r.Group("/")
	protegidas.Use(autenticador.JWTMiddleware())

	{
		// Endpoint para cerrar sesión (Logout)
		protegidas.POST("/logout", func(c *gin.Context) {
			// 1. Obtener el token del contexto (guardado por JWTMiddleware)
			tokenAny, exists := c.Get("token")
			if !exists {
				// Esto no debería suceder si el middleware se ejecuta correctamente
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Token no encontrado en el contexto"})
				return
			}
			tokenString, ok := tokenAny.(string)
			if !ok {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error interno al procesar el token"})
				return
			}

			// 2. Llamar a la función de revocación en la BD
			err := autenticador.RevocarToken(tokenString)

			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al revocar el token en la base de datos"})
				return
			}

			// 3. Respuesta exitosa
			c.JSON(http.StatusOK, gin.H{"mensaje": "Sesión cerrada con éxito. Token revocado."})
		})

		protegidas.Any("/python/*path", ReverseProxy(pythonURL))
		protegidas.Any("/java/*path", ReverseProxy(javaURL))
	}

	// Rutas protegidas para administradores
	admin := r.Group("/admin")
	admin.Use(autenticador.JWTMiddleware())
	admin.Use(autenticador.AutorizarRol("Administrador"))

	{
		// Endpoint para ver el estado de Rate Limit (LimiteIP)
		admin.GET("/rate-limit", func(c *gin.Context) {
			// Obtener el parámetro 'limit' (opcional)
			limitStr := c.DefaultQuery("limit", "50")
			limit, err := strconv.Atoi(limitStr)
			if err != nil {
				limit = 50 // Usar el valor por defecto si la conversión falla
			}

			limites, err := database.ObtenerLimitesIP(limit)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error consultando LimitesIP", "detalle": err.Error()})
				return
			}

			c.JSON(http.StatusOK, limites)
		})

		// Endpoint para ver el Log de Peticiones (LogPeticion)
		admin.GET("/logs", func(c *gin.Context) {
			// Obtener el parámetro 'limit' (opcional)
			limiteStr := c.DefaultQuery("limit", "100")
			limite, err := strconv.Atoi(limiteStr)
			if err != nil {
				limite = 100 // Usar el valor por defecto si la conversión falla
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

// revisarSalud verifica el estado de un servicio
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
