package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.model.Application;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerAppConfigServiceTest extends AbstractFlociContainerServiceTest {

    static AppConfigClient appConfig;

    @BeforeAll
    static void setUp() {
        appConfig = client(AppConfigClient.builder());
    }

    @Test
    void shouldCreateAndListApplication() {
        String appName = "test-app-" + System.currentTimeMillis();

        String appId = appConfig.createApplication(b -> b.name(appName)).id();

        assertThat(appId).isNotBlank();

        List<String> appNames = appConfig.listApplications(b -> {}).items().stream()
                .map(Application::name)
                .toList();

        assertThat(appNames).contains(appName);
    }

    @Test
    void shouldCreateConfigurationProfile() {
        String appId = appConfig.createApplication(b -> b
                .name("profile-test-app-" + System.currentTimeMillis())).id();

        String profileId = appConfig.createConfigurationProfile(b -> b
                .applicationId(appId)
                .name("test-profile")
                .locationUri("hosted")).id();

        assertThat(profileId).isNotBlank();
    }

}
