package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for KMS-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * KmsConfig config = KmsConfig.builder()
 *     .build();
 * }</pre>
 */
public class KmsConfig extends AbstractServiceConfig {

    private KmsConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_KMS_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link KmsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via KmsConfig.builder()
        }

        /**
         * Enables or disables the KMS service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link KmsConfig} from this builder.
         *
         * @return the KMS configuration
         */
        public KmsConfig build() {
            return new KmsConfig(this);
        }
    }
}
