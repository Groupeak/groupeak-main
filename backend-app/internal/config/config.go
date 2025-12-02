package config

import (
	"fmt"
	"log"
	"os"
)


type Config struct {
	DBDsn string
	JWTToken string
	HTTPPort string	
}


func Load() Config {
	host := os.Getenv("DB_HOST")
	port := os.Getenv("DB_PORT")
	user := os.Getenv("DB_USER")
	password := os.Getenv("DB_PASSWORD")
	name := os.Getenv("DB_NAME")
	sslmode := os.Getenv("DB_SSLMODE")

	if user == "" || name == "" {
		log.Fatal("DB_USER or DB_NAME must be set")
	}

	dsn := fmt.Sprintf("postgres://%s:%s@%s:%s/%s?sslmode=%s", user, password, host, port, name, sslmode)

	jwtToken := os.Getenv("JWT_TOKEN")
	if jwtToken == "" {
		log.Fatal("JWT token is not set")
	}

	httpPort := os.Getenv("APP_PORT")

	return Config {
		DBDsn: dsn,
		JWTToken: jwtToken,
		HTTPPort: httpPort,
	}
}