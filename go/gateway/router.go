package gateway

import (
	"go-gin-gateway/auth"
	"go-gin-gateway/middleware"
	"net/http"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

// URLs de los microservicios
const (
	pythonServiceURL = "http://149.202.215.39:8822"
	javaServiceURL   = "http://149.202.215.39:8585"
)

func PrepararRutas() *gin.Engine {
	r := gin.Default()

	r.Use(middleware.LogPeticiones())

	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"}, // tu frontend real
		AllowMethods:     []string{"POST", "GET", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: false,
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
		user, err := auth.RegistrarUsuario(
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
		user, err := auth.Login(credenciales.Usuario, credenciales.Contrasenia)

		// 2. Manejar error de autenticación
		// Si el login falla (credenciales inválidas o no encontrado)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Usuario o contraseña incorrectos"})
			return
		}

		// Si el login es exitoso, generar token

		// 3. Generar el JWT
		tokenModel, tokenErr := auth.GenerarJWT(user.Email, user.ID, user.Rol)

		if tokenErr != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo generar el token"})
			return
		}

		// 3. Devolver la string del token
		c.JSON(http.StatusOK, gin.H{
			"token":   tokenModel.TokenJWT,
			"user_id": user.ID,
			"rol":     user.Rol,
		})
	})

	// Endpoint de health check
	r.GET("/health", func(c *gin.Context) {
		client := &http.Client{
			Timeout: 5 * time.Second,
		}

		// Verificar estado de Go (siempre UP si llegamos aquí)
		goStatus := "UP"

		// Verificar estado de Python
		pythonStatus := checkServiceHealth(client, pythonServiceURL+"/health")

		// Verificar estado de Java
		javaStatus := checkServiceHealth(client, javaServiceURL+"/health")

		// Determinar estado general
		overallStatus := "UP"
		if pythonStatus == "DOWN" || javaStatus == "DOWN" {
			overallStatus = "DEGRADED"
		}
		if pythonStatus == "DOWN" && javaStatus == "DOWN" {
			overallStatus = "DOWN"
		}

		c.JSON(http.StatusOK, gin.H{
			"status": overallStatus,
			"services": gin.H{
				"go": gin.H{
					"status": goStatus,
				},
				"python": gin.H{
					"status": pythonStatus,
				},
				"java": gin.H{
					"status": javaStatus,
				},
			},
			"timestamp": time.Now().Format(time.RFC3339),
		})
	})

	// Rutas protegidas
	protegidas := r.Group("/")
	protegidas.Use(auth.JWTMiddleware())

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
			err := auth.RevocarToken(tokenString)

			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al revocar el token en la base de datos"})
				return
			}

			// 3. Respuesta exitosa
			c.JSON(http.StatusOK, gin.H{"mensaje": "Sesión cerrada con éxito. Token revocado."})
		})

		// Las rutas de proxy existentes ahora van dentro del grupo protegido:
		r.Any("/python/*path", ReverseProxy(pythonServiceURL)) // Ya no requiere el middleware aquí
		r.Any("/java/*path", ReverseProxy(javaServiceURL))     // Ya no requiere el middleware aquí
	}

	return r
}

// checkServiceHealth verifica el estado de un servicio
func checkServiceHealth(client *http.Client, url string) string {
	resp, err := client.Get(url)
	if err != nil {
		return "DOWN"
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusOK {
		return "UP"
	}
	return "DOWN"
}
