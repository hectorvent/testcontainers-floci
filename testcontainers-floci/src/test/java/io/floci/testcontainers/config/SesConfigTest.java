package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SesConfigTest {

    @Test
    void shouldApplyDefaultSesConfig() {
        SesConfig config = SesConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void shouldApplyCustomSesConfig() {
        SesConfig config = SesConfig.builder()
                .enabled(false)
                .build();
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SesConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SES_ENABLED", "true");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SesConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SES_ENABLED", "false");
    }
}
