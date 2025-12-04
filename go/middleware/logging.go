// middleware/logging.go

package middleware

import (
	"database/sql"
	"go-gin-gateway/database"
	"time"

	"github.com/gin-gonic/gin"
)

// LogPeticiones registra todas las peticiones en la base de datos
func LogPeticiones() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Procesar la petición primero
		c.Next()

		ip := c.ClientIP()
		ruta := c.Request.URL.Path
		metodo := c.Request.Method

		// Inicializar el valor del ID del usuario para SQL
		var sqlIDUsuario sql.NullInt32

		// Intentar obtener el ID del usuario si está autenticado
		if id, exists := c.Get("id_usuario"); exists {
			if userID, ok := id.(int); ok {
				// Si el ID existe y es válido, se asignar el valor y Valid = true
				sqlIDUsuario = sql.NullInt32{Int32: int32(userID), Valid: true}
			}
		}
		// Reigstro de la petición en la base de datos
		go func() {
			consulta := `INSERT INTO "LogPeticion" (ip, ruta, metodo, fecha, id_usuario) 
             VALUES ($1, $2, $3, $4, $5)`

			_, err := database.BD.Exec(consulta, ip, ruta, metodo, time.Now(), sqlIDUsuario)

			if err != nil {
				println("Error registrando petición:", err.Error())
			}
		}()
	}
}
