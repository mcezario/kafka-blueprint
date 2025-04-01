.PHONY: build
.DEFAULT_GOAL: build

generate-protobuf-java-source:
	./mvnw generate-sources
