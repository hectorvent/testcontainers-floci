package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.testcontainers.containers.GenericContainer;
import io.floci.testcontainers.FlociContainer;

class CloudWatchMetricsConfigTest {

    @Test
    void shouldApplyDefaultCloudWatchMetricsConfig() {
        CloudWatchMetricsConfig config = CloudWatchMetricsConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void shouldApplyCustomCloudWatchMetricsConfig() {
        CloudWatchMetricsConfig config = CloudWatchMetricsConfig.builder()
                .enabled(false)
                .build();
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        CloudWatchMetricsConfig.builder().build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_CLOUDWATCHMETRICS_ENABLED", "true");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        CloudWatchMetricsConfig.builder().enabled(false).build().applyToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_CLOUDWATCHMETRICS_ENABLED", "false");
    }
}
