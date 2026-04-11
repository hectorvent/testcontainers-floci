package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for OpenSearch-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * OpenSearchConfig config = OpenSearchConfig.builder()
 *     .enabled(true)
 *     .mode("mock")
 *     .proxyPortRange(9400, 100)
 *     .build();
 * }</pre>
 */
public class OpenSearchConfig extends AbstractServiceConfig {

    private static final String DEFAULT_MODE = "mock";
    private static final String DEFAULT_IMAGE = "opensearchproject/opensearch:2";
    private static final int DEFAULT_PROXY_BASE_PORT = 9400;
    private static final int DEFAULT_PROXY_PORTS_COUNT = 10;

    private final String mode;
    private final String defaultImage;
    private final int proxyBasePort;
    private final int proxyPortsCount;
    private final String dockerNetwork;

    private OpenSearchConfig(Builder builder) {
        super(builder.enabled);
        this.mode = builder.mode;
        this.defaultImage = builder.defaultImage;
        this.proxyBasePort = builder.proxyBasePort;
        this.proxyPortsCount = builder.proxyPortsCount;
        this.dockerNetwork = builder.dockerNetwork;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the OpenSearch mode ({@code "mock"} or {@code "docker"}).
     *
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Returns the default Docker image used for OpenSearch instances.
     *
     * @return the image name
     */
    public String getDefaultImage() {
        return defaultImage;
    }

    /**
     * Returns the base port for the OpenSearch proxy port range.
     *
     * @return the base port
     */
    public int getProxyBasePort() {
        return proxyBasePort;
    }

    /**
     * Returns the number of ports allocated for the OpenSearch proxy, starting from {@link #getProxyBasePort()}.
     *
     * @return the number of proxy ports
     */
    public int getProxyPortsCount() {
        return proxyPortsCount;
    }

    /**
     * Returns the maximum port for the OpenSearch proxy port range.
     *
     * @return the maximum port
     */
    public int getProxyMaxPort() {
        return proxyBasePort + proxyPortsCount - 1;
    }

    /**
     * Returns the Docker network used for OpenSearch containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_OPENSEARCH_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_OPENSEARCH_MODE", mode);
            container.withEnv("FLOCI_SERVICES_OPENSEARCH_DEFAULT_IMAGE", defaultImage);
            container.withEnv("FLOCI_SERVICES_OPENSEARCH_PROXY_BASE_PORT", String.valueOf(proxyBasePort));
            container.withEnv("FLOCI_SERVICES_OPENSEARCH_PROXY_MAX_PORT", String.valueOf(getProxyMaxPort()));

            if (dockerNetwork != null) {
                container.withEnv("FLOCI_SERVICES_OPENSEARCH_DOCKER_NETWORK", dockerNetwork);
            }
        }
    }

    @Override
    public void applyExposedPortsToContainer(Container<?> container) {
        if (isEnabled()) {
            for (int port = proxyBasePort; port <= getProxyMaxPort(); port++) {
                container.addExposedPorts(port);
            }
        }
    }

    /**
     * Builder for {@link OpenSearchConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private String mode = DEFAULT_MODE;
        private String defaultImage = DEFAULT_IMAGE;
        private int proxyBasePort = DEFAULT_PROXY_BASE_PORT;
        private int proxyPortsCount = DEFAULT_PROXY_PORTS_COUNT;
        private String dockerNetwork;

        private Builder() {
            // Allow instantiation only via OpenSearchConfig.builder()
        }

        /**
         * Enables or disables the OpenSearch service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the OpenSearch mode.
         *
         * @param mode {@code "mock"} for in-memory or {@code "docker"} for real OpenSearch containers (default {@value DEFAULT_MODE})
         * @return this builder
         */
        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the default Docker image for OpenSearch instances.
         *
         * @param defaultImage the image name (default {@value DEFAULT_IMAGE})
         * @return this builder
         */
        public Builder defaultImage(String defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        /**
         * Sets the port range for the OpenSearch proxy.
         *
         * @param basePort the base port (default {@value DEFAULT_PROXY_BASE_PORT})
         * @param amount   the amount of ports (default {@value DEFAULT_PROXY_PORTS_COUNT})
         * @return this builder
         */
        public Builder proxyPortRange(int basePort, int amount) {
            this.proxyBasePort = basePort;
            this.proxyPortsCount = amount;
            return this;
        }

        /**
         * Sets the Docker network that OpenSearch containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use the default bridge
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Creates an immutable {@link OpenSearchConfig} from this builder.
         *
         * @return the OpenSearch configuration
         */
        public OpenSearchConfig build() {
            return new OpenSearchConfig(this);
        }
    }
}
