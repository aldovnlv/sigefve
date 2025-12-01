package gateway

import (
	"go-gin-gateway/auth"
	"net/http"

	"github.com/gin-gonic/gin"
)

// Mapa simple de usuarios válidos
var usuarios = map[string]string{
	"jessica": "1234",
	"aldo":    "5678",
}

func PrepararRutas() *gin.Engine {
	r := gin.Default()

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
