package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func main() {
	r := gin.Default()

	// Endpoint de login (ejemplo)
	r.POST("/login", func(c *gin.Context) {
		var credentials struct {
			Usuario    string `json:"usuario"`
			Contrasena string `json:"contrasena"`
		}

		if err := c.BindJSON(&credentials); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "JSON inválido"})
			return
		}

		// Validación simple (puedes conectar a DB luego)
		if credentials.Usuario == "admin" && credentials.Contrasena == "1234" {
			c.JSON(http.StatusOK, gin.H{
				"mensaje": "Login exitoso",
				"token":   "token-falso-123",
			})
		} else {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Credenciales incorrectas"})
		}
	})

	// Endpoint de obtener usuarios (ejemplo)
	r.GET("/users", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"usuarios": []gin.H{
				{"id": 1, "nombre": "Juan"},
				{"id": 2, "nombre": "Maria"},
			},
		})
	})

	r.Run(":8081") // Puerto del microservicio USERS
}
