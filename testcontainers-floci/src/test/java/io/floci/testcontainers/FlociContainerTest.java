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
                assertThat(env).containsEntry("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", "9209");
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

    @Nested
    class RdsService {
        @Test
        void shouldConfigureRdsWithDefaultValues() {
            try (FlociContainer container = new FlociContainer()) {
                container.withRdsConfig(c -> {});

                var env = container.getEnvMap();
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_ENABLED", "true");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_PROXY_BASE_PORT", "7000");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_PROXY_MAX_PORT", "7009");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DEFAULT_POSTGRES_IMAGE", "postgres:16-alpine");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MYSQL_IMAGE", "mysql:8.0");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MARIADB_IMAGE", "mariadb:11");
                assertThat(env).doesNotContainKey("FLOCI_SERVICES_RDS_DOCKER_NETWORK");
            }
        }

        @Test
        void shouldConfigureRdsWithCustomValues() {
            try (FlociContainer container = new FlociContainer()) {
                container.withRdsConfig(c -> c
                        .enabled(true)
                        .proxyPortRange(8000, 100)
                        .defaultPostgresImage("postgres:15")
                        .defaultMysqlImage("mysql:9.0")
                        .defaultMariadbImage("mariadb:10")
                        .dockerNetwork("my-rds-network"));

                var env = container.getEnvMap();
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_ENABLED", "true");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_PROXY_BASE_PORT", "8000");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_PROXY_MAX_PORT", "8099");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DEFAULT_POSTGRES_IMAGE", "postgres:15");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MYSQL_IMAGE", "mysql:9.0");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MARIADB_IMAGE", "mariadb:10");
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_DOCKER_NETWORK", "my-rds-network");
            }
        }

        @Test
        void shouldNotSetRdsEnvVarsWhenDisabled() {
            try (FlociContainer container = new FlociContainer()) {
                container.withRdsConfig(c -> c.enabled(false).proxyPortRange(8000, 100));

                var env = container.getEnvMap();
                assertThat(env).containsEntry("FLOCI_SERVICES_RDS_ENABLED", "false");
                assertThat(container.getExposedPorts()).doesNotContain(8000);
            }
        }

        @Test
        void shouldStoreRdsConfigOnContainer() {
            try (FlociContainer container = new FlociContainer()) {
                container.withRdsConfig(c -> c
                        .defaultPostgresImage("postgres:15")
                        .proxyPortRange(8000, 100));

                assertThat(container.getRdsConfig().getDefaultPostgresImage()).isEqualTo("postgres:15");
                assertThat(container.getRdsConfig().getProxyBasePort()).isEqualTo(8000);
                assertThat(container.getRdsConfig().getProxyPortsCount()).isEqualTo(100);
            }
        }

        @Test
        void shouldReturnContainerFromWithRdsConfig() {
            try (FlociContainer container = new FlociContainer()) {
                FlociContainer result = container.withRdsConfig(c -> c
                        .proxyPortRange(8000, 100));
                assertThat(result).isSameAs(container);
            }
        }
    }
}
