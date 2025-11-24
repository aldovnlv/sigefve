package auth

import (
	"time"

	"github.com/golang-jwt/jwt/v5"
)

var llaveSecreta = []byte("CLAVE_SECRETA_SUPER_SEGURA")

// Generar un token JWT
func GenerarJWT(usuario string) string {
	peticion := jwt.MapClaims{
		"username": usuario,
		"exp":      time.Now().Add(time.Hour * 1).Unix(), // expira en 1 hora
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, peticion)
	cadenaToken, _ := token.SignedString(llaveSecreta)
	return cadenaToken
}

// Validar token
func ValidarJWT(cadenaToken string) (*jwt.Token, error) {
	return jwt.Parse(cadenaToken, func(token *jwt.Token) (interface{}, error) {
		return llaveSecreta, nil
	})
}
