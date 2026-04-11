package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class LambdaConfigTest {

    @Test
    void shouldApplyDefaultLambdaConfig() {
        LambdaConfig config = LambdaConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isEphemeral()).isFalse();
        assertThat(config.isExposeRuntimePorts()).isFalse();
        assertThat(config.getDefaultMemoryMb()).isEqualTo(128);
        assertThat(config.getDefaultTimeoutSeconds()).isEqualTo(3);
        assertThat(config.getDockerNetwork()).isNull();
        assertThat(config.getRuntimeApiBasePort()).isEqualTo(9200);
        assertThat(config.getRuntimeApiMaxPort()).isEqualTo(9209);
        assertThat(config.getRuntimeApiPortsCount()).isEqualTo(10);
        assertThat(config.getPollIntervalMs()).isEqualTo(1000);
        assertThat(config.getContainerIdleTimeoutSeconds()).isEqualTo(300);
    }

    @Test
    void shouldApplyCustomLambdaConfig() {
        LambdaConfig config = LambdaConfig.builder()
                .enabled(false)
                .ephemeral(true)
                .exposeRuntimePorts(true)
                .defaultMemoryMb(256)
                .defaultTimeoutSeconds(10)
                .dockerNetwork("my-network")
                .runtimeApiPortRange(9300, 50)
                .pollIntervalMs(500)
                .containerIdleTimeoutSeconds(600)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isEphemeral()).isTrue();
        assertThat(config.isExposeRuntimePorts()).isTrue();
        assertThat(config.getDefaultMemoryMb()).isEqualTo(256);
        assertThat(config.getDefaultTimeoutSeconds()).isEqualTo(10);
        assertThat(config.getDockerNetwork()).isEqualTo("my-network");
        assertThat(config.getRuntimeApiBasePort()).isEqualTo(9300);
        assertThat(config.getRuntimeApiMaxPort()).isEqualTo(9349);
        assertThat(config.getRuntimeApiPortsCount()).isEqualTo(50);
        assertThat(config.getPollIntervalMs()).isEqualTo(500);
        assertThat(config.getContainerIdleTimeoutSeconds()).isEqualTo(600);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        LambdaConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_LAMBDA_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_LAMBDA_EPHEMERAL", "false")
                .containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_MEMORY_MB", "128")
                .containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_TIMEOUT_SECONDS", "3")
                .containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_BASE_PORT", "9200")
                .containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", "9209")
                .containsEntry("FLOCI_SERVICES_LAMBDA_POLL_INTERVAL_MS", "1000")
                .containsEntry("FLOCI_SERVICES_LAMBDA_CONTAINER_IDLE_TIMEOUT_SECONDS", "300")
                .doesNotContainKey("FLOCI_SERVICES_LAMBDA_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        LambdaConfig.builder()
                .enabled(true)
                .ephemeral(true)
                .defaultMemoryMb(256)
                .defaultTimeoutSeconds(30)
                .runtimeApiPortRange(9500, 50)
                .pollIntervalMs(500)
                .containerIdleTimeoutSeconds(600)
                .dockerNetwork("my-network")
                .build()
                .applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_LAMBDA_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_MEMORY_MB", "256")
                .containsEntry("FLOCI_SERVICES_LAMBDA_EPHEMERAL", "true")
                .containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_TIMEOUT_SECONDS", "30")
                .containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_BASE_PORT", "9500")
                .containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", "9549")
                .containsEntry("FLOCI_SERVICES_LAMBDA_POLL_INTERVAL_MS", "500")
                .containsEntry("FLOCI_SERVICES_LAMBDA_CONTAINER_IDLE_TIMEOUT_SECONDS", "600")
                .containsEntry("FLOCI_SERVICES_LAMBDA_DOCKER_NETWORK", "my-network");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        LambdaConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_LAMBDA_ENABLED", "false");
    }

    @Test
    void shouldExposeLambdaRuntimeApiPortsWhenEnabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withLambdaConfig(c -> c
                    .exposeRuntimePorts(true)
                    .runtimeApiPortRange(9300, 10));

            var ports = container.getExposedPorts();
            for (int port = 9300; port < 9310; port++) {
                assertThat(ports).contains(port);
            }
        }
    }

    @Test
    void shouldNotExposeLambdaRuntimeApiPortsWhenDisabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withLambdaConfig(c -> c
                    .enabled(false)
                    .runtimeApiPortRange(9300, 10));

            assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_LAMBDA_ENABLED", "false");
            assertThat(container.getExposedPorts()).doesNotContain(9300);
        }
    }
}
