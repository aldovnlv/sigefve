// middleware/ratelimiting.go

package middleware

import (
	"database/sql"
	"go-gin-gateway/database"
	"net"
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	MaxPeticiones = 50              // Número máximo de peticiones permitidas
	VentanaTiempo = 1 * time.Minute // Periodo de tiempo para el conteo de peticiones
	TiempoBloqueo = 1 * time.Minute // Duración del bloqueo cuando se excede el límite
)

// getIP extrae la dirección IP real del cliente considerando proxies y balanceadores de carga
func getIP(r *http.Request) string {
	// Verifica primero el header X-Forwarded-For (usado por proxies)
	if fwd := r.Header.Get("X-Forwarded-For"); fwd != "" {
		// Puede contener múltiples IPs separadas por comas, tomamos la primera
		parts := strings.Split(fwd, ",")
		return strings.TrimSpace(parts[0])
	}

	// Si no existe X-Forwarded-For, verifica X-Real-IP
	if rip := r.Header.Get("X-Real-IP"); rip != "" {
		return rip
	}

	// Como último recurso, extrae la IP de RemoteAddr
	ip, _, _ := net.SplitHostPort(r.RemoteAddr)
	return ip
}

// RateLimitMiddleware limita la cantidad de peticiones por IP en un tiempo determinado
func RateLimitMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Obtiene la IP real del cliente
		ip := getIP(c.Request)
		now := time.Now()

		// Inicia una transacción de base de datos para garantizar atomicidad
		tx, err := database.BD.Begin()
		if err != nil {
			// Si no se puede iniciar la transacción, permite continuar (fail-open)
			c.Next()
			return
		}

		// Garantiza reversión automática en caso de pánico o retorno prematuro
		// sin confirmación explícita, evitando bloqueos huérfanos en la base de datos
		defer tx.Rollback()

		var conteo int
		var ultimaPeticion time.Time
		var bloqueado bool

		// FOR UPDATE establece un bloqueo a nivel de fila, previniendo
		// condiciones de carrera en escenarios concurrentes donde múltiples peticiones
		// de la misma IP llegan simultáneamente
		consulta := `SELECT conteo, ultima_peticion, bloqueado 
                  FROM "LimiteIP" 
                  WHERE ip = $1 
                  FOR UPDATE`

		err = tx.QueryRow(consulta, ip).Scan(&conteo, &ultimaPeticion, &bloqueado)

		// Si no existe registro para esta IP, crea uno nuevo
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
			// Confirma la transacción y permite la petición
			tx.Commit()
			c.Next()
			return
		}

		// Si hubo otro error al consultar, retorna error 500
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error del sistema"})
			c.Abort()
			return
		}

		// Verifica si la IP está actualmente bloqueada
		if bloqueado {
			blockExpiration := ultimaPeticion.Add(TiempoBloqueo)

			// Si el bloqueo aún no ha expirado, rechaza la petición
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
			// sin intervención manual una vez que expira el tiempo de bloqueo
			conteo = 0
			bloqueado = false
		}

		// La ventana de tiempo se reinicia tras su expiración
		expiro := now.After(ultimaPeticion.Add(VentanaTiempo))
		if expiro {
			conteo = 0
		}

		// Incrementa el contador de peticiones
		conteo = conteo + 1

		// Si se excede el límite, bloquea la IP
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
			// Confirma el bloqueo en la base de datos
			tx.Commit()
			c.JSON(http.StatusTooManyRequests, gin.H{
				"error":         "Límite de peticiones excedido. IP bloqueada.",
				"reintentar_en": TiempoBloqueo.String(),
			})
			c.Abort()
			return
		}

		// Actualiza el contador y la fecha de última petición
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

		// Confirma los cambios en la base de datos
		tx.Commit()

		// Permite que la petición continúe al siguiente middleware/handler
		c.Next()
	}
}
