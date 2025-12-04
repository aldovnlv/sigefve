// auth/auth.go

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

// Secreto hardcodeado
var jwtSecret = []byte("a8048938767d1b374f2483b936b3e6bb")

// Factor de costo bcrypt=10 representa 2^10=1024 iteraciones
const bcryptCost = 10

type InfoJWT struct {
	Usuario   string `json:"usuario"`
	IDUsuario int    `json:"id_usuario"`
	Rol       string `json:"rol"`
	jwt.RegisteredClaims
}

type Autenticador struct{}

func (a Autenticador) RegistrarUsuario(nombre, email, contrasena, rol string) (models.Usuario, error) {
	var count int
	err := database.BD.QueryRow(`SELECT COUNT(*) FROM "Usuario" WHERE email = $1`, email).Scan(&count)
	if err != nil {
		return models.Usuario{}, err
	}
	if count > 0 {
		return models.Usuario{}, errors.New("el email ya está registrado")
	}

	hash, err := hashContrasena(contrasena)
	if err != nil {
		return models.Usuario{}, err
	}

	fechaCreacion := time.Now()

	var newUserID int
	consulta := `INSERT INTO "Usuario" (nombre, email, contrasena_hash, rol, fecha_creacion) 
              VALUES ($1, $2, $3, $4, $5) RETURNING id`

	// RETURNING id es extensión PostgreSQL que evita una consulta
	// adicional para consultar el último id agregado
	err = database.BD.QueryRow(consulta, nombre, email, hash, rol, fechaCreacion).Scan(&newUserID)
	if err != nil {
		return models.Usuario{}, err
	}

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

	// Limpieza del hash antes de retorno: previene fuga accidental del hash
	// en logs o respuestas API
	user.ContrasenaHash = ""

	return user, nil
}

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

	consulta := `INSERT INTO "Token" (id_usuario, token_jwt, fecha_emision, fecha_expiracion) 
             VALUES ($1, $2, $3, $4) RETURNING id`

	var newTokenID int

	// Persistencia de tokens en base de datos implementa lista blanca:
	// permite revocación antes de expiración natural, superando limitación
	// de JWT (aunque añade latencia de consulta en cada validación)
	err = database.BD.QueryRow(consulta, idUsuario, tokenString, fechaEmision, expiracion).Scan(&newTokenID)

	if err != nil {
		return models.Token{}, err
	}

	tokenModel := models.Token{
		ID:              newTokenID,
		IDUsuario:       idUsuario,
		TokenJWT:        tokenString,
		FechaEmision:    fechaEmision,
		FechaExpiracion: expiracion,
	}

	return tokenModel, nil
}

func validarToken(tokenString string) (*InfoJWT, error) {
	infoJWT := &InfoJWT{}

	// ParseWithClaims verifica la intregridad de la firma criptográfica
	token, err := jwt.ParseWithClaims(tokenString, infoJWT, func(token *jwt.Token) (interface{}, error) {
		return jwtSecret, nil
	})

	if err != nil || !token.Valid {
		return nil, err
	}

	// Combina validación criptográfica del JWT con verificación de existencia en BD,
	// permitiendo revocación inmediata mientras mantiene beneficios de distribución del JWT
	var fechaExpiracion time.Time
	consulta := `SELECT fecha_expiracion FROM "Token" WHERE token_jwt = $1`
	err = database.BD.QueryRow(consulta, tokenString).Scan(&fechaExpiracion)

	if err == sql.ErrNoRows {
		return nil, jwt.ErrSignatureInvalid
	}
	if err != nil {
		return nil, err
	}

	// Aunque el JWT incluye claim exp, se valida también contra BD para detectar
	// desincronización de relojes o manipulación de timestamps del sistema
	if time.Now().After(fechaExpiracion) {
		return nil, jwt.ErrTokenExpired
	}

	return infoJWT, nil
}

func (a Autenticador) RevocarToken(tokenString string) error {
	consulta := `DELETE FROM "Token" WHERE token_jwt = $1`

	// Revocación mediante eliminación
	_, err := database.BD.Exec(consulta, tokenString)

	return err
}

func (a Autenticador) JWTMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token no proporcionado"})
			c.Abort()
			return
		}

		// Extracción del token removiendo prefijo Bearer estándar RFC 6750:
		// formato "Authorization: Bearer <token>" para tokens OAuth 2.0/JWT
		tokenString := strings.TrimPrefix(authHeader, "Bearer ")
		infoJWT, err := validarToken(tokenString)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token inválido o expirado"})
			c.Abort()
			return
		}

		// Inyección de claims en contexto de Gin: propaga información de autenticación
		// a manejadores sin re-parseo
		c.Set("usuario", infoJWT.Usuario)
		c.Set("id_usuario", infoJWT.IDUsuario)
		c.Set("rol", infoJWT.Rol)
		c.Set("token", tokenString)

		c.Next()
	}
}

func (a Autenticador) AutorizarRol(rolRequerido string) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Middleware de autorización basado en roles: separación de
		// responsabilidades donde JWTMiddleware maneja autenticación
		// y AutorizarRol maneja autorización
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

		// Comparación estricta de rol
		if rolActual != rolRequerido {
			c.JSON(http.StatusForbidden, gin.H{"error": "Acceso denegado: Se requiere rol de " + rolRequerido})
			c.Abort()
			return
		}

		c.Next()
	}
}

func verificarContrasena(hash, contrasena string) bool {
	// bcrypt usa comparación de tiempo constante evitando filtración de información sobre
	// similitud entre hash almacenado y contraseña proporcionada
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(contrasena))
	return err == nil
}

func hashContrasena(contrasena string) (string, error) {
	// Cada hash es único incluso para contraseñas idénticas
	bytes, err := bcrypt.GenerateFromPassword([]byte(contrasena), bcryptCost)
	return string(bytes), err
}
