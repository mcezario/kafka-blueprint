package kafka

import (
	"fmt"
	"os"

	"github.com/riferrei/srclient"
)

type SchemaManager struct {
	client *srclient.SchemaRegistryClient
}

// Global instance
var schemaManagerInstance *SchemaManager

// InitSchemaManager initializes the schema registry client
func InitSchemaManager(schemaRegistryURL string) {
	client := srclient.CreateSchemaRegistryClient(schemaRegistryURL)
	schemaManagerInstance = &SchemaManager{client: client}
}

// GetSchemaManager returns the singleton schema manager
func GetSchemaManager() *SchemaManager {
	if schemaManagerInstance == nil {
		panic("SchemaManager is not initialized")
	}
	return schemaManagerInstance
}

// GetOrRegisterSchema fetches or registers the schema and returns the schema ID
func (s *SchemaManager) GetOrRegisterSchema(subject string, protoFilePath string) (int, error) {
	// Try to get latest schema
	schema, err := s.client.GetLatestSchema(subject)
	if err == nil {
		return schema.ID(), nil
	}

	// Not found — try to register
	fmt.Printf("Schema not found for subject %s. Registering...\n", subject)

	schemaBytes, err := os.ReadFile(protoFilePath)
	if err != nil {
		return 0, fmt.Errorf("failed to read proto file: %w", err)
	}

	schema, err = s.client.CreateSchema(
		subject,
		string(schemaBytes),
		srclient.Protobuf,
	)
	if err != nil {
		return 0, fmt.Errorf("failed to register schema: %w", err)
	}

	return schema.ID(), nil
}
