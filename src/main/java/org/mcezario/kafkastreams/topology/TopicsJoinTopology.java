package org.mcezario.kafkastreams.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.mcezario.kafkastreams.serde.SchemaRegistrySerdeFactory;
import org.mcezario.kafkastreams.serde.TenantJson;
import org.mcezario.schema.protobuf.Customer;
import org.mcezario.schema.protobuf.CustomerEnriched;
import org.mcezario.schema.protobuf.EntityMessageKey;
import org.mcezario.schema.protobuf.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TopicsJoinTopology {

    protected static final String BRANCH_PREFIX = "customers-enriched-";

    private static final String BRANCH_JOIN_SUCCESS = "join-ok";

    private static final String BRANCH_JOIN_ERROR = "tenant-not-found";

    protected static final String TENANTS_TOPIC = "tenants";

    protected static final String CUSTOMERS_TOPIC = "customers";

    protected static final String CUSTOMERS_ENRICHED_TOPIC = "customers-enriched";

    protected static final String CUSTOMERS_WITHOUT_TENANT_TOPIC = "customers-without-tenant";

    @Autowired
    private SchemaRegistrySerdeFactory schemaRegistrySerdeFactory;

    public Topology joinCustomerAndTenantTopics(StreamsBuilder builder) {
        KafkaProtobufSerde<EntityMessageKey> entityKeySerde = schemaRegistrySerdeFactory.create(EntityMessageKey.class, true);
        KafkaProtobufSerde<Customer> customersSerde = schemaRegistrySerdeFactory.create(Customer.class, false);
        KafkaProtobufSerde<CustomerEnriched> customerEnrichedSerde = schemaRegistrySerdeFactory.create(CustomerEnriched.class, false);
        JsonSerde<TenantJson> tenantSerde = new JsonSerde<>(TenantJson.class, new ObjectMapper());

        // Read topics
        final KTable<Long, TenantJson> tenantsTable = builder
                .stream(TENANTS_TOPIC, Consumed.with(Serdes.Long(), tenantSerde))
                .selectKey((k, v) -> v.getId())
                .toTable(Named.as("tenants-by-id"));

        final KStream<Long, Customer> customersTable = builder
                .stream(CUSTOMERS_TOPIC, Consumed.with(entityKeySerde, customersSerde))
                .selectKey((k, v) -> k.getTenantId(), Named.as("customers-by-tenant-id"));

        // Join the streams
        Map<String, KStream<EntityMessageKey, CustomerEnriched>> branches = customersTable
                .leftJoin(tenantsTable, (l, r) -> createCustomerEnriched(l, r), Joined.with(Serdes.Long(), customersSerde, tenantSerde))
                .selectKey((k, v) -> createCustomerEnrichedKey(v.getCustomer().getId(), k))
                .split(Named.as(BRANCH_PREFIX))
                .branch((k, v) -> StringUtils.isNotBlank(v.getTenant().getName()), Branched.as(BRANCH_JOIN_SUCCESS))
                .branch((k, v) -> StringUtils.isBlank(v.getTenant().getName()), Branched.as(BRANCH_JOIN_ERROR))
                .noDefaultBranch();

        branches.get(BRANCH_PREFIX + BRANCH_JOIN_SUCCESS)
                .to(CUSTOMERS_ENRICHED_TOPIC, Produced.with(entityKeySerde, customerEnrichedSerde));
        branches.get(BRANCH_PREFIX + BRANCH_JOIN_ERROR)
                .mapValues((k,v) -> v.getCustomer())
                .to(CUSTOMERS_WITHOUT_TENANT_TOPIC, Produced.with(entityKeySerde, customersSerde));

        return builder.build();
    }


    private static Tenant buildTenant(TenantJson json) {
        if (json == null) {
            return Tenant.newBuilder().build();
        }
        return Tenant.newBuilder()
                .setId(json.getId())
                .setName(json.getName())
                .build();
    }

    private CustomerEnriched createCustomerEnriched(Customer customer, TenantJson tenant) {
        return CustomerEnriched.newBuilder()
                .setCustomer(customer)
                .setTenant(buildTenant(tenant))
                .build();
    }

    private EntityMessageKey createCustomerEnrichedKey(long customerId, long tenantId) {
        return EntityMessageKey.newBuilder()
                .setScope("customers")
                .setEntityId(customerId)
                .setTenantId(Long.valueOf(tenantId))
                .build();
    }

}
