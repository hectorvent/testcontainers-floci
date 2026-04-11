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
            assertThat(container.getDockerImageName()).isEqualTo("hectorvent/floci:latest");
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
    void shouldReturnDefaultCredentials() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getAccessKey()).isEqualTo("test");
            assertThat(container.getSecretKey()).isEqualTo("test");
        }
    }

    @Test
    void shouldExposeFlociPort() {
        try (FlociContainer container = new FlociContainer()) {
            assertThat(container.getExposedPorts()).containsOnly(FlociContainer.PORT);
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
}
