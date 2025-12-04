// database/queries.go

package database

import (
	"database/sql"
	"go-gin-gateway/models"
	"log"
)

func ObtenerLimitesIP(limit int) ([]models.LimiteIP, error) {
	// Previene denegación de servicio por transferencia masiva de
	// datos al restringir rango válido del parámetro
	if limit <= 0 || limit > 100 {
		limit = 50
	}

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
	// rows.Err() captura errores durante iteración que Scan() puede omitir:
	// usado para detectar interrupciones de conexión o errores de red
	if err = rows.Err(); err != nil {
		return nil, err
	}
	return limites, nil
}

func ObtenerLogsPeticion(limit int) ([]models.LogPeticion, error) {
	if limit <= 0 || limit > 500 {
		limit = 100
	}

	consulta := `SELECT id, ip, ruta, metodo, fecha, id_usuario FROM "LogPeticion" ORDER BY fecha DESC LIMIT $1`
	rows, err := BD.Query(consulta, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	var logs []models.LogPeticion
	for rows.Next() {
		var l models.LogPeticion
		// sql.NullInt32 maneja valores NULL de PostgreSQL: encapsula valor opcional
		// con una bandera booleana Valid, evitando pánico al escanear columnas anulables
		var userID sql.NullInt32
		if err := rows.Scan(&l.ID, &l.IP, &l.Ruta, &l.Metodo, &l.Fecha, &userID); err != nil {
			log.Println("Error escaneando LogPeticion:", err)
			continue
		}
		// Conversión de tipo anulable SQL a puntero Go: usado para
		// representar opcionalidad donde nil indica ausencia de valor vs cero
		if userID.Valid {
			tempID := int(userID.Int32)
			l.IDUsuario = &tempID
		} else {
			l.IDUsuario = nil
		}
		logs = append(logs, l)
	}
	if err = rows.Err(); err != nil {
		return nil, err
	}
	return logs, nil
}
