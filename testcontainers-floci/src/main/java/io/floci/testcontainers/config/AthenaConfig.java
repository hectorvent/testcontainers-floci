package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Athena-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * AthenaConfig config = AthenaConfig.builder()
 *     .mock(false)
 *     .defaultImage("floci/floci-duck:latest")
 *     .build();
 * }</pre>
 */
public class AthenaConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_MOCK = false;
    private static final String DEFAULT_IMAGE = "floci/floci-duck:latest";

    private final boolean mock;
    private final String duckUrl;
    private final String defaultImage;

    private AthenaConfig(Builder builder) {
        super(builder.enabled);
        this.mock = builder.mock;
        this.duckUrl = builder.duckUrl;
        this.defaultImage = builder.defaultImage;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether Athena operates in mock mode (no real DuckDB backend).
     *
     * @return {@code true} if mock mode is enabled
     */
    public boolean isMock() {
        return mock;
    }

    /**
     * Returns the custom DuckDB URL, or {@code null} if Floci manages the container itself.
     *
     * @return the DuckDB URL, or {@code null}
     */
    public String getDuckUrl() {
        return duckUrl;
    }

    /**
     * Returns the default Docker image used for the floci-duck container.
     *
     * @return the image name
     */
    public String getDefaultImage() {
        return defaultImage;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_ATHENA_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_ATHENA_MOCK", String.valueOf(mock));
            container.withEnv("FLOCI_SERVICES_ATHENA_DEFAULT_IMAGE", defaultImage);

            if (duckUrl != null) {
                container.withEnv("FLOCI_SERVICES_ATHENA_DUCK_URL", duckUrl);
            }
        }
    }

    /**
     * Builder for {@link AthenaConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean mock = DEFAULT_MOCK;
        private String duckUrl;
        private String defaultImage = DEFAULT_IMAGE;

        private Builder() {
            // Allow instantiation only via AthenaConfig.builder()
        }

        /**
         * Enables or disables the Athena service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets whether Athena operates in mock mode (no real DuckDB backend).
         *
         * @param mock {@code true} to enable mock mode (default {@value DEFAULT_MOCK})
         * @return this builder
         */
        public Builder mock(boolean mock) {
            this.mock = mock;
            return this;
        }

        /**
         * Sets a custom DuckDB URL. When set, Floci uses this URL and skips floci-duck container management.
         *
         * @param duckUrl the DuckDB URL, or {@code null} to let Floci manage the container
         * @return this builder
         */
        public Builder duckUrl(String duckUrl) {
            this.duckUrl = duckUrl;
            return this;
        }

        /**
         * Sets the default Docker image for the floci-duck container.
         *
         * @param defaultImage the image name (default {@value DEFAULT_IMAGE})
         * @return this builder
         */
        public Builder defaultImage(String defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        /**
         * Creates an immutable {@link AthenaConfig} from this builder.
         *
         * @return the Athena configuration
         */
        public AthenaConfig build() {
            return new AthenaConfig(this);
        }
    }
}
