package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class EksConfigTest {

    @Test
    void shouldApplyDefaultEksConfig() {
        EksConfig config = EksConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isMock()).isFalse();
        assertThat(config.getProvider()).isEqualTo("k3s");
        assertThat(config.getDefaultImage()).isEqualTo("rancher/k3s:latest");
        assertThat(config.getApiServerBasePort()).isEqualTo(6500);
        assertThat(config.getApiServerPortsCount()).isEqualTo(10);
        assertThat(config.getApiServerMaxPort()).isEqualTo(6509);
        assertThat(config.getDockerNetwork()).isNull();
    }

    @Test
    void shouldApplyCustomEksConfig() {
        EksConfig config = EksConfig.builder()
                .enabled(false)
                .mock(true)
                .provider("kind")
                .defaultImage("kindest/node:v1.30")
                .apiServerPortRange(8000, 50)
                .dockerNetwork("my-eks-network")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isMock()).isTrue();
        assertThat(config.getProvider()).isEqualTo("kind");
        assertThat(config.getDefaultImage()).isEqualTo("kindest/node:v1.30");
        assertThat(config.getApiServerBasePort()).isEqualTo(8000);
        assertThat(config.getApiServerPortsCount()).isEqualTo(50);
        assertThat(config.getApiServerMaxPort()).isEqualTo(8049);
        assertThat(config.getDockerNetwork()).isEqualTo("my-eks-network");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        EksConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_EKS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_EKS_MOCK", "false")
                .containsEntry("FLOCI_SERVICES_EKS_PROVIDER", "k3s")
                .containsEntry("FLOCI_SERVICES_EKS_DEFAULT_IMAGE", "rancher/k3s:latest")
                .containsEntry("FLOCI_SERVICES_EKS_API_SERVER_BASE_PORT", "6500")
                .containsEntry("FLOCI_SERVICES_EKS_API_SERVER_MAX_PORT", "6509")
                .doesNotContainKey("FLOCI_SERVICES_EKS_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        EksConfig.builder()
                .enabled(true)
                .mock(true)
                .provider("kind")
                .defaultImage("kindest/node:v1.30")
                .apiServerPortRange(8000, 50)
                .dockerNetwork("my-eks-network")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_EKS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_EKS_MOCK", "true")
                .containsEntry("FLOCI_SERVICES_EKS_PROVIDER", "kind")
                .containsEntry("FLOCI_SERVICES_EKS_DEFAULT_IMAGE", "kindest/node:v1.30")
                .containsEntry("FLOCI_SERVICES_EKS_API_SERVER_BASE_PORT", "8000")
                .containsEntry("FLOCI_SERVICES_EKS_API_SERVER_MAX_PORT", "8049")
                .containsEntry("FLOCI_SERVICES_EKS_DOCKER_NETWORK", "my-eks-network");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        EksConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_EKS_ENABLED", "false")
                .doesNotContainKey("FLOCI_SERVICES_EKS_MOCK")
                .doesNotContainKey("FLOCI_SERVICES_EKS_PROVIDER");
    }

    @Test
    void shouldNotExposeEksPortsWhenDisabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withEksConfig(c -> c.enabled(false).apiServerPortRange(8000, 50));

            var env = container.getEnvMap();
            assertThat(env).containsEntry("FLOCI_SERVICES_EKS_ENABLED", "false");
            assertThat(container.getExposedPorts()).doesNotContain(8000);
        }
    }
}
