package gateway

import (
	"go-gin-gateway/auth"
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/rs/cors"
)

// Mapa simple de usuarios válidos
var usuarios = map[string]string{
	"jessica": "1234",
	"aldo":    "5678",
}

func PrepararRutas() *gin.Engine {
	// Configuración CORS
	c := cors.New(cors.Options{
		AllowedOrigins:   []string{"https://example.com"}, // Aquí va tu dominio frontend
		AllowedMethods:   []string{"GET", "POST", "OPTIONS"}, // Métodos permitidos
		AllowedHeaders:   []string{"Content-Type", "Authorization"}, // Cabeceras permitidas
		AllowCredentials: true, // Permitir credenciales (cookies, encabezados)
	})

	r := gin.Default()

	r.Use(c)
	
	// Endpoint para iniciar sesión
	r.POST("/login", CORSMiddleware(), func(c *gin.Context) {
		var credenciales struct {
			Usuario     string `json:"username"`
			Contrasenia string `json:"password"`
		}
		if err := c.ShouldBindJSON(&credenciales); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "JSON inválido"})
			return
		}

		// Validar usuario y contraseña
		if clave, ok := usuarios[credenciales.Usuario]; !ok || clave != credenciales.Contrasenia {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Usuario o contraseña incorrectos"})
			return
		}

		// Si es correcto, generar token
		token := auth.GenerarJWT(credenciales.Usuario)
		c.JSON(http.StatusOK, gin.H{"token": token})
	})

	// Rutas protegidas
	r.Any("/python/*path", auth.JWTMiddleware(), CORSMiddleware(), ReverseProxy("http://149.202.215.39:8822"))
	r.Any("/java/*path", auth.JWTMiddleware(), CORSMiddleware(), ReverseProxy("http://149.202.215.39:8585"))

	return r
}
