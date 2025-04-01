package org.mcezario.kafkastreams.config;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.mcezario.kafkastreams.topology.TopicsJoinTopology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {

    @Bean
    Topology createTopology(@Autowired StreamsBuilder builder, @Autowired TopicsJoinTopology topology) {
        return topology.joinCustomerAndTenantTopics(builder);
    }

}
