package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for CloudWatch Metrics-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * CloudWatchMetricsConfig config = CloudWatchMetricsConfig.builder()
 *     .build();
 * }</pre>
 */
public class CloudWatchMetricsConfig extends AbstractServiceConfig {


    private CloudWatchMetricsConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_CLOUDWATCHMETRICS_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link CloudWatchMetricsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via CloudWatchMetricsConfig.builder()
        }

        /**
         * Enables or disables the CloudWatch Metrics service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link CloudWatchMetricsConfig} from this builder.
         *
         * @return the CloudWatch Metrics configuration
         */
        public CloudWatchMetricsConfig build() {
            return new CloudWatchMetricsConfig(this);
        }
    }
}
