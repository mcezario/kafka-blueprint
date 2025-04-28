# kafka-blueprint
Repository exploring Kafka with different programing languages dealing with Producer/Consumer, Streams, Schema Registry with protobuf, and Kafka connect.

## Structure
- applications/python-tenants
  - Creates a REST Api project that creates a tenant record in a Postgres database
- applications/kafka-connect
  - Syncs content from the tenant's Postgres table to a kafka topic
- applications/go-customers
  - To-be-created: Creates a REST Api project that produces messages to a customers kafka topic
- applications/ror-topic-consumer
  - To-be-created: Creates a Ruby on Rails application that consumes topics from kafka
- applications/java-stream
  - Joins the tenants and customers kafka topic into an enriched topic: customers-enriched

## Set up local infrastructure
Create a `.env` file in the root directory with these variables:
```
# .env
POSTGRES_DB=kafka_blueprint
POSTGRES_USER=postgres
POSTGRES_PASSWORD=dbpasswd
```

### Init local environment
  ```
  make init-local-environment
  ```
