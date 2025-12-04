// middleware/ratelimiting.go

package middleware

import (
	"database/sql"
	"go-gin-gateway/database"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	MaxPeticiones = 5
	VentanaTiempo = 1 * time.Minute
	TiempoBloqueo = 1 * time.Minute
)

// RateLimitMiddleware limita la cantidad de peticiones por IP en un tiempo
func RateLimitMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		ip := c.ClientIP()
		now := time.Now()
		tx, err := database.BD.Begin()
		if err != nil {
			c.Next()
			return
		}
		// Garantiza reversión automática en caso de pánico o retorno prematuro
		// sin confirmación explícita, evitando bloqueos huérfanos en la base de datos
		defer tx.Rollback()
		var conteo int
		var ultimaPeticion time.Time
		var bloqueado bool
		// FOR UPDATE establece un bloqueo pesimista a nivel de fila, previniendo
		// condiciones de carrera en escenarios concurrentes donde múltiples peticiones
		// de la misma IP llegan simultáneamente
		consulta := `SELECT conteo, ultima_peticion, bloqueado 
                  FROM "LimiteIP" 
                  WHERE ip = $1 
                  FOR UPDATE`
		err = tx.QueryRow(consulta, ip).Scan(&conteo, &ultimaPeticion, &bloqueado)

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

		if bloqueado {
			blockExpiration := ultimaPeticion.Add(TiempoBloqueo)
			if now.Before(blockExpiration) {
				tx.Commit()
				c.JSON(http.StatusTooManyRequests, gin.H{
					"error":         "Demasiadas peticiones. IP bloqueada temporalmente.",
					"reintentar_en": blockExpiration.Sub(now).Round(time.Second).String(),
				})
				c.Abort()
				return
			}
			// Permite recuperación automática del estado bloqueado
			// sin intervención manual
			conteo = 0
			bloqueado = false
		}

		// La ventana se reinicia tras expiración
		expiro := now.After(ultimaPeticion.Add(VentanaTiempo))
		if expiro {
			conteo = 0
		}

		conteo++

		if conteo > MaxPeticiones {
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
				"reintentar_en": TiempoBloqueo.String(),
			})
			c.Abort()
			return
		}

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
