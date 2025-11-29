package auth

import (
	"log"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

// Middleware JWT para proteger rutas
func JWTMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		encabezadoAutenticacion := c.GetHeader("Authorization")

		if encabezadoAutenticacion == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token faltante"})
			c.Abort()
			return
		}

		// Formato esperado: Bearer <token>
		cadenaToken := strings.TrimPrefix(encabezadoAutenticacion, "Bearer ")

		if cadenaToken == encabezadoAutenticacion {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Formato incorrecto en header Authorization"})
			c.Abort()
			return
		}

		token, err := ValidarJWT(cadenaToken)
		if err != nil || !token.Valid {
			log.Println("Token inválido:", err)
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token inválido"})
			c.Abort()
			return
		}

		peticion := token.Claims.(jwt.MapClaims)
		c.Set("user", peticion["username"])

		c.Next()
	}
}
