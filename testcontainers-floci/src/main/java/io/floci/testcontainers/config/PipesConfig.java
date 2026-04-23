package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Pipes-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * PipesConfig config = PipesConfig.builder()
 *     .build();
 * }</pre>
 */
public class PipesConfig extends AbstractServiceConfig {


    private PipesConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_PIPES_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link PipesConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via PipesConfig.builder()
        }

        /**
         * Enables or disables the Pipes service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link PipesConfig} from this builder.
         *
         * @return the Pipes configuration
         */
        public PipesConfig build() {
            return new PipesConfig(this);
        }
    }
}
