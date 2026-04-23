package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for EKS (Elastic Kubernetes Service)-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * EksConfig config = EksConfig.builder()
 *     .enabled(true)
 *     .mock(false)
 *     .provider("k3s")
 *     .apiServerPortRange(6500, 100)
 *     .build();
 * }</pre>
 */
public class EksConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_MOCK = false;
    private static final String DEFAULT_PROVIDER = "k3s";
    private static final String DEFAULT_IMAGE = "rancher/k3s:latest";
    private static final int DEFAULT_API_SERVER_BASE_PORT = 6500;
    private static final int DEFAULT_API_SERVER_PORTS_COUNT = 10;

    private final boolean mock;
    private final String provider;
    private final String defaultImage;
    private final int apiServerBasePort;
    private final int apiServerPortsCount;
    private final String dockerNetwork;

    private EksConfig(Builder builder) {
        super(builder.enabled);
        this.mock = builder.mock;
        this.provider = builder.provider;
        this.defaultImage = builder.defaultImage;
        this.apiServerBasePort = builder.apiServerBasePort;
        this.apiServerPortsCount = builder.apiServerPortsCount;
        this.dockerNetwork = builder.dockerNetwork;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether clusters go straight to ACTIVE without starting real Docker containers.
     *
     * @return {@code true} if mock mode is enabled
     */
    public boolean isMock() {
        return mock;
    }

    /**
     * Returns the Kubernetes provider used for EKS clusters.
     *
     * @return the provider name
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the default Docker image used for EKS (k3s) instances.
     *
     * @return the image name
     */
    public String getDefaultImage() {
        return defaultImage;
    }

    /**
     * Returns the base port for the EKS API server port range.
     *
     * @return the base port
     */
    public int getApiServerBasePort() {
        return apiServerBasePort;
    }

    /**
     * Returns the number of ports allocated for the EKS API server, starting from {@link #getApiServerBasePort()}.
     *
     * @return the number of API server ports
     */
    public int getApiServerPortsCount() {
        return apiServerPortsCount;
    }

    /**
     * Returns the maximum port for the EKS API server port range.
     *
     * @return the maximum port
     */
    public int getApiServerMaxPort() {
        return apiServerBasePort + apiServerPortsCount - 1;
    }

    /**
     * Returns the Docker network used for EKS containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_EKS_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_EKS_MOCK", String.valueOf(mock));
            container.withEnv("FLOCI_SERVICES_EKS_PROVIDER", provider);
            container.withEnv("FLOCI_SERVICES_EKS_DEFAULT_IMAGE", defaultImage);
            container.withEnv("FLOCI_SERVICES_EKS_API_SERVER_BASE_PORT", String.valueOf(apiServerBasePort));
            container.withEnv("FLOCI_SERVICES_EKS_API_SERVER_MAX_PORT", String.valueOf(getApiServerMaxPort()));

            if (dockerNetwork != null) {
                container.withEnv("FLOCI_SERVICES_EKS_DOCKER_NETWORK", dockerNetwork);
            }
        }
    }

    @Override
    public void applyExposedPortsToContainer(Container<?> container) {
        if (isEnabled()) {
            for (int port = apiServerBasePort; port <= getApiServerMaxPort(); port++) {
                container.addExposedPorts(port);
            }
        }
    }

    /**
     * Builder for {@link EksConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean mock = DEFAULT_MOCK;
        private String provider = DEFAULT_PROVIDER;
        private String defaultImage = DEFAULT_IMAGE;
        private int apiServerBasePort = DEFAULT_API_SERVER_BASE_PORT;
        private int apiServerPortsCount = DEFAULT_API_SERVER_PORTS_COUNT;
        private String dockerNetwork;

        private Builder() {
            // Allow instantiation only via EksConfig.builder()
        }

        /**
         * Enables or disables the EKS service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets whether clusters go straight to ACTIVE without starting real Docker containers.
         *
         * @param mock {@code true} to enable mock mode (default {@value DEFAULT_MOCK})
         * @return this builder
         */
        public Builder mock(boolean mock) {
            this.mock = mock;
            return this;
        }

        /**
         * Sets the Kubernetes provider used for EKS clusters.
         *
         * @param provider the provider name (default {@value DEFAULT_PROVIDER})
         * @return this builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the default Docker image for EKS (k3s) instances.
         *
         * @param defaultImage the image name (default {@value DEFAULT_IMAGE})
         * @return this builder
         */
        public Builder defaultImage(String defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        /**
         * Sets the port range for the EKS API server.
         *
         * @param basePort the base port (default {@value DEFAULT_API_SERVER_BASE_PORT})
         * @param amount   the amount of ports (default {@value DEFAULT_API_SERVER_PORTS_COUNT})
         * @return this builder
         */
        public Builder apiServerPortRange(int basePort, int amount) {
            this.apiServerBasePort = basePort;
            this.apiServerPortsCount = amount;
            return this;
        }

        /**
         * Sets the Docker network that EKS containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use the default bridge
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Creates an immutable {@link EksConfig} from this builder.
         *
         * @return the EKS configuration
         */
        public EksConfig build() {
            return new EksConfig(this);
        }
    }
}
