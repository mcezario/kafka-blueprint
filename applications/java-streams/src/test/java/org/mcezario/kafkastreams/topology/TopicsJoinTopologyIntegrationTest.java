package org.mcezario.kafkastreams.topology;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mcezario.kafkastreams.serde.SchemaRegistrySerdeFactory;
import org.mcezario.schema.protobuf.Customer;
import org.mcezario.schema.protobuf.CustomerEnriched;
import org.mcezario.schema.protobuf.EntityMessageKey;
import org.mcezario.schema.protobuf.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith( SpringRunner.class )
@SpringBootTest( classes = {TopicsJoinTopology.class, SchemaRegistrySerdeFactory.class, StreamsBuilder.class} )
@ActiveProfiles( "test" )
public class TopicsJoinTopologyIntegrationTest {

    private static final long TENANT_1_ID = 1l;

    private static final String TENANT_1_NAME = "Dummy Tenant Name";

    private static final long TENANT_99_ID = 99l;

    private static final long CUSTOMER_1_ID = 1l;

    private static final String CUSTOMER_1_NAME = "Customer 1";

    private static final long CUSTOMER_2_ID = 2l;

    private static final String CUSTOMER_2_NAME = "Customer 2";

    private static final long CUSTOMER_3_ID = 3l;

    private static final String CUSTOMER_3_NAME = "Customer 3";

    @Autowired
    private TopicsJoinTopology topicsJoinTopology;

    @Autowired
    private SchemaRegistrySerdeFactory schemaRegistrySerdeFactory;

    @Value( "${spring.kafka.properties.schema.registry.url}" )
    private String schemaRegistry;

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, String> tenants;

    private TestInputTopic<EntityMessageKey, Customer> customers;

    private TestOutputTopic<EntityMessageKey, CustomerEnriched> customersEnrichedTopic;

    private TestOutputTopic<EntityMessageKey, Customer> customersWithoutTenantTopic;

    @Before
    public void setUp() {
        StreamsBuilder builder = new StreamsBuilder();
        topicsJoinTopology.joinCustomerAndTenantTopics(builder);

        final Topology topology = builder.build();

        // setup test driver
        final Properties props = new Properties();
        props.setProperty(APPLICATION_ID_CONFIG, this.getClass().getSimpleName());
        props.setProperty(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        props.setProperty(DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaProtobufSerde.class.getName());
        props.setProperty(DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        props.setProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);

        testDriver = new TopologyTestDriver(topology, props);

        tenants = testDriver.createInputTopic(TopicsJoinTopology.TENANTS_TOPIC, new StringSerializer(), new StringSerializer());

        Serializer<EntityMessageKey> messageKeySerializer = schemaRegistrySerdeFactory.create(EntityMessageKey.class, true).serializer();
        Serializer<Customer> customerSerializer = schemaRegistrySerdeFactory.create(Customer.class, false).serializer();
        customers = testDriver.createInputTopic(TopicsJoinTopology.CUSTOMERS_TOPIC, messageKeySerializer, customerSerializer);

        Deserializer<EntityMessageKey> messageKeyDeserializer = schemaRegistrySerdeFactory.create(EntityMessageKey.class, true).deserializer();
        Deserializer<CustomerEnriched> customerEnrichedDeserializer = schemaRegistrySerdeFactory.create(CustomerEnriched.class, false).deserializer();
        customersEnrichedTopic = testDriver.createOutputTopic(TopicsJoinTopology.CUSTOMERS_ENRICHED_TOPIC, messageKeyDeserializer, customerEnrichedDeserializer);

        Deserializer<Customer> customerDeserializer = schemaRegistrySerdeFactory.create(Customer.class, false).deserializer();
        customersWithoutTenantTopic = testDriver.createOutputTopic(TopicsJoinTopology.CUSTOMERS_WITHOUT_TENANT_TOPIC, messageKeyDeserializer, customerDeserializer);
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void shouldJoinCustomersWithTenantsSuccessfully() {
        // Given
        Tenant expectedTenant = Tenant.newBuilder().setId(TENANT_1_ID).setName(TENANT_1_NAME).build();

        // When messages are produce to topics
        tenants.pipeInput(String.format("{\"id\": %d, \"name\": \"%s\"}", TENANT_1_ID, TENANT_1_NAME));

        Customer customer1 = createCustomersMessageValue(CUSTOMER_1_ID, CUSTOMER_1_NAME);
        customers.pipeInput(createCustomersMessageKey(CUSTOMER_1_ID, TENANT_1_ID), customer1);

        Customer customer2 = createCustomersMessageValue(CUSTOMER_2_ID, CUSTOMER_2_NAME);
        customers.pipeInput(createCustomersMessageKey(CUSTOMER_2_ID, TENANT_1_ID), customer2);

        Customer customer3 = createCustomersMessageValue(CUSTOMER_3_ID, CUSTOMER_3_NAME);
        customers.pipeInput(createCustomersMessageKey(CUSTOMER_3_ID, TENANT_99_ID), customer3);

        // Then ensure proper join happens
        assertThat(customersEnrichedTopic.isEmpty()).isFalse(); // Ensure there are messages in the customers-enriched topic
        TestRecord<EntityMessageKey, CustomerEnriched> record1 = customersEnrichedTopic.readRecord(); // Consumes message
        assertThat(record1.key()).isEqualTo(createCustomersMessageKey(CUSTOMER_1_ID, TENANT_1_ID));
        assertThat(record1.value()).isEqualTo(createCustomersEnrichedMessageValue(customer1, expectedTenant));

        TestRecord<EntityMessageKey, CustomerEnriched> record2 = customersEnrichedTopic.readRecord(); // Consumes message
        assertThat(record2.key()).isEqualTo(createCustomersMessageKey(CUSTOMER_2_ID, TENANT_1_ID));
        assertThat(record2.value()).isEqualTo(createCustomersEnrichedMessageValue(customer2, expectedTenant));

        assertThat(customersEnrichedTopic.isEmpty()).isTrue(); // Ensure there is no more messages in the customers-enriched topic
    }

    @Test
    public void shouldRouteCustomersWithoutTenantToSpecificTopic() {
        // Given
        Tenant expectedTenant = Tenant.newBuilder().setId(TENANT_1_ID).setName(TENANT_1_NAME).build();

        // When messages are produce to topics
        Customer customer3 = createCustomersMessageValue(CUSTOMER_3_ID, CUSTOMER_3_NAME);
        customers.pipeInput(createCustomersMessageKey(CUSTOMER_3_ID, TENANT_99_ID), customer3);

        // Then ensure proper join happens
        assertThat(customersWithoutTenantTopic.isEmpty()).isFalse(); // Ensure there are messages in the customers-enriched topic

        TestRecord<EntityMessageKey, Customer> record1 = customersWithoutTenantTopic.readRecord(); // Consumes message
        assertThat(record1.key()).isEqualTo(createCustomersMessageKey(CUSTOMER_3_ID, TENANT_99_ID));
        assertThat(record1.value()).isEqualTo(customer3);

        assertThat(customersWithoutTenantTopic.isEmpty()).isTrue(); // Ensure there is no more messages in the customers-enriched topic
    }

    private Customer createCustomersMessageValue(final long id, final String name) {
        return Customer.newBuilder()
                .setId(id)
                .setName(name)
                .build();
    }

    private CustomerEnriched createCustomersEnrichedMessageValue(final Customer customer, final Tenant tenant) {
        return CustomerEnriched.newBuilder()
                .setCustomer(customer)
                .setTenant(tenant == null ? Tenant.newBuilder().build() : tenant)
                .build();
    }

    private EntityMessageKey createCustomersMessageKey(final long entityId, final long tenantId) {
        return EntityMessageKey.newBuilder()
                .setEntityId(entityId).setTenantId(tenantId)
                .setScope("customers")
                .build();
    }

}
