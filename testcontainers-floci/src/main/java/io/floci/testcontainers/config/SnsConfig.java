package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for SNS-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SnsConfig config = SnsConfig.builder()
 *     .build();
 * }</pre>
 */
public class SnsConfig extends AbstractServiceConfig {

    private SnsConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_SNS_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link SnsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via SnsConfig.builder()
        }

        /**
         * Enables or disables the SNS service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link SnsConfig} from this builder.
         *
         * @return the SNS configuration
         */
        public SnsConfig build() {
            return new SnsConfig(this);
        }
    }
}
