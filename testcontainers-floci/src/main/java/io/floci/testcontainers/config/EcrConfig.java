package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for ECR-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * EcrConfig config = EcrConfig.builder()
 *     .enabled(true)
 *     .registryPortRange(5000, 100)
 *     .registryImage("registry:2")
 *     .build();
 * }</pre>
 */
public class EcrConfig extends AbstractServiceConfig {

    private static final String DEFAULT_REGISTRY_IMAGE = "registry:2";
    private static final String DEFAULT_REGISTRY_CONTAINER_NAME = "floci-ecr-registry";
    private static final int DEFAULT_REGISTRY_BASE_PORT = 5100;
    private static final int DEFAULT_REGISTRY_PORTS_COUNT = 10;
    private static final boolean DEFAULT_TLS_ENABLED = false;
    private static final String DEFAULT_URI_STYLE = "hostname";

    private final String registryImage;
    private final String registryContainerName;
    private final int registryBasePort;
    private final int registryPortsCount;
    private final boolean tlsEnabled;
    private final String uriStyle;
    private final String dockerNetwork;

    private EcrConfig(Builder builder) {
        super(builder.enabled);
        this.registryImage = builder.registryImage;
        this.registryContainerName = builder.registryContainerName;
        this.registryBasePort = builder.registryBasePort;
        this.registryPortsCount = builder.registryPortsCount;
        this.tlsEnabled = builder.tlsEnabled;
        this.uriStyle = builder.uriStyle;
        this.dockerNetwork = builder.dockerNetwork;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the Docker image used for the ECR registry.
     *
     * @return the registry image name
     */
    public String getRegistryImage() {
        return registryImage;
    }

    /**
     * Returns the container name for the ECR registry.
     *
     * @return the registry container name
     */
    public String getRegistryContainerName() {
        return registryContainerName;
    }

    /**
     * Returns the base port for the ECR registry port range.
     *
     * @return the base port
     */
    public int getRegistryBasePort() {
        return registryBasePort;
    }

    /**
     * Returns the number of ports allocated for the ECR registry, starting from {@link #getRegistryBasePort()}.
     *
     * @return the number of registry ports
     */
    public int getRegistryPortsCount() {
        return registryPortsCount;
    }

    /**
     * Returns the maximum port for the ECR registry port range.
     *
     * @return the maximum port
     */
    public int getRegistryMaxPort() {
        return registryBasePort + registryPortsCount - 1;
    }

    /**
     * Returns whether TLS is enabled for the ECR registry.
     *
     * @return {@code true} if TLS is enabled
     */
    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    /**
     * Returns the URI style for repositoryUri responses.
     *
     * @return {@code "hostname"} or {@code "path"}
     */
    public String getUriStyle() {
        return uriStyle;
    }

    /**
     * Returns the Docker network used for ECR registry containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_ECR_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_ECR_REGISTRY_IMAGE", registryImage);
            container.withEnv("FLOCI_SERVICES_ECR_REGISTRY_CONTAINER_NAME", registryContainerName);
            container.withEnv("FLOCI_SERVICES_ECR_REGISTRY_BASE_PORT", String.valueOf(registryBasePort));
            container.withEnv("FLOCI_SERVICES_ECR_REGISTRY_MAX_PORT", String.valueOf(getRegistryMaxPort()));
            container.withEnv("FLOCI_SERVICES_ECR_TLS_ENABLED", String.valueOf(tlsEnabled));
            container.withEnv("FLOCI_SERVICES_ECR_URI_STYLE", uriStyle);
            container.withEnv("FLOCI_SERVICES_ECR_KEEP_RUNNING_ON_SHUTDOWN", "false");

            if (dockerNetwork != null) {
                container.withEnv("FLOCI_SERVICES_ECR_DOCKER_NETWORK", dockerNetwork);
            }
        }
    }

    @Override
    public void applyExposedPortsToContainer(Container<?> container) {
        if (isEnabled()) {
            for (int port = registryBasePort; port <= getRegistryMaxPort(); port++) {
                container.addExposedPorts(port);
            }
        }
    }

    /**
     * Builder for {@link EcrConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private String registryImage = DEFAULT_REGISTRY_IMAGE;
        private String registryContainerName = DEFAULT_REGISTRY_CONTAINER_NAME;
        private int registryBasePort = DEFAULT_REGISTRY_BASE_PORT;
        private int registryPortsCount = DEFAULT_REGISTRY_PORTS_COUNT;
        private boolean tlsEnabled = DEFAULT_TLS_ENABLED;
        private String uriStyle = DEFAULT_URI_STYLE;
        private String dockerNetwork;

        private Builder() {
            // Allow instantiation only via EcrConfig.builder()
        }

        /**
         * Enables or disables the ECR service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the Docker image for the ECR registry.
         *
         * @param registryImage the image name (default {@value DEFAULT_REGISTRY_IMAGE})
         * @return this builder
         */
        public Builder registryImage(String registryImage) {
            this.registryImage = registryImage;
            return this;
        }

        /**
         * Sets the container name for the ECR registry.
         *
         * @param registryContainerName the container name (default {@value DEFAULT_REGISTRY_CONTAINER_NAME})
         * @return this builder
         */
        public Builder registryContainerName(String registryContainerName) {
            this.registryContainerName = registryContainerName;
            return this;
        }

        /**
         * Sets the port range for the ECR registry.
         *
         * @param basePort the base port (default {@value DEFAULT_REGISTRY_BASE_PORT})
         * @param amount   the amount of ports (default {@value DEFAULT_REGISTRY_PORTS_COUNT})
         * @return this builder
         */
        public Builder registryPortRange(int basePort, int amount) {
            this.registryBasePort = basePort;
            this.registryPortsCount = amount;
            return this;
        }

        /**
         * Sets whether TLS is enabled for the ECR registry.
         *
         * @param tlsEnabled {@code true} to enable TLS (default {@value DEFAULT_TLS_ENABLED})
         * @return this builder
         */
        public Builder tlsEnabled(boolean tlsEnabled) {
            this.tlsEnabled = tlsEnabled;
            return this;
        }

        /**
         * Sets the URI style for repositoryUri responses.
         *
         * @param uriStyle {@code "hostname"} or {@code "path"} (default {@value DEFAULT_URI_STYLE})
         * @return this builder
         */
        public Builder uriStyle(String uriStyle) {
            this.uriStyle = uriStyle;
            return this;
        }

        /**
         * Sets the Docker network that ECR registry containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use the default bridge
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Creates an immutable {@link EcrConfig} from this builder.
         *
         * @return the ECR configuration
         */
        public EcrConfig build() {
            return new EcrConfig(this);
        }
    }
}
