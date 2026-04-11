package io.floci.testcontainers.config;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class EcrConfigTest {

    @Test
    void shouldApplyDefaultEcrConfig() {
        EcrConfig config = EcrConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getRegistryImage()).isEqualTo("registry:2");
        assertThat(config.getRegistryContainerName()).isEqualTo("floci-ecr-registry");
        assertThat(config.getRegistryBasePort()).isEqualTo(5100);
        assertThat(config.getRegistryMaxPort()).isEqualTo(5109);
        assertThat(config.getRegistryPortsCount()).isEqualTo(10);
        assertThat(config.isTlsEnabled()).isFalse();
        assertThat(config.getUriStyle()).isEqualTo("hostname");
        assertThat(config.getDockerNetwork()).isNull();
    }

    @Test
    void shouldApplyCustomEcrConfig() {
        EcrConfig config = EcrConfig.builder()
                .enabled(false)
                .registryImage("registry:3")
                .registryContainerName("my-registry")
                .registryPortRange(6000, 50)
                .tlsEnabled(true)
                .uriStyle("path")
                .dockerNetwork("my-ecr-network")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getRegistryImage()).isEqualTo("registry:3");
        assertThat(config.getRegistryContainerName()).isEqualTo("my-registry");
        assertThat(config.getRegistryBasePort()).isEqualTo(6000);
        assertThat(config.getRegistryMaxPort()).isEqualTo(6049);
        assertThat(config.getRegistryPortsCount()).isEqualTo(50);
        assertThat(config.isTlsEnabled()).isTrue();
        assertThat(config.getUriStyle()).isEqualTo("path");
        assertThat(config.getDockerNetwork()).isEqualTo("my-ecr-network");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        EcrConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ECR_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_IMAGE", "registry:2")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_CONTAINER_NAME", "floci-ecr-registry")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_BASE_PORT", "5100")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_MAX_PORT", "5109")
                .containsEntry("FLOCI_SERVICES_ECR_TLS_ENABLED", "false")
                .containsEntry("FLOCI_SERVICES_ECR_URI_STYLE", "hostname")
                .containsEntry("FLOCI_SERVICES_ECR_KEEP_RUNNING_ON_SHUTDOWN", "false")
                .doesNotContainKey("FLOCI_SERVICES_ECR_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        EcrConfig.builder()
                .enabled(true)
                .registryImage("registry:3")
                .registryContainerName("my-registry")
                .registryPortRange(6000, 50)
                .tlsEnabled(true)
                .uriStyle("path")
                .dockerNetwork("my-ecr-network")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ECR_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_IMAGE", "registry:3")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_CONTAINER_NAME", "my-registry")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_BASE_PORT", "6000")
                .containsEntry("FLOCI_SERVICES_ECR_REGISTRY_MAX_PORT", "6049")
                .containsEntry("FLOCI_SERVICES_ECR_TLS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ECR_URI_STYLE", "path")
                .containsEntry("FLOCI_SERVICES_ECR_KEEP_RUNNING_ON_SHUTDOWN", "false")
                .containsEntry("FLOCI_SERVICES_ECR_DOCKER_NETWORK", "my-ecr-network");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        EcrConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_ECR_ENABLED", "false");
    }

    @Test
    void shouldNotExposeEcrPortsWhenDisabled() {
        try (FlociContainer container = new FlociContainer()) {
            container.withEcrConfig(c -> c.enabled(false).registryPortRange(6000, 50));

            var env = container.getEnvMap();
            assertThat(env).containsEntry("FLOCI_SERVICES_ECR_ENABLED", "false");
            assertThat(container.getExposedPorts()).doesNotContain(6000);
        }
    }
}
