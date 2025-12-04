// main.go

package main

import (
	"go-gin-gateway/database"
	"go-gin-gateway/gateway"
	"log"
)

// Estructura auxiliar del servidor
type ServidorAPI struct {
	BaseDatos database.BaseDatos
	Router    gateway.Router
}

// Función para preparar e iniciar el servidor (gin.Engine)
func (servidor ServidorAPI) IniciarServicio() {
	// Verifica que la conexión al servidor funcione
	if err := servidor.BaseDatos.Conectar(servidor.BaseDatos.Config); err != nil {
		log.Fatalf("Error conectando a la base de datos: %v", err)
	}
	// defer hace que la función (Cerrar) se programe, pero no se ejecute:
	// Se ejecutará justo antes de que la función que la contiene termine
	// (en este caso, IniciarServicio)
	defer servidor.BaseDatos.Cerrar()

	// Comprueba que las migraciones se hagan sin problemas
	if err := servidor.BaseDatos.Migraciones(); err != nil {
		log.Fatalf("Error durante la migración de la base de datos: %v", err)
	}

	// Preparar y ejecutar el servidor
	r := servidor.Router.PrepararRutas()

	port := "8080"
	log.Printf("Servidor escuchando en el puerto %s", port)

	// Inicia el servidor en el puerto 8080
	if err := r.Run(":" + port); err != nil {
		log.Fatalf("Error iniciando servidor: %v", err)
	}
}

// Archivo de configuración de la base de datos
var configuracion = database.BaseDatos{
	Config: database.Config{
		Host:       "postgresql_go",
		Puerto:     5432,
		Usuario:    "postgres",
		Contrasena: "postgresgo",
		Nombre:     "api-go",
		SSLMode:    "disable",
	},
}

func main() {
	servidor := ServidorAPI{
		BaseDatos: configuracion,
		Router:    gateway.Router{},
	}

	servidor.IniciarServicio()
}
