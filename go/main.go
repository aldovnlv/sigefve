package main

import (
	"go-gin-gateway/database"
	"go-gin-gateway/gateway"
	"log"
)

func main() {
	// Configuración de la base de datos
	dbConfig := database.Config{
		Host:     "localhost",
		Port:     5432,
		User:     "postgres",
		Password: "123",
		DBName:   "api-go",
		SSLMode:  "disable",
	}

	// Conectar a la base de datos
	if err := database.Connect(dbConfig); err != nil {
		log.Fatalf("Error conectando a la base de datos: %v", err)
	}
	defer database.Close()

	// Preparar y ejecutar el servidor
	r := gateway.PrepararRutas()

	port := "8080"
	log.Printf("Servidor escuchando en el puerto %s", port)

	if err := r.Run(":" + port); err != nil {
		log.Fatalf("Error iniciando servidor: %v", err)
	}
}
