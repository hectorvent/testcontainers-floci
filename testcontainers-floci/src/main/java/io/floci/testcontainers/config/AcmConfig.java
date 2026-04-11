package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for ACM-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * AcmConfig config = AcmConfig.builder()
 *     .validationWaitSeconds(5)
 *     .build();
 * }</pre>
 */
public class AcmConfig extends AbstractServiceConfig {

    private static final int DEFAULT_VALIDATION_WAIT_SECONDS = 0;

    private final int validationWaitSeconds;

    private AcmConfig(Builder builder) {
        super(builder.enabled);
        this.validationWaitSeconds = builder.validationWaitSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the validation wait time in seconds.
     *
     * @return the validation wait time in seconds
     */
    public int getValidationWaitSeconds() {
        return validationWaitSeconds;
    }

    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_ACM_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_ACM_VALIDATION_WAIT_SECONDS", String.valueOf(validationWaitSeconds));
        }
    }

    /**
     * Builder for {@link AcmConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int validationWaitSeconds = DEFAULT_VALIDATION_WAIT_SECONDS;

        private Builder() {
            // Allow instantiation only via AcmConfig.builder()
        }

        /**
         * Enables or disables the ACM service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the number of seconds to wait before transitioning from PENDING_VALIDATION to ISSUED (0 = immediate).
         *
         * @param validationWaitSeconds the number of seconds to wait before transitioning from PENDING_VALIDATION to ISSUED (0 = immediate) (default {@value DEFAULT_VALIDATION_WAIT_SECONDS})
         * @return this builder
         */
        public Builder validationWaitSeconds(int validationWaitSeconds) {
            this.validationWaitSeconds = validationWaitSeconds;
            return this;
        }

        /**
         * Creates an immutable {@link AcmConfig} from this builder.
         *
         * @return the ACM configuration
         */
        public AcmConfig build() {
            return new AcmConfig(this);
        }
    }
}
