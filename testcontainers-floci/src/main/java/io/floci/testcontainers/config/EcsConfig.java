package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for ECS-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * EcsConfig config = EcsConfig.builder()
 *     .enabled(true)
 *     .mock(true)
 *     .defaultMemoryMb(512)
 *     .build();
 * }</pre>
 */
public class EcsConfig extends AbstractServiceConfig {

    private static final boolean DEFAULT_MOCK = false;
    private static final int DEFAULT_MEMORY_MB = 512;
    private static final int DEFAULT_CPU_UNITS = 256;

    private final boolean mock;
    private final String dockerNetwork;
    private final int defaultMemoryMb;
    private final int defaultCpuUnits;

    private EcsConfig(Builder builder) {
        super(builder.enabled);
        this.mock = builder.mock;
        this.dockerNetwork = builder.dockerNetwork;
        this.defaultMemoryMb = builder.defaultMemoryMb;
        this.defaultCpuUnits = builder.defaultCpuUnits;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether ECS tasks go straight to RUNNING without starting real Docker containers.
     *
     * @return {@code true} if mock mode is enabled
     */
    public boolean isMock() {
        return mock;
    }

    /**
     * Returns the Docker network used for ECS task containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    /**
     * Returns the default memory size for ECS tasks in megabytes.
     *
     * @return memory in MB
     */
    public int getDefaultMemoryMb() {
        return defaultMemoryMb;
    }

    /**
     * Returns the default CPU units for ECS tasks.
     *
     * @return CPU units
     */
    public int getDefaultCpuUnits() {
        return defaultCpuUnits;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_ECS_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_ECS_MOCK", String.valueOf(mock));
            container.withEnv("FLOCI_SERVICES_ECS_DEFAULT_MEMORY_MB", String.valueOf(defaultMemoryMb));
            container.withEnv("FLOCI_SERVICES_ECS_DEFAULT_CPU_UNITS", String.valueOf(defaultCpuUnits));

            if (dockerNetwork != null) {
                container.withEnv("FLOCI_SERVICES_ECS_DOCKER_NETWORK", dockerNetwork);
            }
        }
    }

    /**
     * Builder for {@link EcsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private boolean mock = DEFAULT_MOCK;
        private String dockerNetwork;
        private int defaultMemoryMb = DEFAULT_MEMORY_MB;
        private int defaultCpuUnits = DEFAULT_CPU_UNITS;

        private Builder() {
            // Allow instantiation only via EcsConfig.builder()
        }

        /**
         * Enables or disables the ECS service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets whether ECS tasks go straight to RUNNING without starting real Docker containers.
         *
         * @param mock {@code true} to enable mock mode (default {@value DEFAULT_MOCK})
         * @return this builder
         */
        public Builder mock(boolean mock) {
            this.mock = mock;
            return this;
        }

        /**
         * Sets the Docker network that ECS task containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use the default bridge
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Sets the default memory size for ECS tasks in megabytes.
         *
         * @param defaultMemoryMb memory in MB (default {@value DEFAULT_MEMORY_MB})
         * @return this builder
         */
        public Builder defaultMemoryMb(int defaultMemoryMb) {
            this.defaultMemoryMb = defaultMemoryMb;
            return this;
        }

        /**
         * Sets the default CPU units for ECS tasks.
         *
         * @param defaultCpuUnits CPU units (default {@value DEFAULT_CPU_UNITS})
         * @return this builder
         */
        public Builder defaultCpuUnits(int defaultCpuUnits) {
            this.defaultCpuUnits = defaultCpuUnits;
            return this;
        }

        /**
         * Creates an immutable {@link EcsConfig} from this builder.
         *
         * @return the ECS configuration
         */
        public EcsConfig build() {
            return new EcsConfig(this);
        }
    }
}
