package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SchedulerConfigTest {

    @Test
    void shouldApplyDefaultSchedulerConfig() {
        SchedulerConfig config = SchedulerConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isInvocationEnabled()).isTrue();
        assertThat(config.getTickIntervalSeconds()).isEqualTo(10);
    }

    @Test
    void shouldApplyCustomSchedulerConfig() {
        SchedulerConfig config = SchedulerConfig.builder()
                .enabled(false)
                .invocationEnabled(false)
                .tickIntervalSeconds(5)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isInvocationEnabled()).isFalse();
        assertThat(config.getTickIntervalSeconds()).isEqualTo(5);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SchedulerConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SCHEDULER_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_SCHEDULER_INVOCATION_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_SCHEDULER_TICK_INTERVAL_SECONDS", "10");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SchedulerConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SCHEDULER_ENABLED", "false");
    }
}
