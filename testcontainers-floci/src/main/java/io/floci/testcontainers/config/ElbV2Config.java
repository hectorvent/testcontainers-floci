package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for ELBv2 (Elastic Load Balancing v2)-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * ElbV2Config config = ElbV2Config.builder()
 *     .build();
 * }</pre>
 */
public class ElbV2Config extends AbstractServiceConfig {


    private ElbV2Config(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_ELBV2_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link ElbV2Config}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via ElbV2Config.builder()
        }

        /**
         * Enables or disables the ELBv2 service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link ElbV2Config} from this builder.
         *
         * @return the ELBv2 configuration
         */
        public ElbV2Config build() {
            return new ElbV2Config(this);
        }
    }
}
