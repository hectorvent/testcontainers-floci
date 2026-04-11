package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class ElastiCacheConfigTest {

    @Test
    void shouldApplyDefaultElastiCacheConfig() {
        ElastiCacheConfig config = ElastiCacheConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getProxyBasePort()).isEqualTo(6379);
        assertThat(config.getProxyMaxPort()).isEqualTo(6388);
        assertThat(config.getProxyPortsCount()).isEqualTo(10);
        assertThat(config.getDefaultImage()).isEqualTo("valkey/valkey:8");
        assertThat(config.getDockerNetwork()).isNull();
    }

    @Test
    void shouldApplyCustomElastiCacheConfig() {
        ElastiCacheConfig config = ElastiCacheConfig.builder()
                .enabled(false)
                .proxyPortRange(7000, 50)
                .defaultImage("redis:7")
                .dockerNetwork("my-cache-network")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getProxyBasePort()).isEqualTo(7000);
        assertThat(config.getProxyMaxPort()).isEqualTo(7049);
        assertThat(config.getProxyPortsCount()).isEqualTo(50);
        assertThat(config.getDefaultImage()).isEqualTo("redis:7");
        assertThat(config.getDockerNetwork()).isEqualTo("my-cache-network");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        ElastiCacheConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_PROXY_BASE_PORT", "6379")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_PROXY_MAX_PORT", "6388")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_DEFAULT_IMAGE", "valkey/valkey:8")
                .doesNotContainKey("FLOCI_SERVICES_ELASTICACHE_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        ElastiCacheConfig.builder()
                .enabled(true)
                .proxyPortRange(7000, 50)
                .defaultImage("redis:7")
                .dockerNetwork("my-cache-network")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_PROXY_BASE_PORT", "7000")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_PROXY_MAX_PORT", "7049")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_DEFAULT_IMAGE", "redis:7")
                .containsEntry("FLOCI_SERVICES_ELASTICACHE_DOCKER_NETWORK", "my-cache-network");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        ElastiCacheConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_ELASTICACHE_ENABLED", "false");
    }

    @Test
    void shouldNotExposeElastiCachePortsWhenDisabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withElastiCacheConfig(c -> c.enabled(false).proxyPortRange(8100, 50));

            var env = container.getEnvMap();
            assertThat(env).containsEntry("FLOCI_SERVICES_ELASTICACHE_ENABLED", "false");
            assertThat(container.getExposedPorts()).doesNotContain(8100);
        }
    }
}
