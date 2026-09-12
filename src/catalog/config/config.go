// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package config

// Configuration exported
type AppConfiguration struct {
	Port     int `env:"PORT,default=8080"`
	Database DatabaseConfiguration
	Auth     AuthConfiguration
}

// DatabaseConfiguration exported
type DatabaseConfiguration struct {
	Type           string `env:"RETAIL_CATALOG_PERSISTENCE_PROVIDER,default=in-memory"`
	Endpoint       string `env:"RETAIL_CATALOG_PERSISTENCE_ENDPOINT"`
	Name           string `env:"RETAIL_CATALOG_PERSISTENCE_DB_NAME,default=catalogdb"`
	User           string `env:"RETAIL_CATALOG_PERSISTENCE_USER,default=catalog_user"`
	Password       string `env:"RETAIL_CATALOG_PERSISTENCE_PASSWORD"`
	ConnectTimeout int    `env:"RETAIL_CATALOG_PERSISTENCE_CONNECT_TIMEOUT,default=5"`
}

// AuthConfiguration holds the shared secret used to verify JWTs issued by
// the UI service. Every service that validates tokens reads the SAME
// env var name so they all trust tokens signed with the same secret.
type AuthConfiguration struct {
	Secret string `env:"RETAIL_JWT_SECRET,default=local-dev-secret-change-me-32-chars-min"`
}
