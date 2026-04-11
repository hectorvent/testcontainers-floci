package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for IAM-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * IamConfig config = IamConfig.builder()
 *     .build();
 * }</pre>
 */
public class IamConfig extends AbstractServiceConfig {



    private IamConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_IAM_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link IamConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via IamConfig.builder()
        }

        /**
         * Enables or disables the IAM service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link IamConfig} from this builder.
         *
         * @return the IAM configuration
         */
        public IamConfig build() {
            return new IamConfig(this);
        }
    }
}
