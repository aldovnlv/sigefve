package database

import (
	"database/sql"
	"fmt"
	"log"
	"time"

	_ "github.com/lib/pq"
)

var DB *sql.DB

type Config struct {
	Host     string
	Port     int
	User     string
	Password string
	DBName   string
	SSLMode  string
}

func Connect(config Config) error {
	connStr := fmt.Sprintf(
		"host=%s port=%d user=%s password=%s dbname=%s sslmode=%s",
		config.Host, config.Port, config.User, config.Password, config.DBName, config.SSLMode,
	)

	var err error
	DB, err = sql.Open("postgres", connStr)
	if err != nil {
		return fmt.Errorf("error abriendo conexión: %w", err)
	}

	// Configurar pool de conexiones
	DB.SetMaxOpenConns(25)
	DB.SetMaxIdleConns(5)
	DB.SetConnMaxLifetime(5 * time.Minute)

	// Verificar conexión
	if err = DB.Ping(); err != nil {
		return fmt.Errorf("error verificando conexión: %w", err)
	}

	log.Println("Conexión a PostgreSQL establecida exitosamente")
	return nil
}

func Close() {
	if DB != nil {
		DB.Close()
	}
}
