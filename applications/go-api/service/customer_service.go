package service

import (
    "context"

	"go-api/kafka"
	"go-api/proto"
)

func ProduceCustomer(ctx context.Context, id int64, tenantId int64, name string) error {
	key := &proto.EntityMessageKey{
        EntityId: id,
        TenantId: tenantId,
        Scope: "customers",
    }
    encodedKey, err := kafka.EncodeProtobufWithSchema(key, kafka.CustomerKeySchemaID)
	if err != nil {
		panic(err)
	}

	value := &proto.Customer{
		Id:   id,
		Name: name,
	}
	encodedValue, err := kafka.EncodeProtobufWithSchema(value, kafka.CustomerValueSchemaID)
	if err != nil {
		panic(err)
	}

	producer := kafka.GetKafkaProducer()
    return producer.Produce("customers", encodedKey, encodedValue)
}
