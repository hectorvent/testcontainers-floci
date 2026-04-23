package io.floci.testcontainers;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlociContainerTest {

    @Test
    void shouldCreateContainerWithDefaultImage() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getDockerImageName()).isEqualTo("floci/floci:latest");
        }
    }

    @Test
    void shouldRejectIncompatibleImage() {
        assertThatThrownBy(() -> new FlociContainer(DockerImageName.parse("other/image:latest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnDefaultRegion() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getRegion()).isEqualTo("us-east-1");
        }
    }

    @Test
    void shouldReturnCustomRegion() {
        try (FlociContainer container = new FlociContainer()) {
            container.withRegion("eu-west-1");
            assertThat(container.getRegion()).isEqualTo("eu-west-1");
        }
    }

    @Test
    void shouldReturnDefaultAvailabilityZone() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getDefaultAvailabilityZone()).isEqualTo("us-east-1a");
        }
    }

    @Test
    void shouldReturnCustomAvailabilityZone() {
        try (FlociContainer container = new FlociContainer()) {
            container.withDefaultAvailabilityZone("eu-west-1a");
            assertThat(container.getDefaultAvailabilityZone()).isEqualTo("eu-west-1a");
        }
    }

    @Test
    void shouldReturnDefaultAccountId() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getDefaultAccountId()).isEqualTo("000000000000");
        }
    }

    @Test
    void shouldReturnCustomAccountId() {
        try (FlociContainer container = new FlociContainer()) {
            container.withDefaultAccountId("123456789012");
            assertThat(container.getDefaultAccountId()).isEqualTo("123456789012");
        }
    }

    @Test
    void shouldReturnDefaultCredentials() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getAccessKey()).isEqualTo("test");
            assertThat(container.getSecretKey()).isEqualTo("test");
        }
    }

    @Test
    void shouldExposeFlociPort() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getExposedPorts()).contains(FlociContainer.PORT);
        }
    }

    @Test
    void shouldReturnDefaultLogLevel() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getLogLevel()).isEqualTo(Level.WARN);
        }
    }

    @Test
    void shouldReturnCustomLogLevel() {
        try (FlociContainer container = new FlociContainer()) {
            container.withLogLevel(Level.DEBUG);
            assertThat(container.getLogLevel()).isEqualTo(Level.DEBUG);
        }
    }

    @Test
    void shouldFallbackToWarnForInvalidLogLevel() {
        try (FlociContainer container = new FlociContainer()) {
            container.withEnv("QUARKUS_LOG_CATEGORY__IO_GITHUB_HECTORVENT__LEVEL", "INVALID");
            assertThat(container.getLogLevel()).isEqualTo(Level.WARN);
        }
    }

    @Test
    void shouldConfigureDedicatedNetwork() {
        try (FlociContainer container = new FlociContainer()) {
            container.withDedicatedNetwork();

            String networkName = container.getDedicatedNetworkName();
            assertThat(networkName).startsWith("floci-network-");
            assertThat(networkName).hasSize("floci-network-".length() + 8);
            assertThat(container.getNetwork()).isNotNull();
        }
    }

    @Test
    void shouldCreateUniqueNetworkPerCall() {
        try (FlociContainer container1 = new FlociContainer();
             FlociContainer container2 = new FlociContainer()) {
            container1.withDedicatedNetwork();
            container2.withDedicatedNetwork();

            String network1 = container1.getDedicatedNetworkName();
            String network2 = container2.getDedicatedNetworkName();
            assertThat(network1).isNotEqualTo(network2);
        }
    }

    @Test
    void shouldStoreAcmConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withAcmConfig(c -> c.validationWaitSeconds(5));

            assertThat(container.getAcmConfig().getValidationWaitSeconds()).isEqualTo(5);
        }
    }

    @Test
    void shouldStoreApiGatewayConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withApiGatewayConfig(c -> c.enabled(false));

            assertThat(container.getApiGatewayConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreApiGatewayV2ConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withApiGatewayV2Config(c -> c.enabled(false));

            assertThat(container.getApiGatewayV2Config().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreAppConfigConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withAppConfigConfig(c -> c.enabled(false));

            assertThat(container.getAppConfigConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreAppConfigDataConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withAppConfigDataConfig(c -> c.enabled(false));

            assertThat(container.getAppConfigDataConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreCloudFormationConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withCloudFormationConfig(c -> c.enabled(false));

            assertThat(container.getCloudFormationConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreCloudWatchLogsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withCloudWatchLogsConfig(c -> c.maxEventsPerQuery(5000));

            assertThat(container.getCloudWatchLogsConfig().getMaxEventsPerQuery()).isEqualTo(5000);
        }
    }

    @Test
    void shouldStoreCloudWatchMetricsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withCloudWatchMetricsConfig(c -> c.enabled(false));

            assertThat(container.getCloudWatchMetricsConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreCognitoConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withCognitoConfig(c -> c.enabled(false));

            assertThat(container.getCognitoConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreDynamoDbConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withDynamoDbConfig(c -> c.enabled(false));

            assertThat(container.getDynamoDbConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreEc2ConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withEc2Config(c -> c.enabled(false));

            assertThat(container.getEc2Config().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreEventBridgeConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withEventBridgeConfig(c -> c.enabled(false));

            assertThat(container.getEventBridgeConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreIamConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withIamConfig(c -> c.enabled(false));

            assertThat(container.getIamConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreKinesisConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withKinesisConfig(c -> c.enabled(false));

            assertThat(container.getKinesisConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreKmsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withKmsConfig(c -> c.enabled(false));

            assertThat(container.getKmsConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreLambdaConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withLambdaConfig(c -> c
                    .defaultMemoryMb(512)
                    .ephemeral(true));

            assertThat(container.getLambdaConfig().getDefaultMemoryMb()).isEqualTo(512);
            assertThat(container.getLambdaConfig().isEphemeral()).isTrue();
        }
    }

    @Test
    void shouldStoreRdsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withRdsConfig(c -> c
                    .defaultPostgresImage("postgres:15")
                    .proxyPortRange(8000, 100));

            assertThat(container.getRdsConfig().getDefaultPostgresImage()).isEqualTo("postgres:15");
            assertThat(container.getRdsConfig().getProxyBasePort()).isEqualTo(8000);
            assertThat(container.getRdsConfig().getProxyPortsCount()).isEqualTo(100);
        }
    }

    @Test
    void shouldStoreS3ConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withS3Config(c -> c.defaultPresignExpirySeconds(7200));

            assertThat(container.getS3Config().getDefaultPresignExpirySeconds()).isEqualTo(7200);
        }
    }

    @Test
    void shouldStoreSchedulerConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withSchedulerConfig(c -> c.enabled(false));

            assertThat(container.getSchedulerConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreSecretsManagerConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withSecretsManagerConfig(c -> c.defaultRecoveryWindowDays(7));

            assertThat(container.getSecretsManagerConfig().getDefaultRecoveryWindowDays()).isEqualTo(7);
        }
    }

    @Test
    void shouldStoreSesConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withSesConfig(c -> c.enabled(false));

            assertThat(container.getSesConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreSnsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withSnsConfig(c -> c.enabled(false));

            assertThat(container.getSnsConfig().isEnabled()).isFalse();
        }
    }

    @Test
    void shouldStoreSqsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withSqsConfig(c -> c
                    .defaultVisibilityTimeout(60)
                    .maxMessageSize(131072));

            assertThat(container.getSqsConfig().getDefaultVisibilityTimeout()).isEqualTo(60);
            assertThat(container.getSqsConfig().getMaxMessageSize()).isEqualTo(131072);
        }
    }

    @Test
    void shouldStoreSsmConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withSsmConfig(c -> c.maxParameterHistory(10));

            assertThat(container.getSsmConfig().getMaxParameterHistory()).isEqualTo(10);
        }
    }

    @Test
    void shouldStoreStepFunctionsConfigOnContainer() {
        try (FlociContainer container = new FlociContainer()) {
            container.withStepFunctionsConfig(c -> c.enabled(false));

            assertThat(container.getStepFunctionsConfig().isEnabled()).isFalse();
        }
    }
}
