package io.floci.testcontainers;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import software.amazon.awssdk.services.kafka.KafkaClient;
import software.amazon.awssdk.services.kafka.model.BrokerNodeGroupInfo;
import software.amazon.awssdk.services.kafka.model.ClusterInfo;
import software.amazon.awssdk.services.kafka.model.ClusterState;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerMskServiceTest extends AbstractFlociContainerServiceTest {

    public static final String TOPIC_NAME = "test-topic";

    static KafkaClient kafka;
    static String clusterName;
    static String clusterArn;
    static String bootstrapBrokers;

    @BeforeAll
    static void setUp() {
        clusterName = "test-cluster-" + System.currentTimeMillis();
        kafka = client(KafkaClient.builder());
    }

    @Test
    @Order(1)
    void shouldCreateCluster() {
        clusterArn = kafka.createCluster(b -> b
                .clusterName(clusterName)
                .kafkaVersion("3.6.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(BrokerNodeGroupInfo.builder()
                        .instanceType("kafka.m5.large")
                        .clientSubnets("subnet-12345")
                        .build())).clusterArn();

        assertThat(clusterArn).isNotBlank();

        List<String> clusterNames = kafka.listClusters(b -> {}).clusterInfoList().stream()
                .map(ClusterInfo::clusterName)
                .toList();

        assertThat(clusterNames).contains(clusterName);
    }

    @Test
    @Order(2)
    @Disabled
    void shouldWaitForClusterActive() {
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ClusterState state = kafka.describeCluster(b -> b.clusterArn(clusterArn))
                            .clusterInfo()
                            .state();
                    assertThat(state).isEqualTo(ClusterState.ACTIVE);
                });

        bootstrapBrokers = kafka.getBootstrapBrokers(b -> b.clusterArn(clusterArn))
                .bootstrapBrokerString();

        assertThat(bootstrapBrokers).isNotBlank();
    }

    @Test
    @Order(3)
    @Disabled
    void shouldCreateTopic() throws Exception {
        try (AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapBrokers))) {

            admin.createTopics(List.of(new NewTopic(TOPIC_NAME, 1, (short) 1))).all().get();

            var topicNames = admin.listTopics().names().get();

            assertThat(topicNames).contains("test-topic");
        }
    }

    @Test
    @Order(4)
    @Disabled
    void shouldProduceAndConsumeMessage() throws Exception {
        String messageKey = "key-1";
        String messageValue = "Hello from Floci MSK!";

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapBrokers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()))) {

            producer.send(new ProducerRecord<>(TOPIC_NAME, messageKey, messageValue)).get();
        }

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapBrokers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()))) {

            consumer.subscribe(List.of(TOPIC_NAME));

            ConsumerRecords<String, String> records = ConsumerRecords.empty();
            for (int attempt = 1; attempt <= 30; attempt++) {
                records = consumer.poll(Duration.ofSeconds(2));
                if (!records.isEmpty()) {
                    break;
                }
            }

            assertThat(records).isNotEmpty();

            ConsumerRecord<String, String> record = records.iterator().next();
            assertThat(record.key()).isEqualTo(messageKey);
            assertThat(record.value()).isEqualTo(messageValue);
        }
    }

    @Test
    @Order(5)
    void shouldDeleteCluster() {
        kafka.deleteCluster(b -> b.clusterArn(clusterArn));

        List<String> clusterNames = kafka.listClusters(b -> {}).clusterInfoList().stream()
                .map(ClusterInfo::clusterName)
                .toList();

        assertThat(clusterNames).doesNotContain(clusterName);
    }

}
