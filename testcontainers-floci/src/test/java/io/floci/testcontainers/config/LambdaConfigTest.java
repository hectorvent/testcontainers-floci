package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;

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
}
