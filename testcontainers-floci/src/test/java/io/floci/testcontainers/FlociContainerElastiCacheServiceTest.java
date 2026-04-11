package io.floci.testcontainers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.ClusterMode;
import software.amazon.awssdk.services.elasticache.model.ReplicationGroup;
import software.amazon.awssdk.utils.builder.SdkBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerElastiCacheServiceTest extends AbstractFlociContainerServiceTest {

    static ElastiCacheClient elastiCache;
    static String replicationGroupId;

    @BeforeAll
    static void setUp() {
        replicationGroupId = "test-rg-" + System.currentTimeMillis();
        elastiCache = client(ElastiCacheClient.builder());
    }

    @Test
    @Order(1)
    void shouldCreateReplicationGroup() {
        elastiCache.createReplicationGroup(b -> b
                .replicationGroupId(replicationGroupId)
                .replicationGroupDescription("Test replication group")
                .engine("redis")
                .clusterMode(ClusterMode.DISABLED)
                .cacheNodeType("cache.t3.micro")
                .numCacheClusters(1));

        elastiCache.waiter().waitUntilReplicationGroupAvailable(
                b -> b.replicationGroupId(replicationGroupId),
                c -> c.waitTimeout(Duration.ofSeconds(60)));

        ReplicationGroup group = elastiCache.describeReplicationGroups(
                b -> b.replicationGroupId(replicationGroupId)).replicationGroups().get(0);

        assertThat(group.status()).isEqualTo("available");
    }

    @Test
    @Order(2)
    void shouldStoreAndRetrieveDataViaRedis() {
        int redisProxyPort = floci.getMappedPort(floci.getElastiCacheConfig().getProxyBasePort());
        String redisUri = String.format("redis://%s:%d", floci.getHost(), redisProxyPort);

        for (int attempt = 1; ; attempt++) {
            try (RedisClient redisClient = RedisClient.create(redisUri);
                 StatefulRedisConnection<String, String> connection = redisClient.connect()) {

                RedisCommands<String, String> commands = connection.sync();

                // String values
                commands.set("greeting", "hello from floci");
                assertThat(commands.get("greeting")).isEqualTo("hello from floci");

                // Hash values
                commands.hset("user:1", Map.of(
                        "name", "Alice",
                        "email", "alice@example.com"));
                assertThat(commands.hget("user:1", "name")).isEqualTo("Alice");
                assertThat(commands.hget("user:1", "email")).isEqualTo("alice@example.com");
                assertThat(commands.hgetall("user:1")).containsEntry("name", "Alice");

                // List values
                commands.rpush("queue", "first", "second", "third");
                assertThat(commands.lrange("queue", 0, -1))
                        .containsExactly("first", "second", "third");
                assertThat(commands.lpop("queue")).isEqualTo("first");

                // Key expiry
                commands.set("temp-key", "expires-soon");
                commands.expire("temp-key", 3600);
                assertThat(commands.ttl("temp-key")).isGreaterThan(0);

                break;
            } catch (Exception e) {
                if (attempt >= 10) {
                    throw new RuntimeException(
                            "Failed to connect to ElastiCache Redis instance via Lettuce after 10 attempts", e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    @Order(3)
    void shouldDeleteReplicationGroup() {
        elastiCache.deleteReplicationGroup(b -> b.replicationGroupId(replicationGroupId));

        List<ReplicationGroup> groups = elastiCache.describeReplicationGroups(SdkBuilder::build).replicationGroups();
        assertThat(groups).noneMatch(g -> g.replicationGroupId().equals(replicationGroupId));
    }
}
