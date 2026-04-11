package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class AcmConfigTest {

    @Test
    void shouldApplyDefaultAcmConfig() {
        AcmConfig config = AcmConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getValidationWaitSeconds()).isZero();
    }

    @Test
    void shouldApplyCustomAcmConfig() {
        AcmConfig config = AcmConfig.builder()
                .enabled(false)
                .validationWaitSeconds(5)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getValidationWaitSeconds()).isEqualTo(5);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        AcmConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ACM_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ACM_VALIDATION_WAIT_SECONDS", "0");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        AcmConfig.builder()
                .validationWaitSeconds(5)
                .build()
                .applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_ACM_VALIDATION_WAIT_SECONDS", "5");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        AcmConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_ACM_ENABLED", "false");
    }
}
