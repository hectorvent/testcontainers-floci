package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class RdsConfigTest {

    @Test
    void shouldApplyDefaultRdsConfig() {
        RdsConfig config = RdsConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getProxyBasePort()).isEqualTo(7000);
        assertThat(config.getProxyMaxPort()).isEqualTo(7009);
        assertThat(config.getProxyPortsCount()).isEqualTo(10);
        assertThat(config.getDefaultPostgresImage()).isEqualTo("postgres:16-alpine");
        assertThat(config.getDefaultMysqlImage()).isEqualTo("mysql:8.0");
        assertThat(config.getDefaultMariadbImage()).isEqualTo("mariadb:11");
        assertThat(config.getDockerNetwork()).isNull();
    }

    @Test
    void shouldApplyCustomRdsConfig() {
        RdsConfig config = RdsConfig.builder()
                .enabled(false)
                .proxyPortRange(8000, 100)
                .defaultPostgresImage("postgres:15")
                .defaultMysqlImage("mysql:9.0")
                .defaultMariadbImage("mariadb:10")
                .dockerNetwork("my-rds-network")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getProxyBasePort()).isEqualTo(8000);
        assertThat(config.getProxyMaxPort()).isEqualTo(8099);
        assertThat(config.getProxyPortsCount()).isEqualTo(100);
        assertThat(config.getDefaultPostgresImage()).isEqualTo("postgres:15");
        assertThat(config.getDefaultMysqlImage()).isEqualTo("mysql:9.0");
        assertThat(config.getDefaultMariadbImage()).isEqualTo("mariadb:10");
        assertThat(config.getDockerNetwork()).isEqualTo("my-rds-network");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        RdsConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_RDS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_RDS_PROXY_BASE_PORT", "7000")
                .containsEntry("FLOCI_SERVICES_RDS_PROXY_MAX_PORT", "7009")
                .containsEntry("FLOCI_SERVICES_RDS_DEFAULT_POSTGRES_IMAGE", "postgres:16-alpine")
                .containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MYSQL_IMAGE", "mysql:8.0")
                .containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MARIADB_IMAGE", "mariadb:11")
                .doesNotContainKey("FLOCI_SERVICES_RDS_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        RdsConfig.builder()
                .enabled(true)
                .proxyPortRange(8000, 100)
                .defaultPostgresImage("postgres:15")
                .defaultMysqlImage("mysql:9.0")
                .defaultMariadbImage("mariadb:10")
                .dockerNetwork("my-rds-network")
                .build()
                .applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_RDS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_RDS_PROXY_BASE_PORT", "8000")
                .containsEntry("FLOCI_SERVICES_RDS_PROXY_MAX_PORT", "8099")
                .containsEntry("FLOCI_SERVICES_RDS_DEFAULT_POSTGRES_IMAGE", "postgres:15")
                .containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MYSQL_IMAGE", "mysql:9.0")
                .containsEntry("FLOCI_SERVICES_RDS_DEFAULT_MARIADB_IMAGE", "mariadb:10")
                .containsEntry("FLOCI_SERVICES_RDS_DOCKER_NETWORK", "my-rds-network");
    }

    @Test
    void shouldNotExposeRdsPortsWhenDisabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withRdsConfig(c -> c.enabled(false).proxyPortRange(8000, 100));

            var env = container.getEnvMap();
            assertThat(env).containsEntry("FLOCI_SERVICES_RDS_ENABLED", "false");
            assertThat(container.getExposedPorts()).doesNotContain(8000);
        }
    }
}
