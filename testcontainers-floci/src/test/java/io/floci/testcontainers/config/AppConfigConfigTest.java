package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class AppConfigConfigTest {

    @Test
    void shouldApplyDefaultAppConfigConfig() {
        AppConfigConfig config = AppConfigConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void shouldApplyCustomAppConfigConfig() {
        AppConfigConfig config = AppConfigConfig.builder()
                .enabled(false)
                .build();
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        AppConfigConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_APPCONFIG_ENABLED", "true");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        AppConfigConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_APPCONFIG_ENABLED", "false");
    }
}
