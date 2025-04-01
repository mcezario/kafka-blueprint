.PHONY: build
.DEFAULT_GOAL: build

generate-protobuf-java-source:
	cd ./applications/java-streams && ./mvnw generate-sources
