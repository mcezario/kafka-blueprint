package main

import (
    "log"
    "go-api/controller"
    "go-api/kafka"

    "github.com/gin-gonic/gin"
)

func main() {
    if err := kafka.InitKafkaProducer("localhost:9092"); err != nil {
        log.Fatalf("Kafka init failed: %v", err)
    }

	// Initialize Schema Registry client
    kafka.InitSchemaManager("http://localhost:8081")

    // Register schemas
    schemaKeyID, err := kafka.GetSchemaManager().GetOrRegisterSchema("customers-key", "./../../schemas/protobuf/EntityMessageKey.proto")
    if err != nil {
        log.Fatalf("Schema Key error: %v", err)
    }
    kafka.CustomerKeySchemaID = schemaKeyID

    schemaValueID, err := kafka.GetSchemaManager().GetOrRegisterSchema("customers-value", "./../../schemas/protobuf/Customer.proto")
    if err != nil {
        log.Fatalf("Schema Value error: %v", err)
    }
    kafka.CustomerValueSchemaID = schemaValueID

    r := gin.Default()

    r.POST("/customers", controller.PostCustomer)

    r.Run(":8088")
}
