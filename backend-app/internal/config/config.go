package config

import (
	"log"
	"os"
)


type Config struct {
	DBDsn string
	JWTToken string
	HTTPPort string	
	AppDomain string
}


func Load() Config {
	httpPort := os.Getenv("APP_PORT")

	dbUser := os.Getenv("DB_USER")
	dbPass := os.Getenv("DB_PASSWORD")
	dbHost := os.Getenv("DB_HOST")
	dbPort := os.Getenv("DB_PORT")
	dbName := os.Getenv("DB_NAME")
	dbSSL := os.Getenv("DB_SSLMODE")

	if dbUser == "" || dbName == "" {
		log.Fatal("DB_USER or DB_NAME must be set")
	}

	dsn := "postgres://" + dbUser + ":" + dbPass + "@" + dbHost + ":" + dbPort + "/" + dbName + "?sslmode=" + dbSSL

	jwtToken := os.Getenv("JWT_TOKEN")
	if jwtToken == "" {
		log.Fatal("JWT_TOKEN must be set")
	}

	appDomain := os.Getenv("APP_DOMAIN")

	return Config{
		HTTPPort: httpPort,
		DBDsn:    dsn,
		JWTToken: jwtToken,
		AppDomain: appDomain,
	}
}