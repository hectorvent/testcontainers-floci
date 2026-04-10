package io.floci.testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

/**
 * Testcontainers module for <a href="https://github.com/floci-io/floci">Floci</a> — a
 * free, open-source local AWS emulator.
 *
 * <p>Starts a Floci container that exposes all emulated AWS services on a single HTTP
 * endpoint. Use {@link #getEndpoint()} to obtain the URL for configuring AWS SDK clients.
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

    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_ACCESS_KEY = "test";
    private static final String DEFAULT_SECRET_KEY = "test";

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
        withExposedPorts(PORT);
        withTmpFs(Map.of("/app/data", "rw"));
        withLogLevel(Level.WARN);
        waitingFor(Wait.forHttp("/_floci/health")
                .forPort(PORT)
                .withStartupTimeout(Duration.ofSeconds(30)));
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
}