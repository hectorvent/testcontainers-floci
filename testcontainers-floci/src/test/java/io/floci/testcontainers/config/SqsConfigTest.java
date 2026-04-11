package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SqsConfigTest {

    @Test
    void shouldApplyDefaultSqsConfig() {
        SqsConfig config = SqsConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getDefaultVisibilityTimeout()).isEqualTo(30);
        assertThat(config.getMaxMessageSize()).isEqualTo(262144);
    }

    @Test
    void shouldApplyCustomSqsConfig() {
        SqsConfig config = SqsConfig.builder()
                .enabled(false)
                .defaultVisibilityTimeout(60)
                .maxMessageSize(131072)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getDefaultVisibilityTimeout()).isEqualTo(60);
        assertThat(config.getMaxMessageSize()).isEqualTo(131072);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SqsConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SQS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_SQS_DEFAULT_VISIBILITY_TIMEOUT", "30")
                .containsEntry("FLOCI_SERVICES_SQS_MAX_MESSAGE_SIZE", "262144");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SqsConfig.builder()
                .defaultVisibilityTimeout(60)
                .maxMessageSize(131072)
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SQS_DEFAULT_VISIBILITY_TIMEOUT", "60")
                .containsEntry("FLOCI_SERVICES_SQS_MAX_MESSAGE_SIZE", "131072");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SqsConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SQS_ENABLED", "false");
    }
}
