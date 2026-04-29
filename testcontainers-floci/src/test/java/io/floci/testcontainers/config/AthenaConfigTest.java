package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class AthenaConfigTest {

    @Test
    void shouldApplyDefaultAthenaConfig() {
        AthenaConfig config = AthenaConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isMock()).isFalse();
        assertThat(config.getDuckUrl()).isNull();
        assertThat(config.getDefaultImage()).isEqualTo("floci/floci-duck:latest");
    }

    @Test
    void shouldApplyCustomAthenaConfig() {
        AthenaConfig config = AthenaConfig.builder()
                .enabled(false)
                .mock(true)
                .duckUrl("jdbc:duckdb:memory:")
                .defaultImage("custom/duck:1.0")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isMock()).isTrue();
        assertThat(config.getDuckUrl()).isEqualTo("jdbc:duckdb:memory:");
        assertThat(config.getDefaultImage()).isEqualTo("custom/duck:1.0");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        AthenaConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ATHENA_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_ATHENA_MOCK", "false")
                .containsEntry("FLOCI_SERVICES_ATHENA_DEFAULT_IMAGE", "floci/floci-duck:latest")
                .doesNotContainKey("FLOCI_SERVICES_ATHENA_DUCK_URL");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        AthenaConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ATHENA_ENABLED", "false")
                .doesNotContainKey("FLOCI_SERVICES_ATHENA_MOCK")
                .doesNotContainKey("FLOCI_SERVICES_ATHENA_DEFAULT_IMAGE")
                .doesNotContainKey("FLOCI_SERVICES_ATHENA_DUCK_URL");
    }

    @Test
    void shouldApplyDuckUrlEnvVarWhenSet() {
        GenericContainer<?> container = genericContainer();
        AthenaConfig.builder()
                .duckUrl("jdbc:duckdb:memory:")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ATHENA_DUCK_URL", "jdbc:duckdb:memory:");
    }

    @Test
    void shouldApplyMockEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        AthenaConfig.builder()
                .mock(true)
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ATHENA_MOCK", "true");
    }

    @Test
    void shouldApplyCustomDefaultImageEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        AthenaConfig.builder()
                .defaultImage("custom/duck:2.0")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_ATHENA_DEFAULT_IMAGE", "custom/duck:2.0");
    }
}
