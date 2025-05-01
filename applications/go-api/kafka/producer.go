package kafka

import (
    "bytes"
    "fmt"
    "encoding/binary"
	"log"

	"github.com/confluentinc/confluent-kafka-go/kafka"
	protobuf "google.golang.org/protobuf/proto"
)

type KafkaProducer struct {
	producer *kafka.Producer
}

var producerInstance *KafkaProducer

func InitKafkaProducer(brokerAddress string) error {
	p, err := kafka.NewProducer(&kafka.ConfigMap{
    		"bootstrap.servers": brokerAddress,
    	})
    if err != nil {
        return err
    }

    producerInstance = &KafkaProducer{producer: p}
    return nil
}

func GetKafkaProducer() *KafkaProducer {
	if producerInstance == nil {
		log.Fatal("Kafka producer not initialized")
	}
	return producerInstance
}

func (kp *KafkaProducer) Produce(topic string, key []byte, value []byte) error {
	return kp.producer.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Key:            key,
		Value:          value,
	}, nil)
}

func EncodeProtobufWithSchema(message protobuf.Message, schemaID int) ([]byte, error) {
	serialized, err := protobuf.Marshal(message)
	if err != nil {
		return nil, err
	}

    // Create a buffer for the Confluent wire format
    var buf bytes.Buffer

    // Magic byte
    if err := buf.WriteByte(0); err != nil {
        return nil, fmt.Errorf("failed to write magic byte: %w", err)
    }

    // Write schema ID (big-endian)
    if err := binary.Write(&buf, binary.BigEndian, int32(schemaID)); err != nil {
        return nil, fmt.Errorf("failed to write schema ID: %w", err)
    }

    // Optional: write message indexes (empty for top-level messages)
    // This example assumes a single root message, so we write a 0-length varint.
    if err := buf.WriteByte(0); err != nil {
        return nil, fmt.Errorf("failed to write message index length: %w", err)
    }

    // Write the actual Protobuf payload
    if _, err := buf.Write(serialized); err != nil {
        return nil, fmt.Errorf("failed to write protobuf payload: %w", err)
    }

    return buf.Bytes(), nil
}