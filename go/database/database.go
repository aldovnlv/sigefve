// database/database.go

package database

import (
	"database/sql"
	"fmt"
	"log"
	"time"

	_ "github.com/lib/pq"
)

type BaseDatos struct {
	Config
}

var BD *sql.DB

type Config struct {
	Host       string
	Puerto     int
	Usuario    string
	Contrasena string
	Nombre     string
	SSLMode    string
}

func (bd BaseDatos) Conectar(config Config) error {
	connStr := fmt.Sprintf(
		"host=%s port=%d user=%s password=%s dbname=%s sslmode=%s",
		config.Host, config.Puerto, config.Usuario, config.Contrasena, config.Nombre, config.SSLMode,
	)
	var err error
	BD, err = sql.Open("postgres", connStr)
	if err != nil {
		return fmt.Errorf("error abriendo conexión: %w", err)
	}
	// Configuración del pool de conexiones:
	// MaxOpenConns limita conexiones concurrentes para evitar
	// saturación del servidor PostgreSQL;
	// MaxIdleConns mantiene conexiones reutilizables reduciendo
	// latencia de establecimiento;
	// ConnMaxLifetime recicla conexiones periódicamente previniendo
	// acumulación de conexiones obsoletas y permitiendo rebalanceo
	// de carga en réplicas con rotación DNS
	BD.SetMaxOpenConns(25)
	BD.SetMaxIdleConns(5)
	BD.SetConnMaxLifetime(5 * time.Minute)

	// Ping valida conectividad real contra el servidor:
	// Open() solo inicializa el pool sin verificar accesibilidad de red/autenticación
	if err = BD.Ping(); err != nil {
		return fmt.Errorf("error verificando conexión: %w", err)
	}
	log.Println("Conexión a PostgreSQL establecida exitosamente")
	return nil
}
func (bd BaseDatos) Cerrar() {
	if BD != nil {
		BD.Close()
	}
}
