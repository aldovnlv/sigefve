package models

import "time"

type Usuario struct {
	ID             int       `db:"id"`
	Nombre         string    `db:"nombre"`
	Email          string    `db:"email"`
	ContrasenaHash string    `db:"contrasena_hash"`
	Rol            string    `db:"rol"`
	FechaCreacion  time.Time `db:"fecha_creacion"`
}

type Token struct {
	ID              int       `db:"id"`
	IDUsuario       int       `db:"id_usuario"`
	TokenJWT        string    `db:"token_jwt"`
	FechaEmision    time.Time `db:"fecha_emision"`
	FechaExpiracion time.Time `db:"fecha_expiracion"`
}

type LogPeticion struct {
	ID        int       `db:"id"`
	IP        string    `db:"ip"`
	Ruta      string    `db:"ruta"`
	Metodo    string    `db:"metodo"`
	Fecha     time.Time `db:"fecha"`
	IDUsuario *int      `db:"id_usuario"` // Puntero para permitir NULL
}

type LimiteIP struct {
	ID             int       `db:"id"`
	IP             string    `db:"ip"`
	Conteo         int       `db:"conteo"`
	UltimaPeticion time.Time `db:"ultima_peticion"`
	Bloqueado      bool      `db:"bloqueado"`
}
