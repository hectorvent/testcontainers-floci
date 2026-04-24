package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Glue-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * GlueConfig config = GlueConfig.builder()
 *     .build();
 * }</pre>
 */
public class GlueConfig extends AbstractServiceConfig {


    private GlueConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_GLUE_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link GlueConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via GlueConfig.builder()
        }

        /**
         * Enables or disables the Glue service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link GlueConfig} from this builder.
         *
         * @return the Glue configuration
         */
        public GlueConfig build() {
            return new GlueConfig(this);
        }
    }
}
