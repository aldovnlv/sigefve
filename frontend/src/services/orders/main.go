// Microservicio de "orders" (órdenes)
// Expone endpoints simples que podrán ser llamados desde el API Gateway

package main

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// ------------------------------------------------------------
// Modelo de ejemplo de una orden
// ------------------------------------------------------------
type Order struct {
	ID          int    `json:"id"`
	Cliente     string `json:"cliente"`
	Descripcion string `json:"descripcion"`
	Estado      string `json:"estado"`
}

// Datos simulados
var ordenes = []Order{
	{ID: 1, Cliente: "Juan Perez", Descripcion: "Paquete 1", Estado: "Entregado"},
	{ID: 2, Cliente: "María López", Descripcion: "Paquete 2", Estado: "En ruta"},
	{ID: 3, Cliente: "Carlos Díaz", Descripcion: "Paquete 3", Estado: "Pendiente"},
}

func main() {

	// Crear router Gin
	r := gin.Default()

	// ------------------------------------------------------------
	// Ruta principal del servicio
	// ------------------------------------------------------------
	r.GET("/", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"servicio": "orders",
			"version":  "1.0",
			"status":   "OK",
		})
	})

	// ------------------------------------------------------------
	// Obtener todas las órdenes
	// GET /orders
	// ------------------------------------------------------------
	r.GET("/orders", func(c *gin.Context) {
		c.JSON(http.StatusOK, ordenes)
	})

	// ------------------------------------------------------------
	// Obtener una orden por ID
	// GET /orders/:id
	// ------------------------------------------------------------
	r.GET("/orders/:id", func(c *gin.Context) {
		id := c.Param("id")

		for _, o := range ordenes {
			if id == strconv.Itoa(o.ID) {
				c.JSON(http.StatusOK, o)
				return
			}
		}

		c.JSON(http.StatusNotFound, gin.H{"error": "Orden no encontrada"})
	})

	// ------------------------------------------------------------
	// Crear una nueva orden
	// POST /orders
	// ------------------------------------------------------------
	r.POST("/orders", func(c *gin.Context) {
		var nueva Order

		// Intentar parsear JSON enviado
		if err := c.ShouldBindJSON(&nueva); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "JSON inválido"})
			return
		}

		nueva.ID = len(ordenes) + 1
		ordenes = append(ordenes, nueva)

		c.JSON(http.StatusCreated, nueva)
	})

	// ------------------------------------------------------------
	// Arrancar servidor
	// ------------------------------------------------------------
	r.Run(":8082") // Puedes cambiar el puerto si es necesario
}
