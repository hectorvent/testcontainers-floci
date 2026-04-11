package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for S3-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * S3Config config = S3Config.builder()
 *     .defaultPresignExpirySeconds(7200)
 *     .build();
 * }</pre>
 */
public class S3Config extends AbstractServiceConfig {

    private static final int DEFAULT_PRESIGN_EXPIRY_SECONDS = 3600;

    private final int defaultPresignExpirySeconds;

    private S3Config(Builder builder) {
        super(builder.enabled);
        this.defaultPresignExpirySeconds = builder.defaultPresignExpirySeconds;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the default presign expiry in seconds.
     *
     * @return the default presign expiry in seconds
     */
    public int getDefaultPresignExpirySeconds() {
        return defaultPresignExpirySeconds;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_S3_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_S3_DEFAULT_PRESIGN_EXPIRY_SECONDS", String.valueOf(defaultPresignExpirySeconds));
        }
    }

    /**
     * Builder for {@link S3Config}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int defaultPresignExpirySeconds = DEFAULT_PRESIGN_EXPIRY_SECONDS;

        private Builder() {
            // Allow instantiation only via S3Config.builder()
        }

        /**
         * Enables or disables the S3 service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the default expiry time for presigned URLs in seconds.
         *
         * @param defaultPresignExpirySeconds the default expiry time for presigned URLs in seconds (default {@value DEFAULT_PRESIGN_EXPIRY_SECONDS})
         * @return this builder
         */
        public Builder defaultPresignExpirySeconds(int defaultPresignExpirySeconds) {
            this.defaultPresignExpirySeconds = defaultPresignExpirySeconds;
            return this;
        }

        /**
         * Creates an immutable {@link S3Config} from this builder.
         *
         * @return the S3 configuration
         */
        public S3Config build() {
            return new S3Config(this);
        }
    }
}
