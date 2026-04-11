package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class S3ConfigTest {

    @Test
    void shouldApplyDefaultS3Config() {
        S3Config config = S3Config.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getDefaultPresignExpirySeconds()).isEqualTo(3600);
    }

    @Test
    void shouldApplyCustomS3Config() {
        S3Config config = S3Config.builder()
                .enabled(false)
                .defaultPresignExpirySeconds(7200)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getDefaultPresignExpirySeconds()).isEqualTo(7200);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        S3Config.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_S3_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_S3_DEFAULT_PRESIGN_EXPIRY_SECONDS", "3600");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        S3Config.builder()
                .defaultPresignExpirySeconds(7200)
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_S3_DEFAULT_PRESIGN_EXPIRY_SECONDS", "7200");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        S3Config.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_S3_ENABLED", "false");
    }
}
