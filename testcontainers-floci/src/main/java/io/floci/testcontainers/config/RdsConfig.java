package io.floci.testcontainers.config;

/**
 * Configuration for RDS-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * RdsConfig config = RdsConfig.builder()
 *     .enabled(true)
 *     .proxyPortRange(7000, 7099)
 *     .defaultPostgresImage("postgres:16-alpine")
 *     .build();
 * }</pre>
 */
public class RdsConfig {

    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_PROXY_BASE_PORT = 7000;
    private static final int DEFAULT_PROXY_PORTS_COUNT = 10;
    private static final String DEFAULT_POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String DEFAULT_MYSQL_IMAGE = "mysql:8.0";
    private static final String DEFAULT_MARIADB_IMAGE = "mariadb:11";

    private final boolean enabled;
    private final int proxyBasePort;
    private final int proxyPortsCount;
    private final String defaultPostgresImage;
    private final String defaultMysqlImage;
    private final String defaultMariadbImage;
    private final String dockerNetwork;

    private RdsConfig(Builder builder) {
        this.enabled = builder.enabled;
        this.proxyBasePort = builder.proxyBasePort;
        this.proxyPortsCount = builder.proxyPortsCount;
        this.defaultPostgresImage = builder.defaultPostgresImage;
        this.defaultMysqlImage = builder.defaultMysqlImage;
        this.defaultMariadbImage = builder.defaultMariadbImage;
        this.dockerNetwork = builder.dockerNetwork;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether the RDS service is enabled.
     *
     * @return {@code true} if RDS is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the base port for the RDS proxy port range.
     *
     * @return the base port
     */
    public int getProxyBasePort() {
        return proxyBasePort;
    }

    /**
     * Returns the number of ports allocated for the RDS proxy, starting from {@link #getProxyBasePort()}.
     *
     * @return the number of proxy ports
     */
    public int getProxyPortsCount() {
        return proxyPortsCount;
    }

    /**
     * Returns the maximum port for the RDS proxy port range.
     *
     * @return the maximum port
     */
    public int getProxyMaxPort() {
    	return proxyBasePort + proxyPortsCount - 1;
    }

    /**
     * Returns the default Docker image used for PostgreSQL RDS instances.
     *
     * @return the PostgreSQL image name
     */
    public String getDefaultPostgresImage() {
        return defaultPostgresImage;
    }

    /**
     * Returns the default Docker image used for MySQL RDS instances.
     *
     * @return the MySQL image name
     */
    public String getDefaultMysqlImage() {
        return defaultMysqlImage;
    }

    /**
     * Returns the default Docker image used for MariaDB RDS instances.
     *
     * @return the MariaDB image name
     */
    public String getDefaultMariadbImage() {
        return defaultMariadbImage;
    }

    /**
     * Returns the Docker network used for RDS database containers, or {@code null} if not set.
     *
     * @return the Docker network name, or {@code null}
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    /**
     * Builder for {@link RdsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int proxyBasePort = DEFAULT_PROXY_BASE_PORT;
        private int proxyPortsCount = DEFAULT_PROXY_PORTS_COUNT;
        private String defaultPostgresImage = DEFAULT_POSTGRES_IMAGE;
        private String defaultMysqlImage = DEFAULT_MYSQL_IMAGE;
        private String defaultMariadbImage = DEFAULT_MARIADB_IMAGE;
        private String dockerNetwork;

        private Builder() {
            // Allow instantiation only via RdsConfig.builder()
        }

        /**
         * Enables or disables the RDS service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the port range for the RDS proxy.
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
         * Sets the default Docker image for PostgreSQL RDS instances.
         *
         * @param defaultPostgresImage the image name (default {@value DEFAULT_POSTGRES_IMAGE})
         * @return this builder
         */
        public Builder defaultPostgresImage(String defaultPostgresImage) {
            this.defaultPostgresImage = defaultPostgresImage;
            return this;
        }

        /**
         * Sets the default Docker image for MySQL RDS instances.
         *
         * @param defaultMysqlImage the image name (default {@value DEFAULT_MYSQL_IMAGE})
         * @return this builder
         */
        public Builder defaultMysqlImage(String defaultMysqlImage) {
            this.defaultMysqlImage = defaultMysqlImage;
            return this;
        }

        /**
         * Sets the default Docker image for MariaDB RDS instances.
         *
         * @param defaultMariadbImage the image name (default {@value DEFAULT_MARIADB_IMAGE})
         * @return this builder
         */
        public Builder defaultMariadbImage(String defaultMariadbImage) {
            this.defaultMariadbImage = defaultMariadbImage;
            return this;
        }

        /**
         * Sets the Docker network that RDS database containers should join.
         *
         * @param dockerNetwork the network name, or {@code null} to use the default bridge
         * @return this builder
         */
        public Builder dockerNetwork(String dockerNetwork) {
            this.dockerNetwork = dockerNetwork;
            return this;
        }

        /**
         * Creates an immutable {@link RdsConfig} from this builder.
         *
         * @return the RDS configuration
         */
        public RdsConfig build() {
            return new RdsConfig(this);
        }
    }
}
