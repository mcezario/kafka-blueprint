package org.mcezario.kafkastreams.serde;

import com.google.protobuf.Message;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SchemaRegistrySerdeFactory {

    @Value( "${spring.kafka.properties.schema.registry.url}" )
    private String kafkaRegistryUrlConfig;

    public <T extends Message> KafkaProtobufSerde<T> create(Class<T> clazz, boolean key) {
        KafkaProtobufSerde<T> serde = new KafkaProtobufSerde<>();
        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaRegistryUrlConfig);
        String specificProtobufType = key
                ? KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_KEY_TYPE
                : KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE;
        serdeConfig.put(specificProtobufType, clazz.getName());
        serde.configure(serdeConfig, key);

        return serde;
    }

}