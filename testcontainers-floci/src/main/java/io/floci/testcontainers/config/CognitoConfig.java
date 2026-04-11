package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Cognito-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * CognitoConfig config = CognitoConfig.builder()
 *     .build();
 * }</pre>
 */
public class CognitoConfig extends AbstractServiceConfig {


    private CognitoConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_COGNITO_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link CognitoConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via CognitoConfig.builder()
        }

        /**
         * Enables or disables the Cognito service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link CognitoConfig} from this builder.
         *
         * @return the Cognito configuration
         */
        public CognitoConfig build() {
            return new CognitoConfig(this);
        }
    }
}
