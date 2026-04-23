package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Athena-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * AthenaConfig config = AthenaConfig.builder()
 *     .build();
 * }</pre>
 */
public class AthenaConfig extends AbstractServiceConfig {


    private AthenaConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_ATHENA_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link AthenaConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via AthenaConfig.builder()
        }

        /**
         * Enables or disables the Athena service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link AthenaConfig} from this builder.
         *
         * @return the Athena configuration
         */
        public AthenaConfig build() {
            return new AthenaConfig(this);
        }
    }
}
