package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class MskConfigTest {

    @Test
    void shouldApplyDefaultMskConfig() {
        MskConfig config = MskConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isMock()).isFalse();
        assertThat(config.getDefaultImage()).isEqualTo("redpandadata/redpanda:latest");
    }

    @Test
    void shouldApplyCustomMskConfig() {
        MskConfig config = MskConfig.builder()
                .enabled(false)
                .mock(true)
                .defaultImage("redpandadata/redpanda:v24")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isMock()).isTrue();
        assertThat(config.getDefaultImage()).isEqualTo("redpandadata/redpanda:v24");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        MskConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_MSK_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_MSK_MOCK", "false")
                .containsEntry("FLOCI_SERVICES_MSK_DEFAULT_IMAGE", "redpandadata/redpanda:latest");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        MskConfig.builder()
                .enabled(true)
                .mock(true)
                .defaultImage("redpandadata/redpanda:v24")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_MSK_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_MSK_MOCK", "true")
                .containsEntry("FLOCI_SERVICES_MSK_DEFAULT_IMAGE", "redpandadata/redpanda:v24");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        MskConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_MSK_ENABLED", "false")
                .doesNotContainKey("FLOCI_SERVICES_MSK_MOCK")
                .doesNotContainKey("FLOCI_SERVICES_MSK_DEFAULT_IMAGE");
    }
}
