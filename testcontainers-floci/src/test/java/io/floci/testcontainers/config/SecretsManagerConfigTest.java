package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SecretsManagerConfigTest {

    @Test
    void shouldApplyDefaultSecretsManagerConfig() {
        SecretsManagerConfig config = SecretsManagerConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getDefaultRecoveryWindowDays()).isEqualTo(30);
    }

    @Test
    void shouldApplyCustomSecretsManagerConfig() {
        SecretsManagerConfig config = SecretsManagerConfig.builder()
                .enabled(false)
                .defaultRecoveryWindowDays(7)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getDefaultRecoveryWindowDays()).isEqualTo(7);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SecretsManagerConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SECRETSMANAGER_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_SECRETSMANAGER_DEFAULT_RECOVERY_WINDOW_DAYS", "30");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SecretsManagerConfig.builder()
                .defaultRecoveryWindowDays(7)
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SECRETSMANAGER_DEFAULT_RECOVERY_WINDOW_DAYS", "7");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SecretsManagerConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SECRETSMANAGER_ENABLED", "false");
    }
}
