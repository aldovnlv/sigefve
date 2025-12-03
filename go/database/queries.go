// database/queries.go
package database

import (
	"database/sql"
	"go-gin-gateway/models"
	"log"
)

// ObtenerLimitesIP recupera registros de LimiteIP con un límite opcional.
func ObtenerLimitesIP(limit int) ([]models.LimiteIP, error) {
	// Establecer un límite máximo por defecto si es 0 o muy grande.
	if limit <= 0 || limit > 100 {
		limit = 50
	}

	// Usamos $1 para el límite.
	consulta := `SELECT id, ip, conteo, ultima_peticion, bloqueado FROM "LimiteIP" ORDER BY ultima_peticion DESC LIMIT $1`

	rows, err := BD.Query(consulta, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var limites []models.LimiteIP
	for rows.Next() {
		var l models.LimiteIP
		if err := rows.Scan(&l.ID, &l.IP, &l.Conteo, &l.UltimaPeticion, &l.Bloqueado); err != nil {
			log.Println("Error escaneando LimiteIP:", err)
			continue
		}
		limites = append(limites, l)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return limites, nil
}

// En un archivo como database/queries.go o auth/queries.go

// ObtenerLogsPeticion recupera registros de LogPeticion con un límite opcional.
func ObtenerLogsPeticion(limit int) ([]models.LogPeticion, error) {
	if limit <= 0 || limit > 500 { // Límite más grande para logs
		limit = 100
	}

	// id_usuario debe ser escaneado con sql.NullInt32 o sql.NullInt64
	consulta := `SELECT id, ip, ruta, metodo, fecha, id_usuario FROM "LogPeticion" ORDER BY fecha DESC LIMIT $1`

	rows, err := BD.Query(consulta, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logs []models.LogPeticion
	for rows.Next() {
		var l models.LogPeticion
		var userID sql.NullInt32 // Usar NullInt32 para manejar NULL en id_usuario

		if err := rows.Scan(&l.ID, &l.IP, &l.Ruta, &l.Metodo, &l.Fecha, &userID); err != nil {
			log.Println("Error escaneando LogPeticion:", err)
			continue
		}

		// Convertir sql.NullInt32 a *int para tu modelo (si es lo que quieres)
		if userID.Valid {
			tempID := int(userID.Int32)
			l.IDUsuario = &tempID // Asignar puntero
		} else {
			l.IDUsuario = nil // Si es NULL, asignar nil
		}

		logs = append(logs, l)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return logs, nil
}
