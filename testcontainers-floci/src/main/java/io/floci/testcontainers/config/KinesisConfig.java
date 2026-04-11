package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Kinesis-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * KinesisConfig config = KinesisConfig.builder()
 *     .build();
 * }</pre>
 */
public class KinesisConfig extends AbstractServiceConfig {

    private KinesisConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_KINESIS_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link KinesisConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via KinesisConfig.builder()
        }

        /**
         * Enables or disables the Kinesis service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link KinesisConfig} from this builder.
         *
         * @return the Kinesis configuration
         */
        public KinesisConfig build() {
            return new KinesisConfig(this);
        }
    }
}
