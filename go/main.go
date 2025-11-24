package main

import (
	"go-gin-gateway/gateway"
	"log"
)

func main() {
	r := gateway.PrepararRutas()
	log.Println("API Gateway corriendo en http://localhost:8080")
	r.Run(":8080")
}
