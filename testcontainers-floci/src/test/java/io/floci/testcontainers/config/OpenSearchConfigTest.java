package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class OpenSearchConfigTest {

    @Test
    void shouldApplyDefaultOpenSearchConfig() {
        OpenSearchConfig config = OpenSearchConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getMode()).isEqualTo("mock");
        assertThat(config.getDefaultImage()).isEqualTo("opensearchproject/opensearch:2");
        assertThat(config.getProxyBasePort()).isEqualTo(9400);
        assertThat(config.getProxyMaxPort()).isEqualTo(9409);
        assertThat(config.getProxyPortsCount()).isEqualTo(10);
        assertThat(config.getDockerNetwork()).isNull();
    }

    @Test
    void shouldApplyCustomOpenSearchConfig() {
        OpenSearchConfig config = OpenSearchConfig.builder()
                .enabled(false)
                .mode("docker")
                .defaultImage("opensearchproject/opensearch:3")
                .proxyPortRange(9500, 50)
                .dockerNetwork("my-os-network")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getMode()).isEqualTo("docker");
        assertThat(config.getDefaultImage()).isEqualTo("opensearchproject/opensearch:3");
        assertThat(config.getProxyBasePort()).isEqualTo(9500);
        assertThat(config.getProxyMaxPort()).isEqualTo(9549);
        assertThat(config.getProxyPortsCount()).isEqualTo(50);
        assertThat(config.getDockerNetwork()).isEqualTo("my-os-network");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        OpenSearchConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_MODE", "mock")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_DEFAULT_IMAGE", "opensearchproject/opensearch:2")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_PROXY_BASE_PORT", "9400")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_PROXY_MAX_PORT", "9409")
                .doesNotContainKey("FLOCI_SERVICES_OPENSEARCH_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        OpenSearchConfig.builder()
                .enabled(true)
                .mode("docker")
                .defaultImage("opensearchproject/opensearch:3")
                .proxyPortRange(9500, 50)
                .dockerNetwork("my-os-network")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_MODE", "docker")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_DEFAULT_IMAGE", "opensearchproject/opensearch:3")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_PROXY_BASE_PORT", "9500")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_PROXY_MAX_PORT", "9549")
                .containsEntry("FLOCI_SERVICES_OPENSEARCH_DOCKER_NETWORK", "my-os-network");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        OpenSearchConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_OPENSEARCH_ENABLED", "false");
    }

    @Test
    void shouldNotExposeOpenSearchPortsWhenDisabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withOpenSearchConfig(c -> c.enabled(false).proxyPortRange(9500, 50));

            var env = container.getEnvMap();
            assertThat(env).containsEntry("FLOCI_SERVICES_OPENSEARCH_ENABLED", "false");
            assertThat(container.getExposedPorts()).doesNotContain(9500);
        }
    }
}
