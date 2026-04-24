package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for IAM-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * IamConfig config = IamConfig.builder()
 *     .enforcementEnabled(true)
 *     .build();
 * }</pre>
 */
public class IamConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_ENFORCEMENT_ENABLED = false;

    private final boolean enforcementEnabled;

    private IamConfig(Builder builder) {
        super(builder.enabled);
        this.enforcementEnabled = builder.enforcementEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns whether IAM enforcement is enabled.
     *
     * @return {@code true} if IAM enforcement is enabled
     */
    public boolean isEnforcementEnabled() {
        return enforcementEnabled;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_IAM_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_IAM_ENFORCEMENT_ENABLED", String.valueOf(enforcementEnabled));
        }
    }

    /**
     * Builder for {@link IamConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean enforcementEnabled = DEFAULT_ENFORCEMENT_ENABLED;

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
         * Enables or disables IAM enforcement.
         *
         * @param enforcementEnabled {@code true} to enable enforcement (default {@value DEFAULT_ENFORCEMENT_ENABLED})
         * @return this builder
         */
        public Builder enforcementEnabled(boolean enforcementEnabled) {
            this.enforcementEnabled = enforcementEnabled;
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
