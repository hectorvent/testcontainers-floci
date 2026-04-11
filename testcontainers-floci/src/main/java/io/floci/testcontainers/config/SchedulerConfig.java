package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for Scheduler-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SchedulerConfig config = SchedulerConfig.builder()
 *     .build();
 * }</pre>
 */
public class SchedulerConfig extends AbstractServiceConfig {

    private SchedulerConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_SCHEDULER_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link SchedulerConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

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
         * Creates an immutable {@link SchedulerConfig} from this builder.
         *
         * @return the Scheduler configuration
         */
        public SchedulerConfig build() {
            return new SchedulerConfig(this);
        }
    }
}
