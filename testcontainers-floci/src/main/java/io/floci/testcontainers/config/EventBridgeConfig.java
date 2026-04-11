package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for EventBridge-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * EventBridgeConfig config = EventBridgeConfig.builder()
 *     .build();
 * }</pre>
 */
public class EventBridgeConfig extends AbstractServiceConfig {


    private EventBridgeConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_EVENTBRIDGE_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link EventBridgeConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via EventBridgeConfig.builder()
        }

        /**
         * Enables or disables the EventBridge service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link EventBridgeConfig} from this builder.
         *
         * @return the EventBridge configuration
         */
        public EventBridgeConfig build() {
            return new EventBridgeConfig(this);
        }
    }
}
