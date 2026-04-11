package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for Secrets Manager-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SecretsManagerConfig config = SecretsManagerConfig.builder()
 *     .defaultRecoveryWindowDays(7)
 *     .build();
 * }</pre>
 */
public class SecretsManagerConfig extends AbstractServiceConfig {

    private static final int DEFAULT_RECOVERY_WINDOW_DAYS = 30;

    private final int defaultRecoveryWindowDays;

    private SecretsManagerConfig(Builder builder) {
        super(builder.enabled);
        this.defaultRecoveryWindowDays = builder.defaultRecoveryWindowDays;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the default recovery window in days.
     *
     * @return the default recovery window in days
     */
    public int getDefaultRecoveryWindowDays() {
        return defaultRecoveryWindowDays;
    }

    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_SECRETSMANAGER_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_SECRETSMANAGER_DEFAULT_RECOVERY_WINDOW_DAYS", String.valueOf(defaultRecoveryWindowDays));
        }
    }

    /**
     * Builder for {@link SecretsManagerConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int defaultRecoveryWindowDays = DEFAULT_RECOVERY_WINDOW_DAYS;

        private Builder() {
            // Allow instantiation only via SecretsManagerConfig.builder()
        }

        /**
         * Enables or disables the Secrets Manager service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the default recovery window in days for deleted secrets.
         *
         * @param defaultRecoveryWindowDays the default recovery window in days for deleted secrets (default {@value DEFAULT_RECOVERY_WINDOW_DAYS})
         * @return this builder
         */
        public Builder defaultRecoveryWindowDays(int defaultRecoveryWindowDays) {
            this.defaultRecoveryWindowDays = defaultRecoveryWindowDays;
            return this;
        }

        /**
         * Creates an immutable {@link SecretsManagerConfig} from this builder.
         *
         * @return the Secrets Manager configuration
         */
        public SecretsManagerConfig build() {
            return new SecretsManagerConfig(this);
        }
    }
}
