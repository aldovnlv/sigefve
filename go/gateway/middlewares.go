package gateway

import (
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/time/rate"

	"log"
)

var claveJWT = []byte("mi_clave_super_secreta")

// Rate limitador: 3 requests por segundo
var limitador = rate.NewLimiter(3, 3)


func CORSMiddleware() gin.HandlerFunc {
    return func(c *gin.Context) {
        c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
        c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
        c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
        c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT")

        if c.Request.Method == "OPTIONS" {
            c.AbortWithStatus(204)
            return
        }

        c.Next()
    }
}


// JWT Middleware
func ConectorAutenticacion() gin.HandlerFunc {
	return func(c *gin.Context) {
		encabezadoAutenticacion := c.GetHeader("Authorization")
		if encabezadoAutenticacion == "" || !strings.HasPrefix(encabezadoAutenticacion, "Bearer ") {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token no proporcionado"})
			c.Abort()
			return
		}

		cadenaToken := strings.TrimPrefix(encabezadoAutenticacion, "Bearer ")
		token, err := jwt.Parse(cadenaToken, func(token *jwt.Token) (interface{}, error) {
			return claveJWT, nil
		})

		if err != nil || !token.Valid {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token inválido o expirado"})
			c.Abort()
			return
		}

		c.Next()
	}
}

// Middleware para limitar las solicitudes
func ConectorLimiteAcciones() gin.HandlerFunc {
	return func(c *gin.Context) {
		if !limitador.Allow() {
			c.JSON(http.StatusTooManyRequests, gin.H{"error": "Demasiadas solicitudes, intenta de nuevo"})
			c.Abort()
			return
		}
		c.Next()
	}
}

// Imprimir mensajes a consola
func ConectorAcceso() gin.HandlerFunc {
	return func(c *gin.Context) {
		inicio := time.Now()
		c.Next()
		duracion := time.Since(inicio)
		metodo := c.Request.Method
		ruta := c.Request.URL.Path
		stado := c.Writer.Status()
		log.Printf("[%s] %s - %d (%v)", metodo, ruta, stado, duracion)
	}
}
