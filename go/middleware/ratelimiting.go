package middleware

import (
	"database/sql"
	"go-gin-gateway/database"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	MaxRequests = 5
	TimeWindow  = 1 * time.Minute
	BlockTime   = 1 * time.Minute
)

func RateLimitMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		ip := c.ClientIP()
		now := time.Now()

		tx, err := database.BD.Begin()
		if err != nil {
			c.Next()
			return
		}
		defer tx.Rollback()

		var conteo int
		var ultimaPeticion time.Time
		var bloqueado bool

		consulta := `SELECT conteo, ultima_peticion, bloqueado 
                  FROM "LimiteIP" 
                  WHERE ip = $1 
                  FOR UPDATE`

		err = tx.QueryRow(consulta, ip).Scan(&conteo, &ultimaPeticion, &bloqueado)

		// IP nueva - insertar registro
		if err == sql.ErrNoRows {
			_, err = tx.Exec(
				`INSERT INTO "LimiteIP" (ip, conteo, ultima_peticion, bloqueado) 
                 VALUES ($1, 1, $2, FALSE)`,
				ip, now,
			)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error del sistema"})
				c.Abort()
				return
			}
			tx.Commit()
			c.Next()
			return
		}

		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error del sistema"})
			c.Abort()
			return
		}

		// IP bloqueada - verificar si ya expiró
		if bloqueado {
			blockExpiration := ultimaPeticion.Add(BlockTime)
			if now.Before(blockExpiration) {
				tx.Commit()
				c.JSON(http.StatusTooManyRequests, gin.H{
					"error":         "Demasiadas peticiones. IP bloqueada temporalmente.",
					"reintentar_en": blockExpiration.Sub(now).Round(time.Second).String(),
				})
				c.Abort()
				return
			}
			// Bloqueo expiró - resetear
			conteo = 0
			bloqueado = false
		}

		// Verificar ventana de tiempo
		expiro := now.After(ultimaPeticion.Add(TimeWindow))
		if expiro {
			conteo = 0
		}

		// Incrementar contador
		conteo++

		// Verificar si se debe bloquear
		if conteo > MaxRequests {
			_, err = tx.Exec(
				`UPDATE "LimiteIP" 
                 SET conteo = $1, ultima_peticion = $2, bloqueado = TRUE 
                 WHERE ip = $3`,
				conteo, now, ip,
			)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error del sistema"})
				c.Abort()
				return
			}
			tx.Commit()
			c.JSON(http.StatusTooManyRequests, gin.H{
				"error":         "Límite de peticiones excedido. IP bloqueada.",
				"reintentar_en": BlockTime.String(),
			})
			c.Abort()
			return
		}

		// Actualizar contador sin bloquear
		_, err = tx.Exec(
			`UPDATE "LimiteIP" 
             SET conteo = $1, ultima_peticion = $2 
             WHERE ip = $3`,
			conteo, now, ip,
		)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error del sistema"})
			c.Abort()
			return
		}

		tx.Commit()
		c.Next()
	}
}
