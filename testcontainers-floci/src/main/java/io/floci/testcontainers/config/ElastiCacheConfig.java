package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for ElastiCache-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * ElastiCacheConfig config = ElastiCacheConfig.builder()
 *     .enabled(true)
 *     .proxyPortRange(6379, 20)
 *     .defaultImage("valkey/valkey:8")
 *     .build();
 * }</pre>
 */
public class ElastiCacheConfig extends AbstractServiceConfig {

    private static final int DEFAULT_PROXY_BASE_PORT = 6379;
    private static final int DEFAULT_PROXY_PORTS_COUNT = 10;
    private static final String DEFAULT_IMAGE = "valkey/valkey:8";

    private final int proxyBasePort;
    private final int proxyPortsCount;
    private final String defaultImage;
    private final String dockerNetwork;

    private ElastiCacheConfig(Builder builder) {
        super(builder.enabled);
        this.proxyBasePort = builder.proxyBasePort;
        this.proxyPortsCount = builder.proxyPortsCount;
        this.defaultImage = builder.defaultImage;
        this.dockerNetwork = builder.dockerNetwork;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the base port for the ElastiCache proxy port range.
     *
     * @return the base port
     */
    public int getProxyBasePort() {
        return proxyBasePort;
    }

    /**
     * Returns the number of ports allocated for the ElastiCache proxy, starting from {@link #getProxyBasePort()}.
     *
     * @return the number of proxy ports
     */
    public int getProxyPortsCount() {
        return proxyPortsCount;
    }

    /**
     * Returns the maximum port for the ElastiCache proxy port range.
     *
     * @return the maximum port
     */
    public int getProxyMaxPort() {
        return proxyBasePort + proxyPortsCount - 1;
    }

    /**
     * Returns the default Docker image used for ElastiCache (Valkey) instances.
     *
     * @return the image name
     */
    public String getDefaultImage() {
        return defaultImage;
    }

    /**
     * Returns the Docker network used for ElastiCache containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_ELASTICACHE_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_ELASTICACHE_PROXY_BASE_PORT", String.valueOf(proxyBasePort));
            container.withEnv("FLOCI_SERVICES_ELASTICACHE_PROXY_MAX_PORT", String.valueOf(getProxyMaxPort()));
            container.withEnv("FLOCI_SERVICES_ELASTICACHE_DEFAULT_IMAGE", defaultImage);

            if (dockerNetwork != null) {
                container.withEnv("FLOCI_SERVICES_ELASTICACHE_DOCKER_NETWORK", dockerNetwork);
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
     * Builder for {@link ElastiCacheConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int proxyBasePort = DEFAULT_PROXY_BASE_PORT;
        private int proxyPortsCount = DEFAULT_PROXY_PORTS_COUNT;
        private String defaultImage = DEFAULT_IMAGE;
        private String dockerNetwork;

        private Builder() {
            // Allow instantiation only via ElastiCacheConfig.builder()
        }

        /**
         * Enables or disables the ElastiCache service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the port range for the ElastiCache proxy.
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
         * Sets the default Docker image for ElastiCache (Valkey) instances.
         *
         * @param defaultImage the image name (default {@value DEFAULT_IMAGE})
         * @return this builder
         */
        public Builder defaultImage(String defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        /**
         * Sets the Docker network that ElastiCache containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use the default bridge
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Creates an immutable {@link ElastiCacheConfig} from this builder.
         *
         * @return the ElastiCache configuration
         */
        public ElastiCacheConfig build() {
            return new ElastiCacheConfig(this);
        }
    }
}
