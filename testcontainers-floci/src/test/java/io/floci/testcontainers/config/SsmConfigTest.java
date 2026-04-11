package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SsmConfigTest {

    @Test
    void shouldApplyDefaultSsmConfig() {
        SsmConfig config = SsmConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getMaxParameterHistory()).isEqualTo(5);
    }

    @Test
    void shouldApplyCustomSsmConfig() {
        SsmConfig config = SsmConfig.builder()
                .enabled(false)
                .maxParameterHistory(10)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getMaxParameterHistory()).isEqualTo(10);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SsmConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SSM_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_SSM_MAX_PARAMETER_HISTORY", "5");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SsmConfig.builder()
                .maxParameterHistory(10)
                .build()
                .applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SSM_MAX_PARAMETER_HISTORY", "10");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SsmConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SSM_ENABLED", "false");
    }
}
