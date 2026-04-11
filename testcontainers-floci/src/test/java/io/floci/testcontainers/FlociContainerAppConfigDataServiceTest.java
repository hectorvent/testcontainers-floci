package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerAppConfigDataServiceTest extends AbstractFlociContainerServiceTest {

    static AppConfigClient appConfig;
    static AppConfigDataClient appConfigData;

    @BeforeAll
    static void setUp() {
        appConfig = client(AppConfigClient.builder());
        appConfigData = client(AppConfigDataClient.builder());
    }

    @Test
    void shouldStartConfigurationSessionAndGetLatestConfiguration() {
        String appId = appConfig.createApplication(b -> b
                .name("data-test-app-" + System.currentTimeMillis())).id();

        String envId = appConfig.createEnvironment(b -> b
                .applicationId(appId)
                .name("test-env")).id();

        String profileId = appConfig.createConfigurationProfile(b -> b
                .applicationId(appId)
                .name("test-profile")
                .locationUri("hosted")).id();

        appConfig.createHostedConfigurationVersion(b -> b
                .applicationId(appId)
                .configurationProfileId(profileId)
                .contentType("application/json")
                .content(SdkBytes.fromUtf8String("{\"feature\":true}")));

        String strategyId = appConfig.createDeploymentStrategy(b -> b
                .name("immediate-" + System.currentTimeMillis())
                .deploymentDurationInMinutes(0)
                .finalBakeTimeInMinutes(0)
                .growthFactor(100.0f)).id();

        appConfig.startDeployment(b -> b
                .applicationId(appId)
                .environmentId(envId)
                .configurationProfileId(profileId)
                .configurationVersion("1")
                .deploymentStrategyId(strategyId));

        StartConfigurationSessionResponse session = appConfigData.startConfigurationSession(b -> b
                .applicationIdentifier(appId)
                .environmentIdentifier(envId)
                .configurationProfileIdentifier(profileId));

        assertThat(session.initialConfigurationToken()).isNotBlank();

        GetLatestConfigurationResponse config = appConfigData.getLatestConfiguration(b -> b
                .configurationToken(session.initialConfigurationToken()));

        assertThat(config.configuration().asUtf8String()).isEqualTo("{\"feature\":true}");
    }

}
