// main.go
package main

import (
	"go-gin-gateway/database"
	"go-gin-gateway/gateway"
	"log"
)

type ServidorAPI struct {
	BaseDatos database.BaseDatos
	Router    gateway.Router
}

func (servidor ServidorAPI) IniciarServicio() {
	if err := servidor.BaseDatos.Conectar(servidor.BaseDatos.Config); err != nil {
		log.Fatalf("Error conectando a la base de datos: %v", err)
	}
	defer servidor.BaseDatos.Cerrar()

	if err := servidor.BaseDatos.Migraciones(); err != nil {
		log.Fatalf("Error durante la migración de la base de datos: %v", err)
	}

	// Preparar y ejecutar el servidor
	r := servidor.Router.PrepararRutas()

	port := "8080"
	log.Printf("Servidor escuchando en el puerto %s", port)

	if err := r.Run(":" + port); err != nil {
		log.Fatalf("Error iniciando servidor: %v", err)
	}
}

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
