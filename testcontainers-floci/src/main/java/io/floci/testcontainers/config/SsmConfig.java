package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for SSM-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SsmConfig config = SsmConfig.builder()
 *     .maxParameterHistory(10)
 *     .build();
 * }</pre>
 */
public class SsmConfig extends AbstractServiceConfig {

    private static final int DEFAULT_MAX_PARAMETER_HISTORY = 5;

    private final int maxParameterHistory;

    private SsmConfig(Builder builder) {
        super(builder.enabled);
        this.maxParameterHistory = builder.maxParameterHistory;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the maximum parameter history count.
     *
     * @return the maximum parameter history count
     */
    public int getMaxParameterHistory() {
        return maxParameterHistory;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_SSM_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_SSM_MAX_PARAMETER_HISTORY", String.valueOf(maxParameterHistory));
        }
    }

    /**
     * Builder for {@link SsmConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int maxParameterHistory = DEFAULT_MAX_PARAMETER_HISTORY;

        private Builder() {
            // Allow instantiation only via SsmConfig.builder()
        }

        /**
         * Enables or disables the SSM service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the maximum number of parameter history entries to retain.
         *
         * @param maxParameterHistory the maximum number of parameter history entries to retain (default {@value DEFAULT_MAX_PARAMETER_HISTORY})
         * @return this builder
         */
        public Builder maxParameterHistory(int maxParameterHistory) {
            this.maxParameterHistory = maxParameterHistory;
            return this;
        }

        /**
         * Creates an immutable {@link SsmConfig} from this builder.
         *
         * @return the SSM configuration
         */
        public SsmConfig build() {
            return new SsmConfig(this);
        }
    }
}
