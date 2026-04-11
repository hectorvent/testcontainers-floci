package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for CloudWatch Logs-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * CloudWatchLogsConfig config = CloudWatchLogsConfig.builder()
 *     .maxEventsPerQuery(5000)
 *     .build();
 * }</pre>
 */
public class CloudWatchLogsConfig extends AbstractServiceConfig {

    private static final int DEFAULT_MAX_EVENTS_PER_QUERY = 10000;

    private final int maxEventsPerQuery;

    private CloudWatchLogsConfig(Builder builder) {
        super(builder.enabled);
        this.maxEventsPerQuery = builder.maxEventsPerQuery;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the maximum events per query.
     *
     * @return the maximum events per query
     */
    public int getMaxEventsPerQuery() {
        return maxEventsPerQuery;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_CLOUDWATCHLOGS_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_CLOUDWATCHLOGS_MAX_EVENTS_PER_QUERY", String.valueOf(maxEventsPerQuery));
        }
    }

    /**
     * Builder for {@link CloudWatchLogsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int maxEventsPerQuery = DEFAULT_MAX_EVENTS_PER_QUERY;

        private Builder() {
            // Allow instantiation only via CloudWatchLogsConfig.builder()
        }

        /**
         * Enables or disables the CloudWatch Logs service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the maximum number of events returned per log query.
         *
         * @param maxEventsPerQuery the maximum number of events returned per log query (default {@value DEFAULT_MAX_EVENTS_PER_QUERY})
         * @return this builder
         */
        public Builder maxEventsPerQuery(int maxEventsPerQuery) {
            this.maxEventsPerQuery = maxEventsPerQuery;
            return this;
        }

        /**
         * Creates an immutable {@link CloudWatchLogsConfig} from this builder.
         *
         * @return the CloudWatch Logs configuration
         */
        public CloudWatchLogsConfig build() {
            return new CloudWatchLogsConfig(this);
        }
    }
}
