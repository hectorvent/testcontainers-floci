package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for MSK (Managed Streaming for Apache Kafka)-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * MskConfig config = MskConfig.builder()
 *     .enabled(true)
 *     .mock(false)
 *     .defaultImage("redpandadata/redpanda:latest")
 *     .build();
 * }</pre>
 */
public class MskConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_MOCK = false;
    private static final String DEFAULT_IMAGE = "redpandadata/redpanda:latest";

    private final boolean mock;
    private final String defaultImage;

    private MskConfig(Builder builder) {
        super(builder.enabled);
        this.mock = builder.mock;
        this.defaultImage = builder.defaultImage;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether MSK clusters are simulated in-memory without real Docker containers.
     *
     * @return {@code true} if mock mode is enabled
     */
    public boolean isMock() {
        return mock;
    }

    /**
     * Returns the default Docker image used for MSK (Redpanda) instances.
     *
     * @return the image name
     */
    public String getDefaultImage() {
        return defaultImage;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_MSK_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_MSK_MOCK", String.valueOf(mock));
            container.withEnv("FLOCI_SERVICES_MSK_DEFAULT_IMAGE", defaultImage);
        }
    }

    /**
     * Builder for {@link MskConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean mock = DEFAULT_MOCK;
        private String defaultImage = DEFAULT_IMAGE;

        private Builder() {
            // Allow instantiation only via MskConfig.builder()
        }

        /**
         * Enables or disables the MSK service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets whether MSK clusters are simulated in-memory without real Docker containers.
         *
         * @param mock {@code true} to enable mock mode (default {@value DEFAULT_MOCK})
         * @return this builder
         */
        public Builder mock(boolean mock) {
            this.mock = mock;
            return this;
        }

        /**
         * Sets the default Docker image for MSK (Redpanda) instances.
         *
         * @param defaultImage the image name (default {@value DEFAULT_IMAGE})
         * @return this builder
         */
        public Builder defaultImage(String defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        /**
         * Creates an immutable {@link MskConfig} from this builder.
         *
         * @return the MSK configuration
         */
        public MskConfig build() {
            return new MskConfig(this);
        }
    }
}
