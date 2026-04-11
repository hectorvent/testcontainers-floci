package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class CloudWatchLogsConfigTest {

    @Test
    void shouldApplyDefaultCloudWatchLogsConfig() {
        CloudWatchLogsConfig config = CloudWatchLogsConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getMaxEventsPerQuery()).isEqualTo(10000);
    }

    @Test
    void shouldApplyCustomCloudWatchLogsConfig() {
        CloudWatchLogsConfig config = CloudWatchLogsConfig.builder()
                .enabled(false)
                .maxEventsPerQuery(5000)
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getMaxEventsPerQuery()).isEqualTo(5000);
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        CloudWatchLogsConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_CLOUDWATCHLOGS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_CLOUDWATCHLOGS_MAX_EVENTS_PER_QUERY", "10000");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        CloudWatchLogsConfig.builder()
                .maxEventsPerQuery(5000)
                .build()
                .applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_CLOUDWATCHLOGS_MAX_EVENTS_PER_QUERY", "5000");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        CloudWatchLogsConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_CLOUDWATCHLOGS_ENABLED", "false");
    }
}
