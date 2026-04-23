package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Scheduler-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SchedulerConfig config = SchedulerConfig.builder()
 *     .invocationEnabled(false)
 *     .tickIntervalSeconds(5)
 *     .build();
 * }</pre>
 */
public class SchedulerConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_INVOCATION_ENABLED = true;
    private static final long DEFAULT_TICK_INTERVAL_SECONDS = 10;

    private final boolean invocationEnabled;
    private final long tickIntervalSeconds;

    private SchedulerConfig(Builder builder) {
        super(builder.enabled);
        this.invocationEnabled = builder.invocationEnabled;
        this.tickIntervalSeconds = builder.tickIntervalSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns whether the background dispatcher that fires schedule targets is enabled.
     * When {@code false}, the scheduler API is CRUD-only.
     *
     * @return {@code true} if invocation is enabled
     */
    public boolean isInvocationEnabled() {
        return invocationEnabled;
    }

    /**
     * Returns how often the dispatcher scans for due schedules, in seconds.
     *
     * @return the tick interval in seconds
     */
    public long getTickIntervalSeconds() {
        return tickIntervalSeconds;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_SCHEDULER_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_SCHEDULER_INVOCATION_ENABLED", String.valueOf(invocationEnabled));
            container.withEnv("FLOCI_SERVICES_SCHEDULER_TICK_INTERVAL_SECONDS", String.valueOf(tickIntervalSeconds));
        }
    }

    /**
     * Builder for {@link SchedulerConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean invocationEnabled = DEFAULT_INVOCATION_ENABLED;
        private long tickIntervalSeconds = DEFAULT_TICK_INTERVAL_SECONDS;

        private Builder() {
            // Allow instantiation only via SchedulerConfig.builder()
        }

        /**
         * Enables or disables the Scheduler service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets whether the background dispatcher that fires schedule targets is enabled.
         * When {@code false}, the scheduler API is CRUD-only.
         *
         * @param invocationEnabled {@code true} to enable invocation (default {@value DEFAULT_INVOCATION_ENABLED})
         * @return this builder
         */
        public Builder invocationEnabled(boolean invocationEnabled) {
            this.invocationEnabled = invocationEnabled;
            return this;
        }

        /**
         * Sets how often the dispatcher scans for due schedules, in seconds. Must be &gt;= 1.
         *
         * @param tickIntervalSeconds the tick interval in seconds (default {@value DEFAULT_TICK_INTERVAL_SECONDS})
         * @return this builder
         */
        public Builder tickIntervalSeconds(long tickIntervalSeconds) {
            this.tickIntervalSeconds = tickIntervalSeconds;
            return this;
        }

        /**
         * Creates an immutable {@link SchedulerConfig} from this builder.
         *
         * @return the Scheduler configuration
         */
        public SchedulerConfig build() {
            return new SchedulerConfig(this);
        }
    }
}
