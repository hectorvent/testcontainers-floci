package io.floci.testcontainers;

import io.floci.testcontainers.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("floci/floci");
    private static final String DEFAULT_TAG = "latest";

    /**
     * Default port used to startup Floci container
     */
    public static final int PORT = 4566;

    private static final String DOCKER_SOCKET_PATH = "/var/run/docker.sock";
    private static final String ROOT_USER = "root";

    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_AVAILABILITY_ZONE = "us-east-1a";
    private static final String DEFAULT_ACCOUNT_ID = "000000000000";
    private static final String DEFAULT_ACCESS_KEY = "test";
    private static final String DEFAULT_SECRET_KEY = "test";

    private final Path hostPersistentPath;

    private AcmConfig acmConfig = AcmConfig.builder().build();
    private ApiGatewayConfig apiGatewayConfig = ApiGatewayConfig.builder().build();
    private ApiGatewayV2Config apiGatewayV2Config = ApiGatewayV2Config.builder().build();
    private AppConfigConfig appConfigConfig = AppConfigConfig.builder().build();
    private AppConfigDataConfig appConfigDataConfig = AppConfigDataConfig.builder().build();
    private CloudFormationConfig cloudFormationConfig = CloudFormationConfig.builder().build();
    private CloudWatchLogsConfig cloudWatchLogsConfig = CloudWatchLogsConfig.builder().build();
    private CloudWatchMetricsConfig cloudWatchMetricsConfig = CloudWatchMetricsConfig.builder().build();
    private CognitoConfig cognitoConfig = CognitoConfig.builder().build();
    private DynamoDbConfig dynamoDbConfig = DynamoDbConfig.builder().build();
    private Ec2Config ec2Config = Ec2Config.builder().build();
    private EcrConfig ecrConfig = EcrConfig.builder().build();
    private EcsConfig ecsConfig = EcsConfig.builder().build();
    private ElastiCacheConfig elastiCacheConfig = ElastiCacheConfig.builder().build();
    private EventBridgeConfig eventBridgeConfig = EventBridgeConfig.builder().build();
    private IamConfig iamConfig = IamConfig.builder().build();
    private KinesisConfig kinesisConfig = KinesisConfig.builder().build();
    private KmsConfig kmsConfig = KmsConfig.builder().build();
    private LambdaConfig lambdaConfig = LambdaConfig.builder().build();
    private OpenSearchConfig openSearchConfig = OpenSearchConfig.builder().build();
    private RdsConfig rdsConfig = RdsConfig.builder().build();
    private S3Config s3Config = S3Config.builder().build();
    private SchedulerConfig schedulerConfig = SchedulerConfig.builder().build();
    private SecretsManagerConfig secretsManagerConfig = SecretsManagerConfig.builder().build();
    private SesConfig sesConfig = SesConfig.builder().build();
    private SnsConfig snsConfig = SnsConfig.builder().build();
    private SqsConfig sqsConfig = SqsConfig.builder().build();
    private SsmConfig ssmConfig = SsmConfig.builder().build();
    private StepFunctionsConfig stepFunctionsConfig = StepFunctionsConfig.builder().build();
    private MskConfig mskConfig = MskConfig.builder().build();
    private FirehoseConfig firehoseConfig = FirehoseConfig.builder().build();
    private AthenaConfig athenaConfig = AthenaConfig.builder().build();
    private GlueConfig glueConfig = GlueConfig.builder().build();
    private ResourceGroupsTaggingConfig resourceGroupsTaggingConfig = ResourceGroupsTaggingConfig.builder().build();
    private BedrockRuntimeConfig bedrockRuntimeConfig = BedrockRuntimeConfig.builder().build();
    private PipesConfig pipesConfig = PipesConfig.builder().build();
    private EksConfig eksConfig = EksConfig.builder().build();
    private CodeBuildConfig codeBuildConfig = CodeBuildConfig.builder().build();
    private CodeDeployConfig codeDeployConfig = CodeDeployConfig.builder().build();
    private ElbV2Config elbV2Config = ElbV2Config.builder().build();

    /**
     * Creates a new Floci container with the default image ({@code floci/floci:latest}).
     */
    public FlociContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    /**
     * Creates a new Floci container with the specified image name.
     *
     * @param dockerImageName the Docker image name (must be compatible with {@code floci/floci})
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

        // Store all persistent data from the containers in a temporary directory on the host, which is
        // automatically cleaned up after the test run.
        try {
            this.hostPersistentPath = Files.createTempDirectory("floci-").toAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temporary data directory", e);
        }
        withFileSystemBind(hostPersistentPath.toString(), "/app/data", BindMode.READ_WRITE);
        withEnv("FLOCI_STORAGE_HOST_PERSISTENT_PATH", hostPersistentPath.toString());

        // Allow creation of child container instances (e.g. for ECS or RDS service)
        withFileSystemBind(DockerClientFactory.instance().getRemoteDockerUnixSocketPath(), DOCKER_SOCKET_PATH);
        withCreateContainerCmdModifier(cmd -> cmd.withUser(ROOT_USER)); // Allow binding docker socket

        // Configure observability and healthcheck
        withLogLevel(Level.WARN);
        waitingFor(Wait.forHttp("/_floci/health")
                .forPort(PORT)
                .withStartupTimeout(Duration.ofSeconds(30)));

        // Configure services
        configureExposedPorts();
        configureEnvVars();
    }

    @Override
    public void stop() {
        super.stop();

        // Cleanup persistent storage
        try (Stream<Path> paths = Files.walk(hostPersistentPath)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            // Ignore exception silently
        }
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
     * Returns the configured AWS region. Defaults to {@value DEFAULT_REGION}.
     *
     * @return the AWS region
     */
    public String getRegion() {
        return getEnvMap().getOrDefault("FLOCI_DEFAULT_REGION", DEFAULT_REGION);
    }

    /**
     * Returns the AWS access key to use with this instance. Defaults to {@value DEFAULT_ACCESS_KEY}.
     *
     * @return the access key
     */
    public String getAccessKey() {
        return DEFAULT_ACCESS_KEY;
    }

    /**
     * Returns the AWS secret key to use with this instance. Defaults to {@value DEFAULT_SECRET_KEY}.
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
     * Returns the configured default availability zone. Defaults to {@value DEFAULT_AVAILABILITY_ZONE}.
     *
     * @return the availability zone
     */
    public String getDefaultAvailabilityZone() {
        return getEnvMap().getOrDefault("FLOCI_DEFAULT_AVAILABILITY_ZONE", DEFAULT_AVAILABILITY_ZONE);
    }

    /**
     * Sets the default availability zone for this Floci instance.
     *
     * @param availabilityZone the availability zone (e.g. {@code eu-west-1a})
     * @return this container instance
     */
    public FlociContainer withDefaultAvailabilityZone(String availabilityZone) {
        return withEnv("FLOCI_DEFAULT_AVAILABILITY_ZONE", availabilityZone);
    }

    /**
     * Returns the configured default AWS account ID. Defaults to {@value DEFAULT_ACCOUNT_ID}.
     *
     * @return the account ID
     */
    public String getDefaultAccountId() {
        return getEnvMap().getOrDefault("FLOCI_DEFAULT_ACCOUNT_ID", DEFAULT_ACCOUNT_ID);
    }

    /**
     * Sets the default AWS account ID for this Floci instance.
     *
     * @param accountId the AWS account ID (e.g. {@code 123456789012})
     * @return this container instance
     */
    public FlociContainer withDefaultAccountId(String accountId) {
        return withEnv("FLOCI_DEFAULT_ACCOUNT_ID", accountId);
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
     * ACM-specific settings such as certificate validation timing
     *
     * @return the ACM configuration
     */
    public AcmConfig getAcmConfig() {
        return acmConfig;
    }

    /**
     * Configures acm-specific settings such as certificate validation timing.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withAcmConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link AcmConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withAcmConfig(Consumer<AcmConfig.Builder> configurer) {
        AcmConfig.Builder builder = AcmConfig.builder();
        configurer.accept(builder);
        this.acmConfig = builder.build();
        acmConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * API Gateway-specific settings
     *
     * @return the API Gateway configuration
     */
    public ApiGatewayConfig getApiGatewayConfig() {
        return apiGatewayConfig;
    }

    /**
     * Configures api gateway-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withApiGatewayConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link ApiGatewayConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withApiGatewayConfig(Consumer<ApiGatewayConfig.Builder> configurer) {
        ApiGatewayConfig.Builder builder = ApiGatewayConfig.builder();
        configurer.accept(builder);
        this.apiGatewayConfig = builder.build();
        apiGatewayConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * API Gateway V2-specific settings
     *
     * @return the API Gateway V2 configuration
     */
    public ApiGatewayV2Config getApiGatewayV2Config() {
        return apiGatewayV2Config;
    }

    /**
     * Configures api gateway v2-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withApiGatewayV2Config(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link ApiGatewayV2Config.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withApiGatewayV2Config(Consumer<ApiGatewayV2Config.Builder> configurer) {
        ApiGatewayV2Config.Builder builder = ApiGatewayV2Config.builder();
        configurer.accept(builder);
        this.apiGatewayV2Config = builder.build();
        apiGatewayV2Config.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * AppConfig-specific settings
     *
     * @return the AppConfig configuration
     */
    public AppConfigConfig getAppConfigConfig() {
        return appConfigConfig;
    }

    /**
     * Configures appconfig-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withAppConfigConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link AppConfigConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withAppConfigConfig(Consumer<AppConfigConfig.Builder> configurer) {
        AppConfigConfig.Builder builder = AppConfigConfig.builder();
        configurer.accept(builder);
        this.appConfigConfig = builder.build();
        appConfigConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * AppConfig Data-specific settings
     *
     * @return the AppConfig Data configuration
     */
    public AppConfigDataConfig getAppConfigDataConfig() {
        return appConfigDataConfig;
    }

    /**
     * Configures appconfig data-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withAppConfigDataConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link AppConfigDataConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withAppConfigDataConfig(Consumer<AppConfigDataConfig.Builder> configurer) {
        AppConfigDataConfig.Builder builder = AppConfigDataConfig.builder();
        configurer.accept(builder);
        this.appConfigDataConfig = builder.build();
        appConfigDataConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * CloudFormation-specific settings
     *
     * @return the CloudFormation configuration
     */
    public CloudFormationConfig getCloudFormationConfig() {
        return cloudFormationConfig;
    }

    /**
     * Configures cloudformation-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withCloudFormationConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link CloudFormationConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withCloudFormationConfig(Consumer<CloudFormationConfig.Builder> configurer) {
        CloudFormationConfig.Builder builder = CloudFormationConfig.builder();
        configurer.accept(builder);
        this.cloudFormationConfig = builder.build();
        cloudFormationConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * CloudWatch Logs-specific settings such as query event limits
     *
     * @return the CloudWatch Logs configuration
     */
    public CloudWatchLogsConfig getCloudWatchLogsConfig() {
        return cloudWatchLogsConfig;
    }

    /**
     * Configures cloudwatch logs-specific settings such as query event limits.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withCloudWatchLogsConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link CloudWatchLogsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withCloudWatchLogsConfig(Consumer<CloudWatchLogsConfig.Builder> configurer) {
        CloudWatchLogsConfig.Builder builder = CloudWatchLogsConfig.builder();
        configurer.accept(builder);
        this.cloudWatchLogsConfig = builder.build();
        cloudWatchLogsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * CloudWatch Metrics-specific settings
     *
     * @return the CloudWatch Metrics configuration
     */
    public CloudWatchMetricsConfig getCloudWatchMetricsConfig() {
        return cloudWatchMetricsConfig;
    }

    /**
     * Configures cloudwatch metrics-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withCloudWatchMetricsConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link CloudWatchMetricsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withCloudWatchMetricsConfig(Consumer<CloudWatchMetricsConfig.Builder> configurer) {
        CloudWatchMetricsConfig.Builder builder = CloudWatchMetricsConfig.builder();
        configurer.accept(builder);
        this.cloudWatchMetricsConfig = builder.build();
        cloudWatchMetricsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Cognito-specific settings
     *
     * @return the Cognito configuration
     */
    public CognitoConfig getCognitoConfig() {
        return cognitoConfig;
    }

    /**
     * Configures cognito-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withCognitoConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link CognitoConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withCognitoConfig(Consumer<CognitoConfig.Builder> configurer) {
        CognitoConfig.Builder builder = CognitoConfig.builder();
        configurer.accept(builder);
        this.cognitoConfig = builder.build();
        cognitoConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * DynamoDB-specific settings
     *
     * @return the DynamoDB configuration
     */
    public DynamoDbConfig getDynamoDbConfig() {
        return dynamoDbConfig;
    }

    /**
     * Configures dynamodb-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withDynamoDbConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link DynamoDbConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withDynamoDbConfig(Consumer<DynamoDbConfig.Builder> configurer) {
        DynamoDbConfig.Builder builder = DynamoDbConfig.builder();
        configurer.accept(builder);
        this.dynamoDbConfig = builder.build();
        dynamoDbConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * EC2-specific settings
     *
     * @return the EC2 configuration
     */
    public Ec2Config getEc2Config() {
        return ec2Config;
    }

    /**
     * Configures ec2-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withEc2Config(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link Ec2Config.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withEc2Config(Consumer<Ec2Config.Builder> configurer) {
        Ec2Config.Builder builder = Ec2Config.builder();
        configurer.accept(builder);
        this.ec2Config = builder.build();
        ec2Config.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * ECR-specific settings such as registry ports and TLS configuration
     *
     * @return the ECR configuration
     */
    public EcrConfig getEcrConfig() {
        return ecrConfig;
    }

    /**
     * Configures ECR-specific settings such as registry ports and TLS configuration.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withEcrConfig(c -> c
     *         .registryPortRange(5000, 100)
     *         .registryImage("registry:2"));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link EcrConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withEcrConfig(Consumer<EcrConfig.Builder> configurer) {
        EcrConfig.Builder builder = EcrConfig.builder();
        configurer.accept(builder);
        this.ecrConfig = builder.build();
        configureExposedPorts();
        ecrConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * ECS-specific settings such as mock mode and default task resources
     *
     * @return the ECS configuration
     */
    public EcsConfig getEcsConfig() {
        return ecsConfig;
    }

    /**
     * Configures ECS-specific settings such as mock mode and default task resources.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withEcsConfig(c -> c
     *         .mock(true)
     *         .defaultMemoryMb(1024));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link EcsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withEcsConfig(Consumer<EcsConfig.Builder> configurer) {
        EcsConfig.Builder builder = EcsConfig.builder();
        configurer.accept(builder);
        this.ecsConfig = builder.build();
        ecsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * ElastiCache-specific settings such as proxy ports and default image
     *
     * @return the ElastiCache configuration
     */
    public ElastiCacheConfig getElastiCacheConfig() {
        return elastiCacheConfig;
    }

    /**
     * Configures ElastiCache-specific settings such as proxy ports and default image.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withElastiCacheConfig(c -> c
     *         .proxyPortRange(6379, 21)
     *         .defaultImage("valkey/valkey:8"));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link ElastiCacheConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withElastiCacheConfig(Consumer<ElastiCacheConfig.Builder> configurer) {
        ElastiCacheConfig.Builder builder = ElastiCacheConfig.builder();
        configurer.accept(builder);
        this.elastiCacheConfig = builder.build();
        configureExposedPorts();
        elastiCacheConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * EventBridge-specific settings
     *
     * @return the EventBridge configuration
     */
    public EventBridgeConfig getEventBridgeConfig() {
        return eventBridgeConfig;
    }

    /**
     * Configures eventbridge-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withEventBridgeConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link EventBridgeConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withEventBridgeConfig(Consumer<EventBridgeConfig.Builder> configurer) {
        EventBridgeConfig.Builder builder = EventBridgeConfig.builder();
        configurer.accept(builder);
        this.eventBridgeConfig = builder.build();
        eventBridgeConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * IAM-specific settings
     *
     * @return the IAM configuration
     */
    public IamConfig getIamConfig() {
        return iamConfig;
    }

    /**
     * Configures iam-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withIamConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link IamConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withIamConfig(Consumer<IamConfig.Builder> configurer) {
        IamConfig.Builder builder = IamConfig.builder();
        configurer.accept(builder);
        this.iamConfig = builder.build();
        iamConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Kinesis-specific settings
     *
     * @return the Kinesis configuration
     */
    public KinesisConfig getKinesisConfig() {
        return kinesisConfig;
    }

    /**
     * Configures kinesis-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withKinesisConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link KinesisConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withKinesisConfig(Consumer<KinesisConfig.Builder> configurer) {
        KinesisConfig.Builder builder = KinesisConfig.builder();
        configurer.accept(builder);
        this.kinesisConfig = builder.build();
        kinesisConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * KMS-specific settings
     *
     * @return the KMS configuration
     */
    public KmsConfig getKmsConfig() {
        return kmsConfig;
    }

    /**
     * Configures kms-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withKmsConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link KmsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withKmsConfig(Consumer<KmsConfig.Builder> configurer) {
        KmsConfig.Builder builder = KmsConfig.builder();
        configurer.accept(builder);
        this.kmsConfig = builder.build();
        kmsConfig.applyEnvVarsToContainer(this);
        return this;
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
        lambdaConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * OpenSearch-specific settings such as mode, proxy ports and default image
     *
     * @return the OpenSearch configuration
     */
    public OpenSearchConfig getOpenSearchConfig() {
        return openSearchConfig;
    }

    /**
     * Configures OpenSearch-specific settings such as mode, proxy ports and default image.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withOpenSearchConfig(c -> c
     *         .mock(true)
     *         .proxyPortRange(9400, 100));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link OpenSearchConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withOpenSearchConfig(Consumer<OpenSearchConfig.Builder> configurer) {
        OpenSearchConfig.Builder builder = OpenSearchConfig.builder();
        configurer.accept(builder);
        this.openSearchConfig = builder.build();
        configureExposedPorts();
        openSearchConfig.applyEnvVarsToContainer(this);
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
        rdsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * S3-specific settings such as presigned URL expiry
     *
     * @return the S3 configuration
     */
    public S3Config getS3Config() {
        return s3Config;
    }

    /**
     * Configures s3-specific settings such as presigned url expiry.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withS3Config(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link S3Config.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withS3Config(Consumer<S3Config.Builder> configurer) {
        S3Config.Builder builder = S3Config.builder();
        configurer.accept(builder);
        this.s3Config = builder.build();
        s3Config.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Scheduler-specific settings
     *
     * @return the Scheduler configuration
     */
    public SchedulerConfig getSchedulerConfig() {
        return schedulerConfig;
    }

    /**
     * Configures scheduler-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withSchedulerConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link SchedulerConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withSchedulerConfig(Consumer<SchedulerConfig.Builder> configurer) {
        SchedulerConfig.Builder builder = SchedulerConfig.builder();
        configurer.accept(builder);
        this.schedulerConfig = builder.build();
        schedulerConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Secrets Manager-specific settings such as recovery window
     *
     * @return the Secrets Manager configuration
     */
    public SecretsManagerConfig getSecretsManagerConfig() {
        return secretsManagerConfig;
    }

    /**
     * Configures secrets manager-specific settings such as recovery window.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withSecretsManagerConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link SecretsManagerConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withSecretsManagerConfig(Consumer<SecretsManagerConfig.Builder> configurer) {
        SecretsManagerConfig.Builder builder = SecretsManagerConfig.builder();
        configurer.accept(builder);
        this.secretsManagerConfig = builder.build();
        secretsManagerConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * SES-specific settings
     *
     * @return the SES configuration
     */
    public SesConfig getSesConfig() {
        return sesConfig;
    }

    /**
     * Configures ses-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withSesConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link SesConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withSesConfig(Consumer<SesConfig.Builder> configurer) {
        SesConfig.Builder builder = SesConfig.builder();
        configurer.accept(builder);
        this.sesConfig = builder.build();
        sesConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * SNS-specific settings
     *
     * @return the SNS configuration
     */
    public SnsConfig getSnsConfig() {
        return snsConfig;
    }

    /**
     * Configures sns-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withSnsConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link SnsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withSnsConfig(Consumer<SnsConfig.Builder> configurer) {
        SnsConfig.Builder builder = SnsConfig.builder();
        configurer.accept(builder);
        this.snsConfig = builder.build();
        snsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * SQS-specific settings such as visibility timeout and message size limits
     *
     * @return the SQS configuration
     */
    public SqsConfig getSqsConfig() {
        return sqsConfig;
    }

    /**
     * Configures sqs-specific settings such as visibility timeout and message size limits.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withSqsConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link SqsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withSqsConfig(Consumer<SqsConfig.Builder> configurer) {
        SqsConfig.Builder builder = SqsConfig.builder();
        configurer.accept(builder);
        this.sqsConfig = builder.build();
        sqsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * SSM-specific settings such as parameter history limits
     *
     * @return the SSM configuration
     */
    public SsmConfig getSsmConfig() {
        return ssmConfig;
    }

    /**
     * Configures ssm-specific settings such as parameter history limits.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withSsmConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link SsmConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withSsmConfig(Consumer<SsmConfig.Builder> configurer) {
        SsmConfig.Builder builder = SsmConfig.builder();
        configurer.accept(builder);
        this.ssmConfig = builder.build();
        ssmConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Step Functions-specific settings
     *
     * @return the Step Functions configuration
     */
    public StepFunctionsConfig getStepFunctionsConfig() {
        return stepFunctionsConfig;
    }

    /**
     * Configures step functions-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withStepFunctionsConfig(c -> c...);
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link StepFunctionsConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withStepFunctionsConfig(Consumer<StepFunctionsConfig.Builder> configurer) {
        StepFunctionsConfig.Builder builder = StepFunctionsConfig.builder();
        configurer.accept(builder);
        this.stepFunctionsConfig = builder.build();
        stepFunctionsConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * MSK-specific settings such as mock mode and default image.
     *
     * @return the MSK configuration
     */
    public MskConfig getMskConfig() {
        return mskConfig;
    }

    /**
     * Configures MSK-specific settings such as mock mode and default image.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withMskConfig(c -> c
     *         .mock(true)
     *         .defaultImage("redpandadata/redpanda:v24"));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link MskConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withMskConfig(Consumer<MskConfig.Builder> configurer) {
        MskConfig.Builder builder = MskConfig.builder();
        configurer.accept(builder);
        this.mskConfig = builder.build();
        mskConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Firehose-specific settings.
     *
     * @return the Firehose configuration
     */
    public FirehoseConfig getFirehoseConfig() {
        return firehoseConfig;
    }

    /**
     * Configures firehose-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withFirehoseConfig(c -> c.enabled(false));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link FirehoseConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withFirehoseConfig(Consumer<FirehoseConfig.Builder> configurer) {
        FirehoseConfig.Builder builder = FirehoseConfig.builder();
        configurer.accept(builder);
        this.firehoseConfig = builder.build();
        firehoseConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Athena-specific settings.
     *
     * @return the Athena configuration
     */
    public AthenaConfig getAthenaConfig() {
        return athenaConfig;
    }

    /**
     * Configures athena-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withAthenaConfig(c -> c.enabled(false));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link AthenaConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withAthenaConfig(Consumer<AthenaConfig.Builder> configurer) {
        AthenaConfig.Builder builder = AthenaConfig.builder();
        configurer.accept(builder);
        this.athenaConfig = builder.build();
        athenaConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Glue-specific settings.
     *
     * @return the Glue configuration
     */
    public GlueConfig getGlueConfig() {
        return glueConfig;
    }

    /**
     * Configures glue-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withGlueConfig(c -> c.enabled(false));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link GlueConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withGlueConfig(Consumer<GlueConfig.Builder> configurer) {
        GlueConfig.Builder builder = GlueConfig.builder();
        configurer.accept(builder);
        this.glueConfig = builder.build();
        glueConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Resource Groups Tagging-specific settings.
     *
     * @return the Resource Groups Tagging configuration
     */
    public ResourceGroupsTaggingConfig getResourceGroupsTaggingConfig() {
        return resourceGroupsTaggingConfig;
    }

    /**
     * Configures resource groups tagging-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withResourceGroupsTaggingConfig(c -> c.enabled(false));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link ResourceGroupsTaggingConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withResourceGroupsTaggingConfig(Consumer<ResourceGroupsTaggingConfig.Builder> configurer) {
        ResourceGroupsTaggingConfig.Builder builder = ResourceGroupsTaggingConfig.builder();
        configurer.accept(builder);
        this.resourceGroupsTaggingConfig = builder.build();
        resourceGroupsTaggingConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Bedrock Runtime-specific settings.
     *
     * @return the Bedrock Runtime configuration
     */
    public BedrockRuntimeConfig getBedrockRuntimeConfig() {
        return bedrockRuntimeConfig;
    }

    /**
     * Configures bedrock runtime-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withBedrockRuntimeConfig(c -> c.enabled(false));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link BedrockRuntimeConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withBedrockRuntimeConfig(Consumer<BedrockRuntimeConfig.Builder> configurer) {
        BedrockRuntimeConfig.Builder builder = BedrockRuntimeConfig.builder();
        configurer.accept(builder);
        this.bedrockRuntimeConfig = builder.build();
        bedrockRuntimeConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Pipes-specific settings.
     *
     * @return the Pipes configuration
     */
    public PipesConfig getPipesConfig() {
        return pipesConfig;
    }

    /**
     * Configures pipes-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withPipesConfig(c -> c.enabled(false));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link PipesConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withPipesConfig(Consumer<PipesConfig.Builder> configurer) {
        PipesConfig.Builder builder = PipesConfig.builder();
        configurer.accept(builder);
        this.pipesConfig = builder.build();
        pipesConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * EKS-specific settings such as mock mode, provider, API server ports and default image.
     *
     * @return the EKS configuration
     */
    public EksConfig getEksConfig() {
        return eksConfig;
    }

    /**
     * Configures EKS-specific settings such as mock mode, provider, API server ports and default image.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withEksConfig(c -> c
     *         .mock(true)
     *         .apiServerPortRange(6500, 100));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link EksConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withEksConfig(Consumer<EksConfig.Builder> configurer) {
        EksConfig.Builder builder = EksConfig.builder();
        configurer.accept(builder);
        this.eksConfig = builder.build();
        configureExposedPorts();
        eksConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * CodeBuild-specific settings
     *
     * @return the CodeBuild configuration
     */
    public CodeBuildConfig getCodeBuildConfig() {
        return codeBuildConfig;
    }

    /**
     * Configures CodeBuild-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withCodeBuildConfig(c -> c.enabled(true));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link CodeBuildConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withCodeBuildConfig(Consumer<CodeBuildConfig.Builder> configurer) {
        CodeBuildConfig.Builder builder = CodeBuildConfig.builder();
        configurer.accept(builder);
        this.codeBuildConfig = builder.build();
        codeBuildConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * CodeDeploy-specific settings
     *
     * @return the CodeDeploy configuration
     */
    public CodeDeployConfig getCodeDeployConfig() {
        return codeDeployConfig;
    }

    /**
     * Configures CodeDeploy-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withCodeDeployConfig(c -> c.enabled(true));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link CodeDeployConfig.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withCodeDeployConfig(Consumer<CodeDeployConfig.Builder> configurer) {
        CodeDeployConfig.Builder builder = CodeDeployConfig.builder();
        configurer.accept(builder);
        this.codeDeployConfig = builder.build();
        codeDeployConfig.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * ELBv2-specific settings
     *
     * @return the ELBv2 configuration
     */
    public ElbV2Config getElbV2Config() {
        return elbV2Config;
    }

    /**
     * Configures ELBv2-specific settings.
     *
     * <pre>{@code
     * new FlociContainer()
     *     .withElbV2Config(c -> c.enabled(true));
     * }</pre>
     *
     * @param configurer a consumer that receives a {@link ElbV2Config.Builder} to modify
     * @return this container instance
     */
    public FlociContainer withElbV2Config(Consumer<ElbV2Config.Builder> configurer) {
        ElbV2Config.Builder builder = ElbV2Config.builder();
        configurer.accept(builder);
        this.elbV2Config = builder.build();
        elbV2Config.applyEnvVarsToContainer(this);
        return this;
    }

    /**
     * Configures all exposed ports of the Floci container
     */
    private void configureExposedPorts() {
        withExposedPorts(PORT);

        lambdaConfig.applyExposedPortsToContainer(this);
        rdsConfig.applyExposedPortsToContainer(this);
        elastiCacheConfig.applyExposedPortsToContainer(this);
        openSearchConfig.applyExposedPortsToContainer(this);
        ecrConfig.applyExposedPortsToContainer(this);
        eksConfig.applyExposedPortsToContainer(this);
    }

    /**
     * Configures environment variables for all services based on the current configuration objects.
     */
    private void configureEnvVars() {
        acmConfig.applyEnvVarsToContainer(this);
        apiGatewayConfig.applyEnvVarsToContainer(this);
        apiGatewayV2Config.applyEnvVarsToContainer(this);
        appConfigConfig.applyEnvVarsToContainer(this);
        appConfigDataConfig.applyEnvVarsToContainer(this);
        cloudFormationConfig.applyEnvVarsToContainer(this);
        cloudWatchLogsConfig.applyEnvVarsToContainer(this);
        cloudWatchMetricsConfig.applyEnvVarsToContainer(this);
        cognitoConfig.applyEnvVarsToContainer(this);
        dynamoDbConfig.applyEnvVarsToContainer(this);
        ec2Config.applyEnvVarsToContainer(this);
        ecrConfig.applyEnvVarsToContainer(this);
        ecsConfig.applyEnvVarsToContainer(this);
        elastiCacheConfig.applyEnvVarsToContainer(this);
        eventBridgeConfig.applyEnvVarsToContainer(this);
        iamConfig.applyEnvVarsToContainer(this);
        kinesisConfig.applyEnvVarsToContainer(this);
        kmsConfig.applyEnvVarsToContainer(this);
        lambdaConfig.applyEnvVarsToContainer(this);
        openSearchConfig.applyEnvVarsToContainer(this);
        rdsConfig.applyEnvVarsToContainer(this);
        s3Config.applyEnvVarsToContainer(this);
        schedulerConfig.applyEnvVarsToContainer(this);
        secretsManagerConfig.applyEnvVarsToContainer(this);
        sesConfig.applyEnvVarsToContainer(this);
        snsConfig.applyEnvVarsToContainer(this);
        sqsConfig.applyEnvVarsToContainer(this);
        ssmConfig.applyEnvVarsToContainer(this);
        stepFunctionsConfig.applyEnvVarsToContainer(this);
        mskConfig.applyEnvVarsToContainer(this);
        firehoseConfig.applyEnvVarsToContainer(this);
        athenaConfig.applyEnvVarsToContainer(this);
        glueConfig.applyEnvVarsToContainer(this);
        resourceGroupsTaggingConfig.applyEnvVarsToContainer(this);
        bedrockRuntimeConfig.applyEnvVarsToContainer(this);
        pipesConfig.applyEnvVarsToContainer(this);
        eksConfig.applyEnvVarsToContainer(this);
        codeBuildConfig.applyEnvVarsToContainer(this);
        codeDeployConfig.applyEnvVarsToContainer(this);
        elbV2Config.applyEnvVarsToContainer(this);
    }

    private static String uniqueShortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}