package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for Lambda-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * LambdaConfig config = LambdaConfig.builder()
 *     .enabled(true)
 *     .runtimeApiPortRange(9300, 10)
 *     .defaultMemoryMb(256)
 *     .build();
 * }</pre>
 */
public class LambdaConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_EPHEMERAL = false;
    private static final boolean DEFAULT_EXPOSE_RUNTIME_PORTS = false;
    private static final int DEFAULT_MEMORY_MB = 128;
    private static final int DEFAULT_TIMEOUT_SECONDS = 3;
    private static final int DEFAULT_RUNTIME_API_BASE_PORT = 9200;
    private static final int DEFAULT_RUNTIME_API_PORTS_COUNT = 10;
    private static final int DEFAULT_POLL_INTERVAL_MS = 1000;
    private static final int DEFAULT_CONTAINER_IDLE_TIMEOUT_SECONDS = 300;

    private final boolean ephemeral;
    private final boolean exposeRuntimePorts;
    private final int defaultMemoryMb;
    private final int defaultTimeoutSeconds;
    private final String dockerNetwork;
    private final int runtimeApiBasePort;
    private final int runtimeApiPortsCount;
    private final int pollIntervalMs;
    private final int containerIdleTimeoutSeconds;

    private LambdaConfig(Builder builder) {
        super(builder.enabled);
        this.ephemeral = builder.ephemeral;
        this.exposeRuntimePorts=builder.exposeRuntimePorts;
        this.defaultMemoryMb = builder.defaultMemoryMb;
        this.defaultTimeoutSeconds = builder.defaultTimeoutSeconds;
        this.dockerNetwork = builder.dockerNetwork;
        this.runtimeApiBasePort = builder.runtimeApiBasePort;
        this.runtimeApiPortsCount = builder.runtimeApiPortsCount;
        this.pollIntervalMs = builder.pollIntervalMs;
        this.containerIdleTimeoutSeconds = builder.containerIdleTimeoutSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether Lambda containers are removed after each invocation.
     *
     * @return {@code true} if containers are ephemeral
     */
    public boolean isEphemeral() {
        return ephemeral;
    }

    /**
     * Returns whether the Lambda Runtime API ports should be exposed on the host machine.
     *
     * @return {@code true} if Runtime API ports are exposed
     */
    public boolean isExposeRuntimePorts() {
        return exposeRuntimePorts;
    }

    /**
     * Returns the default memory size for Lambda functions in megabytes.
     *
     * @return memory in MB
     */
    public int getDefaultMemoryMb() {
        return defaultMemoryMb;
    }

    /**
     * Returns the default timeout for Lambda function invocations in seconds.
     *
     * @return timeout in seconds
     */
    public int getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    /**
     * Returns the Docker network used for Lambda containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    /**
     * Returns the base port for the Lambda Runtime API port range.
     *
     * @return the base port
     */
    public int getRuntimeApiBasePort() {
        return runtimeApiBasePort;
    }

    /**
     * Returns the number of ports allocated for the Lambda Runtime API.
     *
     * @return the port count
     */
    public int getRuntimeApiPortsCount() {
        return runtimeApiPortsCount;
    }

    /**
     * Returns the highest port for the Lambda Runtime API port range.
     *
     * @return the highest port in the range
     */
    public int getRuntimeApiMaxPort() {
        return runtimeApiBasePort + runtimeApiPortsCount - 1;
    }

    /**
     * Returns the poll interval in milliseconds for Lambda container status checks.
     *
     * @return interval in milliseconds
     */
    public int getPollIntervalMs() {
        return pollIntervalMs;
    }

    /**
     * Returns the idle timeout in seconds after which unused Lambda containers are
     * cleaned up.
     *
     * @return timeout in seconds
     */
    public int getContainerIdleTimeoutSeconds() {
        return containerIdleTimeoutSeconds;
    }

    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_LAMBDA_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_LAMBDA_EPHEMERAL", String.valueOf(ephemeral));
            container.withEnv("FLOCI_SERVICES_LAMBDA_DEFAULT_MEMORY_MB", String.valueOf(defaultMemoryMb));
            container.withEnv("FLOCI_SERVICES_LAMBDA_DEFAULT_TIMEOUT_SECONDS", String.valueOf(defaultTimeoutSeconds));
            container.withEnv("FLOCI_SERVICES_LAMBDA_RUNTIME_API_BASE_PORT", String.valueOf(runtimeApiBasePort));
            container.withEnv("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", String.valueOf(getRuntimeApiMaxPort()));
            container.withEnv("FLOCI_SERVICES_LAMBDA_POLL_INTERVAL_MS", String.valueOf(pollIntervalMs));
            container.withEnv("FLOCI_SERVICES_LAMBDA_CONTAINER_IDLE_TIMEOUT_SECONDS", String.valueOf(containerIdleTimeoutSeconds));

            if (dockerNetwork != null) {
                container.withEnv("FLOCI_SERVICES_LAMBDA_DOCKER_NETWORK", dockerNetwork);
            }
        }
    }

    /**
     * Builder for {@link LambdaConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean ephemeral = DEFAULT_EPHEMERAL;
        private boolean exposeRuntimePorts = DEFAULT_EXPOSE_RUNTIME_PORTS;
        private int defaultMemoryMb = DEFAULT_MEMORY_MB;
        private int defaultTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        private String dockerNetwork;
        private int runtimeApiBasePort = DEFAULT_RUNTIME_API_BASE_PORT;
        private int runtimeApiPortsCount = DEFAULT_RUNTIME_API_PORTS_COUNT;
        private int pollIntervalMs = DEFAULT_POLL_INTERVAL_MS;
        private int containerIdleTimeoutSeconds = DEFAULT_CONTAINER_IDLE_TIMEOUT_SECONDS;

        private Builder(){
            // Allow instantiation only via LambdaConfig.builder()
        }

        /**
         * Enables or disables the Lambda service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets whether Lambda containers are removed after each invocation.
         *
         * @param ephemeral {@code true} to remove containers after each invocation (default {@value DEFAULT_EPHEMERAL})
         * @return this builder
         */
        public Builder ephemeral(boolean ephemeral) {
            this.ephemeral = ephemeral;
            return this;
        }

        /**
         * Sets whether the Lambda Runtime API ports should be exposed on the host machine.
         *
         * @param exposeRuntimePorts {@code true} to expose Runtime API ports (default {@value DEFAULT_EXPOSE_RUNTIME_PORTS})
         * @return this builder
         */
        public Builder exposeRuntimePorts(boolean exposeRuntimePorts) {
            this.exposeRuntimePorts = exposeRuntimePorts;
            return this;
        }

        /**
         * Sets the default memory size for Lambda functions in megabytes.
         *
         * @param defaultMemoryMb memory in MB (default {@value DEFAULT_MEMORY_MB})
         * @return this builder
         */
        public Builder defaultMemoryMb(int defaultMemoryMb) {
            this.defaultMemoryMb = defaultMemoryMb;
            return this;
        }

        /**
         * Sets the default timeout for Lambda function invocations in seconds.
         *
         * @param defaultTimeoutSeconds timeout in seconds (default {@value DEFAULT_TIMEOUT_SECONDS})
         * @return this builder
         */
        public Builder defaultTimeoutSeconds(int defaultTimeoutSeconds) {
            this.defaultTimeoutSeconds = defaultTimeoutSeconds;
            return this;
        }

        /**
         * Sets the Docker network that Lambda containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use default network
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Sets the port range for the Lambda Runtime API.
         *
         * @param basePort the base port (default {@value DEFAULT_RUNTIME_API_BASE_PORT})
         * @param amount   the amount of ports (default {@value DEFAULT_RUNTIME_API_PORTS_COUNT})
         * @return this builder
         */
        public Builder runtimeApiPortRange(int basePort, int amount) {
            this.runtimeApiBasePort = basePort;
            this.runtimeApiPortsCount = amount;
            return this;
        }

        /**
         * Sets the poll interval in milliseconds for Lambda container status checks.
         *
         * @param pollIntervalMs interval in milliseconds (default {@value DEFAULT_POLL_INTERVAL_MS})
         * @return this builder
         */
        public Builder pollIntervalMs(int pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
            return this;
        }

        /**
         * Sets the idle timeout in seconds after which unused Lambda containers are cleaned up.
         *
         * @param containerIdleTimeoutSeconds timeout in seconds (default {@value DEFAULT_CONTAINER_IDLE_TIMEOUT_SECONDS})
         * @return this builder
         */
        public Builder containerIdleTimeoutSeconds(int containerIdleTimeoutSeconds) {
            this.containerIdleTimeoutSeconds = containerIdleTimeoutSeconds;
            return this;
        }

        /**
         * Creates an immutable {@link LambdaConfig} from this builder.
         *
         * @return the Lambda configuration
         */
        public LambdaConfig build() {
            return new LambdaConfig(this);
        }
    }
}
