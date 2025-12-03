// database/migrations.go
package database

import (
	"log"
)

// DDL (Data Definition Language) de las tablas
const migracionDDL = `
CREATE TABLE IF NOT EXISTS "Usuario" (
  "id" serial PRIMARY KEY,
  "nombre" varchar(50) UNIQUE NOT NULL,
  "email" varchar(100) UNIQUE NOT NULL,
  "contrasena_hash" varchar(255) NOT NULL,
  "rol" varchar(20),
  "fecha_creacion" timestamp
);

CREATE TABLE IF NOT EXISTS "Token" (
  "id" serial PRIMARY KEY,
  "id_usuario" int NOT NULL,
  "token_jwt" text NOT NULL,
  "fecha_emision" timestamp,
  "fecha_expiracion" timestamp
);

CREATE TABLE IF NOT EXISTS "LogPeticion" (
  "id" serial PRIMARY KEY,
  "ip" varchar(45),
  "ruta" varchar(150),
  "metodo" varchar(10),
  "fecha" timestamp,
  "id_usuario" int
);

CREATE TABLE IF NOT EXISTS "LimiteIP" (
  "id" serial PRIMARY KEY,
  "ip" varchar(45) UNIQUE NOT NULL,
  "conteo" int,
  "ultima_peticion" timestamp,
  "bloqueado" boolean
);

DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'token_id_usuario_fkey') THEN
        ALTER TABLE "Token" ADD CONSTRAINT token_id_usuario_fkey FOREIGN KEY ("id_usuario") REFERENCES "Usuario" ("id");
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'logpeticion_id_usuario_fkey') THEN
        ALTER TABLE "LogPeticion" ADD CONSTRAINT logpeticion_id_usuario_fkey FOREIGN KEY ("id_usuario") REFERENCES "Usuario" ("id");
    END IF;
END
$$;
`

// Migraciones ejecuta el esquema DDL.
func (bd BaseDatos) Migraciones() error {
	log.Println("Ejecutando migraciones de la base de datos...")

	_, err := BD.Exec(migracionDDL)
	if err != nil {
		return err
	}

	log.Println("Migraciones completadas exitosamente.")
	return nil
}
