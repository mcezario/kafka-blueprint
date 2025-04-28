.PHONY: build
.DEFAULT_GOAL: build

init-local-environment:
	@$(MAKE) get-kafka-connect-plugins
	docker-compose -f docker-compose.yml up -d
	@$(MAKE) wait-for-kafka
	@$(MAKE) create-kafka-topics
	@$(MAKE) wait-for-db
	@$(MAKE) init-tenants
	@$(MAKE) configure-kafka-connect

init-tenants:
	cd applications/python-api && \
	python3 -m venv venv && \
	source venv/bin/activate && \
	pip install -r requirements.txt && \
	make migrate

wait-for-kafka:
	@echo "Waiting for kafka to be healthy..."
	@until [ "$$(docker inspect --format='{{.State.Health.Status}}' broker 2>/dev/null)" = "healthy" ]; do \
		echo "Still waiting..."; \
		sleep 2; \
	done
	@echo "kafka is ready!"

wait-for-db:
	@echo "Waiting for database to be healthy..."
	@until [ "$$(docker inspect --format='{{.State.Health.Status}}' db 2>/dev/null)" = "healthy" ]; do \
		echo "Still waiting..."; \
		sleep 2; \
	done
	@echo "database is ready!"

get-kafka-connect-plugins:
	mkdir -p plugins && \
	cd plugins && \
    curl -L https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/2.7.1.Final/debezium-connector-postgres-2.7.1.Final-plugin.tar.gz | tar xz

configure-kafka-connect:
	set -a && source .env && set +a && \
	curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d " \
        { \
        \"name\": \"debezium-postgres-connector\", \
        \"config\": { \
        \"connector.class\": \"io.debezium.connector.postgresql.PostgresConnector\", \
        \"database.hostname\": \"db\", \
        \"database.port\": \"5432\", \
		\"database.user\": \"$$POSTGRES_USER\", \
    	\"database.password\": \"$$POSTGRES_PASSWORD\", \
    	\"database.dbname\": \"$$POSTGRES_DB\", \
        \"database.server.name\": \"pg\", \
        \"table.include.list\": \"public.tenants_tenant\", \
        \"plugin.name\": \"pgoutput\", \
        \"slot.name\": \"debezium_slot\", \
        \"publication.name\": \"debezium_pub\", \
        \"topic.prefix\": \"pg_\", \
        \"include.before\": \"true\", \
        \"key.converter.schemas.enable\": \"false\", \
        \"value.converter.schemas.enable\": \"false\", \
        \"transforms\": \"renameTopic\", \
        \"transforms.renameTopic.type\": \"org.apache.kafka.connect.transforms.RegexRouter\", \
        \"transforms.renameTopic.regex\": \".*\", \
        \"transforms.renameTopic.replacement\": \"tenants\" \
        } \
        }"

create-kafka-topics:
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic tenants --partitions 1 --replication-factor 1 || true
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic customers --partitions 1 --replication-factor 1 || true
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic customers-enriched --partitions 1 --replication-factor 1 || true
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic connect-configs --partitions 1 --replication-factor 1 --config cleanup.policy=compact || true
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic connect-offsets --partitions 1 --replication-factor 1 --config cleanup.policy=compact || true
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic connect-status --partitions 1 --replication-factor 1 --config cleanup.policy=compact || true

generate-protobuf-java-source:
	cd ./applications/java-streams && ./mvnw generate-sources

start-tenants:
	cd ./applications/python-api && python manage.py runserver
