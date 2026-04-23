package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class IamConfigTest {

    @Test
    void shouldApplyDefaultIamConfig() {
        IamConfig config = IamConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isEnforcementEnabled()).isFalse();
    }

    @Test
    void shouldApplyCustomIamConfig() {
        IamConfig config = IamConfig.builder()
                .enabled(false)
                .enforcementEnabled(true)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isEnforcementEnabled()).isTrue();
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        IamConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_IAM_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_IAM_ENFORCEMENT_ENABLED", "false");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        IamConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_IAM_ENABLED", "false");
    }
}
