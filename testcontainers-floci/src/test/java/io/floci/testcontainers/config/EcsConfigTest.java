package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class EcsConfigTest {

    @Test
    void shouldApplyDefaultEcsConfig() {
        EcsConfig config = EcsConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isMock()).isFalse();
        assertThat(config.getDefaultMemoryMb()).isEqualTo(512);
        assertThat(config.getDefaultCpuUnits()).isEqualTo(256);
        assertThat(config.getDockerNetwork()).isNull();
    }

    @Test
    void shouldApplyCustomEcsConfig() {
        EcsConfig config = EcsConfig.builder()
                .enabled(false)
                .mock(true)
                .defaultMemoryMb(1024)
                .defaultCpuUnits(512)
                .dockerNetwork("my-ecs-network")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isMock()).isTrue();
        assertThat(config.getDefaultMemoryMb()).isEqualTo(1024);
        assertThat(config.getDefaultCpuUnits()).isEqualTo(512);
        assertThat(config.getDockerNetwork()).isEqualTo("my-ecs-network");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        EcsConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ECS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ECS_MOCK", "false")
                .containsEntry("FLOCI_SERVICES_ECS_DEFAULT_MEMORY_MB", "512")
                .containsEntry("FLOCI_SERVICES_ECS_DEFAULT_CPU_UNITS", "256")
                .doesNotContainKey("FLOCI_SERVICES_ECS_DOCKER_NETWORK");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        EcsConfig.builder()
                .enabled(true)
                .mock(true)
                .defaultMemoryMb(1024)
                .defaultCpuUnits(512)
                .dockerNetwork("my-ecs-network")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ECS_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ECS_MOCK", "true")
                .containsEntry("FLOCI_SERVICES_ECS_DEFAULT_MEMORY_MB", "1024")
                .containsEntry("FLOCI_SERVICES_ECS_DEFAULT_CPU_UNITS", "512")
                .containsEntry("FLOCI_SERVICES_ECS_DOCKER_NETWORK", "my-ecs-network");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        EcsConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_ECS_ENABLED", "false");
    }
}
