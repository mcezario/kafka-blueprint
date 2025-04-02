# kafka-blueprint
Repository exploring Kafka with different programing languages dealing with Producer/Consumer, Streams, Schema Registry with protobuf, and Kafka connect.

## Structure
- applications/python-tenants
  - Creates a REST Api project that creates a tenant record in a Postgres database
- applications/kafka-connect
  - To-be-created: Syncs content from the tenant Postgres table to a kafka topic
- applications/go-customers
  - To-be-created: Creates a REST Api project that produces messages to a customers kafka topic
- applications/ror-topic-consumer
  - To-be-created: Creates a Ruby on Rails application that consumes topics from kafka
- applications/java-stream
  - Joins the tenants and customers kafka topic into an enriched topic: customers-enriched
