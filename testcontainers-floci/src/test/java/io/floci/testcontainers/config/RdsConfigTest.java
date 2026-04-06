package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;

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
}
