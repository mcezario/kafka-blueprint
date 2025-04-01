package org.mcezario.kafkastreams.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KafkaTopicConfig {

    @Bean
    public NewTopic tenantsTopic() {
        return new NewTopic("tenants", 3, (short) 1);
    }

    @Bean
    public NewTopic customersTopic() {
        return new NewTopic("customers", 3, (short) 1);
    }

    @Bean
    public NewTopic enrichedCustomerTopic() {
        return new NewTopic("customers-enriched", 3, (short) 1);
    }

}
