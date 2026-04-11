package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for SES-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SesConfig config = SesConfig.builder()
 *     .build();
 * }</pre>
 */
public class SesConfig extends AbstractServiceConfig {

    private SesConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_SES_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link SesConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via SesConfig.builder()
        }

        /**
         * Enables or disables the SES service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link SesConfig} from this builder.
         *
         * @return the SES configuration
         */
        public SesConfig build() {
            return new SesConfig(this);
        }
    }
}
