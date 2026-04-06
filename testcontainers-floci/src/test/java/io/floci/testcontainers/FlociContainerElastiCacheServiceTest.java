package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CreateUserResponse;
import software.amazon.awssdk.services.elasticache.model.User;
import software.amazon.awssdk.utils.builder.SdkBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerElastiCacheServiceTest extends AbstractFlociContainerServiceTest {

    static ElastiCacheClient elastiCache;

    @BeforeAll
    static void setUp() {
        elastiCache = client(ElastiCacheClient.builder());
    }

    @Test
    void shouldDescribeUsers() {
        List<User> users = elastiCache.describeUsers(SdkBuilder::build).users();

        assertThat(users).isNotNull();
    }

    @Test
    void shouldCreateUser() {
        String userId = "test-user-" + System.currentTimeMillis();

        CreateUserResponse response = elastiCache.createUser(b -> b
                .userId(userId)
                .userName(userId)
                .engine("redis")
                .accessString("on ~* +@all")
                .noPasswordRequired(true));

        assertThat(response.userId()).isEqualTo(userId);
    }

}
