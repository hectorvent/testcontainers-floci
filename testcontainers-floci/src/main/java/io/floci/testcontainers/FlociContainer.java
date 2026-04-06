package io.floci.testcontainers;

import io.floci.testcontainers.config.LambdaConfig;
import io.floci.testcontainers.config.RdsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Testcontainers module for <a href="https://github.com/floci-io/floci">Floci</a> — a
 * free, open-source local AWS emulator.
 *
 * <p>Starts a Floci container that exposes all emulated AWS services on a single HTTP
 * endpoint. Use {@link #getEndpoint()} to obtain the URL for configuring AWS SDK clients.
 *
 * <p>Container-based services (RDS, ElastiCache, Lambda, ECS) require access to the Docker
 * daemon. This module automatically mounts the Docker socket and runs as root to enable
 * these services. Sibling containers created by Floci (e.g. PostgreSQL for RDS) are
 * accessible on the Docker host via their mapped ports.
 *
 * <pre>{@code
 * try (FlociContainer floci = new FlociContainer()) {
 *     floci.start();
 *     String endpoint = floci.getEndpoint();
 *     // configure your AWS SDK client with the endpoint
 * }
 * }</pre>
 */
public class FlociContainer extends GenericContainer<FlociContainer> {

    private static final Logger logger = LoggerFactory.getLogger(FlociContainer.class);

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("hectorvent/floci");
    private static final String DEFAULT_TAG = "latest";

    /**
     * Default port used to startup Floci container
     */
    public static final int PORT = 4566;

    private static final String DOCKER_SOCKET_PATH = "/var/run/docker.sock";
    private static final String ROOT_USER = "root";

    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_ACCESS_KEY = "test";
    private static final String DEFAULT_SECRET_KEY = "test";

    private LambdaConfig lambdaConfig = LambdaConfig.builder().build();
    private RdsConfig rdsConfig = RdsConfig.builder().build();

    /**
     * Creates a new Floci container with the default image ({@code hectorvent/floci:latest}).
     */
    public FlociContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    /**
     * Creates a new Floci container with the specified image name.
     *
     * @param dockerImageName the Docker image name (must be compatible with {@code hectorvent/floci})
     */
    public FlociContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Creates a new Floci container with the specified Docker image name.
     *
     * @param dockerImageName the Docker image name
     */
    public FlociContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        withTmpFs(Map.of("/app/data", "rw"));
        withFileSystemBind(DockerClientFactory.instance().getRemoteDockerUnixSocketPath(), DOCKER_SOCKET_PATH);
        withCreateContainerCmdModifier(cmd -> cmd.withUser(ROOT_USER)); // Allow binding docker socket
        withLogLevel(Level.WARN);
        waitingFor(Wait.forHttp("/_floci/health")
                .forPort(PORT)
                .withStartupTimeout(Duration.ofSeconds(30)));

        configureExposedPorts();
        configureLambda();
        configureRds();

        // Bugfix to make it work on podman - fixed by PR https://github.com/floci-io/floci/pull/343
        withCopyToContainer(Transferable.of(""), "/.dockerenv");
    }

    /**
     * Returns the endpoint URL for connecting to Floci (e.g. {@code http://localhost:32781}).
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
        return String.format("http://%s:%d", getHost(), getMappedPort(PORT));
    }

    /**
     * Returns the configured AWS region. Defaults to {@code us-east-1}.
     *
     * @return the AWS region
     */
    public String getRegion() {
        return getEnvMap().getOrDefault("FLOCI_DEFAULT_REGION", DEFAULT_REGION);
    }

    /**
     * Returns the AWS access key to use with this instance. Defaults to {@code test}.
     *
     * @return the access key
     */
    public String getAccessKey() {
        return DEFAULT_ACCESS_KEY;
    }

    /**
     * Returns the AWS secret key to use with this instance. Defaults to {@code test}.
     *
     * @return the secret key
     */
    public String getSecretKey() {
        return DEFAULT_SECRET_KEY;
    }

    /**
     * Sets the AWS region for this Floci instance.
     *
     * @param region the AWS region (e.g. {@code eu-west-1})
     * @return this container instance
     */
    public FlociContainer withRegion(String region) {
        return withEnv("FLOCI_DEFAULT_REGION", region);
    }

    /**
     * Sets the log level for this Floci instance. Defaults to {@link Level#WARN}.
     *
     * @param logLevel the log level
     * @return this container instance
     */
    public FlociContainer withLogLevel(Level logLevel) {
        return withEnv("QUARKUS_LOG_CATEGORY__IO_GITHUB_HECTORVENT__LEVEL", logLevel.toString());
    }

    /**
     * Returns the log level configured for this Floci instance. Defaults to {@link Level#WARN}.
     *
     * @return the log level
     */
    public Level getLogLevel() {
        String logLevelStr = getEnvMap().getOrDefault("QUARKUS_LOG_CATEGORY__IO_GITHUB_HECTORVENT__LEVEL", "WARN");
        try {
            return Level.valueOf(logLevelStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid log level '{}' in environment variable, defaulting to WARN", logLevelStr);
            return Level.WARN;
        }
    }

    /**
     * Configures a dedicated Docker network for this container that will be used by Floci itself and by all
     * services, that spin up additional containers like RDS, Lambda or ElasticCache.
     */
    public FlociContainer withDedicatedNetwork() {
        String networkName = "floci-network-" + uniqueShortId();
        Network network = Network.builder()
                .createNetworkCmdModifier(cmd -> cmd.withName(networkName))
                .build();
        withNetwork(network);
        return withEnv("FLOCI_SERVICES_DOCKER_NETWORK", networkName);
    }

    /**
     * Returns the name of the dedicated Docker network configured for this container, or {@code null} if no dedicated network is configured.
     *
     * @return the name of the dedicated Docker network, or {@code null} if not configured
     */
    public String getDedicatedNetworkName() {
        return getEnvMap().get("FLOCI_SERVICES_DOCKER_NETWORK");
    }

    /**
     * Lambda-specific settings such as the Runtime API port range
     *
     * @return the Lambda configuration
     */
    public LambdaConfig getLambdaConfig() {
        return lambdaConfig;
    }

    /**
     * Configures Lambda-specific settings such as the Runtime API port range.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withLambdaConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link LambdaConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withLambdaConfig(Consumer<LambdaConfig.Builder> configurer) {
        LambdaConfig.Builder builder = LambdaConfig.builder();
        configurer.accept(builder);
        this.lambdaConfig = builder.build();
        configureExposedPorts();
        configureLambda();
        return this;
    }

    /**
     * RDS-specific settings such as proxy ports and default database images.
     *
     * @return the RDS configuration
     */
    public RdsConfig getRdsConfig() {
        return rdsConfig;
    }

    /**
     * Configures RDS-specific settings such as proxy ports and default database images.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withRdsConfig(c -> c
     *         .proxyPortRange(7000, 7099)
     *         .defaultPostgresImage("postgres:16-alpine"));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link RdsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withRdsConfig(Consumer<RdsConfig.Builder> configurer) {
        RdsConfig.Builder builder = RdsConfig.builder();
        configurer.accept(builder);
        this.rdsConfig = builder.build();
        configureExposedPorts();
        configureRds();
        return this;
    }

    /**
     * Configures all exposed ports of the Floci container
     */
    private void configureExposedPorts() {
        withExposedPorts(PORT);

        if (lambdaConfig.isEnabled() && lambdaConfig.isExposeRuntimePorts()) {
            // Expose ports of Lambda runtimes to make them accessible by the user
            for (int port = lambdaConfig.getRuntimeApiBasePort(); port <= lambdaConfig.getRuntimeApiMaxPort(); port++) {
                addExposedPorts(port);
            }
        }

        if (rdsConfig.isEnabled()) {
            // Expose ports of RDS to make them accessible by the user
            for (int port = rdsConfig.getProxyBasePort(); port <= rdsConfig.getProxyMaxPort(); port++) {
                addExposedPorts(port);
            }
        }
    }

    /**
     * Applies Lambda Runtime API configuration
     */
    private void configureLambda() {
        withEnv("FLOCI_SERVICES_LAMBDA_ENABLED", String.valueOf(lambdaConfig.isEnabled()));

        if (lambdaConfig.isEnabled()) {
            withEnv("FLOCI_SERVICES_LAMBDA_EPHEMERAL", String.valueOf(lambdaConfig.isEphemeral()));
            withEnv("FLOCI_SERVICES_LAMBDA_DEFAULT_MEMORY_MB", String.valueOf(lambdaConfig.getDefaultMemoryMb()));
            withEnv("FLOCI_SERVICES_LAMBDA_DEFAULT_TIMEOUT_SECONDS", String.valueOf(lambdaConfig.getDefaultTimeoutSeconds()));
            withEnv("FLOCI_SERVICES_LAMBDA_RUNTIME_API_BASE_PORT", String.valueOf(lambdaConfig.getRuntimeApiBasePort()));
            withEnv("FLOCI_SERVICES_LAMBDA_RUNTIME_API_MAX_PORT", String.valueOf(lambdaConfig.getRuntimeApiMaxPort()));
            withEnv("FLOCI_SERVICES_LAMBDA_POLL_INTERVAL_MS", String.valueOf(lambdaConfig.getPollIntervalMs()));
            withEnv("FLOCI_SERVICES_LAMBDA_CONTAINER_IDLE_TIMEOUT_SECONDS", String.valueOf(lambdaConfig.getContainerIdleTimeoutSeconds()));

            if (lambdaConfig.getDockerNetwork() != null) {
                withEnv("FLOCI_SERVICES_LAMBDA_DOCKER_NETWORK", lambdaConfig.getDockerNetwork());
            }
        }
    }

    /**
     * Applies RDS configuration
     */
    private void configureRds() {
        withEnv("FLOCI_SERVICES_RDS_ENABLED", String.valueOf(rdsConfig.isEnabled()));

        if (rdsConfig.isEnabled()) {
            withEnv("FLOCI_SERVICES_RDS_PROXY_BASE_PORT", String.valueOf(rdsConfig.getProxyBasePort()));
            withEnv("FLOCI_SERVICES_RDS_PROXY_MAX_PORT", String.valueOf(rdsConfig.getProxyMaxPort()));
            withEnv("FLOCI_SERVICES_RDS_DEFAULT_POSTGRES_IMAGE", rdsConfig.getDefaultPostgresImage());
            withEnv("FLOCI_SERVICES_RDS_DEFAULT_MYSQL_IMAGE", rdsConfig.getDefaultMysqlImage());
            withEnv("FLOCI_SERVICES_RDS_DEFAULT_MARIADB_IMAGE", rdsConfig.getDefaultMariadbImage());

            if (rdsConfig.getDockerNetwork() != null) {
                withEnv("FLOCI_SERVICES_RDS_DOCKER_NETWORK", rdsConfig.getDockerNetwork());
            }
        }
    }

    private static String uniqueShortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}