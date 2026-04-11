package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SnsConfigTest {

    @Test
    void shouldApplyDefaultSnsConfig() {
        SnsConfig config = SnsConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void shouldApplyCustomSnsConfig() {
        SnsConfig config = SnsConfig.builder()
                .enabled(false)
                .build();
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SnsConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SNS_ENABLED", "true");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SnsConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SNS_ENABLED", "false");
    }
}
