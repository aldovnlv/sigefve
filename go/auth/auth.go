package auth

import (
	"database/sql"
	"errors"
	"go-gin-gateway/database"
	"net/http"
	"strings"
	"time"

	"go-gin-gateway/models"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

var jwtSecret = []byte("a8048938767d1b374f2483b936b3e6bb")

const bcryptCost = 10

type InfoJWT struct {
	Usuario   string `json:"usuario"`
	IDUsuario int    `json:"id_usuario"`
	Rol       string `json:"rol"`
	jwt.RegisteredClaims
}

type Autenticador struct{}

// RegistrarUsuario inserta un nuevo usuario en la base de datos después de hashear la contraseña.
func (a Autenticador) RegistrarUsuario(nombre, email, contrasena, rol string) (models.Usuario, error) {
	var count int
	err := database.BD.QueryRow(`SELECT COUNT(*) FROM "Usuario" WHERE email = $1`, email).Scan(&count)
	if err != nil {
		return models.Usuario{}, err
	}
	if count > 0 {
		return models.Usuario{}, errors.New("el email ya está registrado")
	}

	// 2. Hashear la contraseña
	hash, err := hashContrasena(contrasena)
	if err != nil {
		return models.Usuario{}, err
	}

	fechaCreacion := time.Now()

	// 3. Insertar en la BD y obtener el ID
	var newUserID int
	consulta := `INSERT INTO "Usuario" (nombre, email, contrasena_hash, rol, fecha_creacion) 
              VALUES ($1, $2, $3, $4, $5) RETURNING id`

	// Asumimos PostgreSQL (usando RETURNING id)
	err = database.BD.QueryRow(consulta, nombre, email, hash, rol, fechaCreacion).Scan(&newUserID)
	if err != nil {
		return models.Usuario{}, err
	}

	// 4. Devolver el nuevo usuario (sin el hash)
	return models.Usuario{
		ID:            newUserID,
		Nombre:        nombre,
		Email:         email,
		Rol:           rol,
		FechaCreacion: fechaCreacion,
	}, nil
}

func (a Autenticador) Login(email, password string) (models.Usuario, error) {
	consulta := `SELECT id, nombre, email, rol, contrasena_hash, fecha_creacion 
              FROM "Usuario" WHERE email = $1`

	// Solo se espera un resultado
	row := database.BD.QueryRow(consulta, email)

	var user models.Usuario

	err := row.Scan(
		&user.ID,
		&user.Nombre,
		&user.Email,
		&user.Rol,
		&user.ContrasenaHash,
		&user.FechaCreacion,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return models.Usuario{}, errors.New("credenciales inválidas")
		}
		return models.Usuario{}, err
	}

	match := verificarContrasena(user.ContrasenaHash, password)

	if !match {
		return models.Usuario{}, errors.New("credenciales inválidas")
	}

	// Se borra la contraseña por seguridad
	user.ContrasenaHash = ""

	return user, nil
}

// GenerarJWT genera un token JWT y lo guarda en la base de datos
func (a Autenticador) GenerarJWT(usuario string, idUsuario int, rol string) (models.Token, error) {
	expiracion := time.Now().Add(24 * time.Hour)
	fechaEmision := time.Now()

	claims := &InfoJWT{
		Usuario:   usuario,
		IDUsuario: idUsuario,
		Rol:       rol,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expiracion),
			IssuedAt:  jwt.NewNumericDate(fechaEmision),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString(jwtSecret)
	if err != nil {
		return models.Token{}, err
	}

	// 1. Guardar token en la base de datos y obtener el ID
	consulta := `INSERT INTO "Token" (id_usuario, token_jwt, fecha_emision, fecha_expiracion) 
             VALUES ($1, $2, $3, $4) RETURNING id`

	var newTokenID int // Variable para capturar el ID de la nueva fila

	err = database.BD.QueryRow(consulta, idUsuario, tokenString, fechaEmision, expiracion).Scan(&newTokenID)

	if err != nil {
		return models.Token{}, err
	}

	// 2. Devolver el modelo Token completamente poblado
	tokenModel := models.Token{
		ID:              newTokenID,
		IDUsuario:       idUsuario,
		TokenJWT:        tokenString,
		FechaEmision:    fechaEmision,
		FechaExpiracion: expiracion,
	}

	return tokenModel, nil
}

// validarToken valida el JWT y verifica que esté activo en la BD
func validarToken(tokenString string) (*InfoJWT, error) {
	infoJWT := &InfoJWT{}

	token, err := jwt.ParseWithClaims(tokenString, infoJWT, func(token *jwt.Token) (interface{}, error) {
		return jwtSecret, nil
	})

	if err != nil || !token.Valid {
		return nil, err
	}

	// Verificar que el token exista en la BD y no haya expirado
	var fechaExpiracion time.Time
	consulta := `SELECT fecha_expiracion FROM "Token" WHERE token_jwt = $1`
	err = database.BD.QueryRow(consulta, tokenString).Scan(&fechaExpiracion)

	if err == sql.ErrNoRows {
		return nil, jwt.ErrSignatureInvalid
	}
	if err != nil {
		return nil, err
	}

	// Verificar que no haya expirado en la BD
	if time.Now().After(fechaExpiracion) {
		return nil, jwt.ErrTokenExpired
	}

	return infoJWT, nil
}

// RevocarToken elimina un token de la base de datos, invalidándolo inmediatamente.
func (a Autenticador) RevocarToken(tokenString string) error {
	consulta := `DELETE FROM "Token" WHERE token_jwt = $1`

	// Ejecutamos la eliminación.
	_, err := database.BD.Exec(consulta, tokenString)

	return err
}

// JWTMiddleware middleware para proteger rutas
func (a Autenticador) JWTMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token no proporcionado"})
			c.Abort()
			return
		}

		tokenString := strings.TrimPrefix(authHeader, "Bearer ")
		infoJWT, err := validarToken(tokenString)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token inválido o expirado"})
			c.Abort()
			return
		}

		// Guardar información del usuario en el contexto
		c.Set("usuario", infoJWT.Usuario)
		c.Set("id_usuario", infoJWT.IDUsuario)
		c.Set("rol", infoJWT.Rol)
		c.Set("token", tokenString)

		c.Next()
	}
}

// AutorizarRol crea un middleware que restringe el acceso a un rol específico.
func (a Autenticador) AutorizarRol(rolRequerido string) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 1. Obtener el rol del contexto (establecido previamente por JWTMiddleware)
		rolAny, existe := c.Get("rol")

		if !existe {
			c.JSON(http.StatusForbidden, gin.H{"error": "Acceso denegado: Rol no definido."})
			c.Abort()
			return
		}

		rolActual, ok := rolAny.(string)
		if !ok {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error interno al procesar el rol."})
			c.Abort()
			return
		}

		// 2. Comprobar si el rol actual coincide con el rol requerido
		if rolActual != rolRequerido {
			c.JSON(http.StatusForbidden, gin.H{"error": "Acceso denegado: Se requiere rol de " + rolRequerido})
			c.Abort()
			return
		}

		// Si el rol coincide, continuar con la petición
		c.Next()
	}
}

// verificarContrasena compara la contraseña con el hash
func verificarContrasena(hash, contrasena string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(contrasena))
	return err == nil
}

// hashContrasena genera un hash bcrypt
func hashContrasena(contrasena string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(contrasena), bcryptCost)
	return string(bytes), err
}
