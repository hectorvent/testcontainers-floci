package io.floci.testcontainers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlociContainerTest {

    @Nested
    class ServiceIndependent {
        @Test
        void shouldCreateContainerWithDefaultImage() {
            try (FlociContainer container = new FlociContainer()) {
                assertThat(container.getDockerImageName()).isEqualTo("hectorvent/floci:latest");
            }
        }

        @Test
        void shouldRejectIncompatibleImage() {
            assertThatThrownBy(() -> new FlociContainer(DockerImageName.parse("other/image:latest")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldReturnDefaultRegion() {
            try (FlociContainer container = new FlociContainer()) {
                assertThat(container.getRegion()).isEqualTo("us-east-1");
            }
        }

        @Test
        void shouldReturnCustomRegion() {
            try (FlociContainer container = new FlociContainer()) {
                container.withRegion("eu-west-1");
                assertThat(container.getRegion()).isEqualTo("eu-west-1");
            }
        }

        @Test
        void shouldReturnDefaultCredentials() {
            try (FlociContainer container = new FlociContainer()) {
                assertThat(container.getAccessKey()).isEqualTo("test");
                assertThat(container.getSecretKey()).isEqualTo("test");
            }
        }

        @Test
        void shouldExposeFlociPort() {
            try (FlociContainer container = new FlociContainer()) {
                assertThat(container.getExposedPorts()).contains(FlociContainer.PORT);
            }
        }

        @Test
        void shouldReturnDefaultLogLevel() {
            try (FlociContainer container = new FlociContainer()) {
                assertThat(container.getLogLevel()).isEqualTo(Level.WARN);
            }
        }

        @Test
        void shouldReturnCustomLogLevel() {
            try (FlociContainer container = new FlociContainer()) {
                container.withLogLevel(Level.DEBUG);
                assertThat(container.getLogLevel()).isEqualTo(Level.DEBUG);
            }
        }

        @Test
        void shouldFallbackToWarnForInvalidLogLevel() {
            try (FlociContainer container = new FlociContainer()) {
                container.withEnv("QUARKUS_LOG_CATEGORY__IO_GITHUB_HECTORVENT__LEVEL", "INVALID");
                assertThat(container.getLogLevel()).isEqualTo(Level.WARN);
            }
        }

        @Test
        void shouldConfigureDedicatedNetwork() {
            try (FlociContainer container = new FlociContainer()) {
                container.withDedicatedNetwork();

                String networkName = container.getDedicatedNetworkName();
                assertThat(networkName).startsWith("floci-network-");
                assertThat(networkName).hasSize("floci-network-".length() + 8);
                assertThat(container.getNetwork()).isNotNull();
            }
        }

        @Test
        void shouldCreateUniqueNetworkPerCall() {
            try (FlociContainer container1 = new FlociContainer();
                 FlociContainer container2 = new FlociContainer()) {
                container1.withDedicatedNetwork();
                container2.withDedicatedNetwork();

                String network1 = container1.getDedicatedNetworkName();
                String network2 = container2.getDedicatedNetworkName();
                assertThat(network1).isNotEqualTo(network2);
            }
        }
    }

    @Nested
    class LambdaService {
        @Test
        void shouldConfigureLambdaWithDefaultValues() {
            try (FlociContainer container = new FlociContainer()) {
                container.withLambdaConfig(c -> {
                });

                var env = container.getEnvMap();
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_ENABLED", "true");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_EPHEMERAL", "false");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_MEMORY_MB", "128");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_TIMEOUT_SECONDS", "3");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_BASE_PORT", "9200");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", "9299");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_POLL_INTERVAL_MS", "1000");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_CONTAINER_IDLE_TIMEOUT_SECONDS", "300");
                assertThat(env).doesNotContainKey("FLOCI_SERVICES_LAMBDA_DOCKER_NETWORK");
            }
        }

        @Test
        void shouldConfigureLambdaWithCustomValues() {
            try (FlociContainer container = new FlociContainer()) {
                container.withLambdaConfig(c -> c
                        .enabled(true)
                        .ephemeral(true)
                        .defaultMemoryMb(256)
                        .defaultTimeoutSeconds(30)
                        .runtimeApiPortRange(9500, 50)
                        .pollIntervalMs(500)
                        .containerIdleTimeoutSeconds(600)
                        .dockerNetwork("my-network"));

                var env = container.getEnvMap();
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_ENABLED", "true");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_EPHEMERAL", "true");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_MEMORY_MB", "256");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_DEFAULT_TIMEOUT_SECONDS", "30");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_BASE_PORT", "9500");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", "9549");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_POLL_INTERVAL_MS", "500");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_CONTAINER_IDLE_TIMEOUT_SECONDS", "600");
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_DOCKER_NETWORK", "my-network");
            }
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
                // Only the default ports should be exposed, not the runtime API ports
                assertThat(container.getExposedPorts()).doesNotContain(9300);
            }
        }

        @Test
        void shouldStoreLambdaConfigOnContainer() {
            try (FlociContainer container = new FlociContainer()) {
                container.withLambdaConfig(c -> c
                        .defaultMemoryMb(512)
                        .ephemeral(true));

                assertThat(container.getLambdaConfig().getDefaultMemoryMb()).isEqualTo(512);
                assertThat(container.getLambdaConfig().isEphemeral()).isTrue();
            }
        }
    }
}
