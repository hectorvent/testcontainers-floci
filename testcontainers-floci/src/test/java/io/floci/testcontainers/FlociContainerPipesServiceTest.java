package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.pipes.PipesClient;
import software.amazon.awssdk.services.pipes.model.PipeState;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerPipesServiceTest extends AbstractFlociContainerServiceTest {

    static PipesClient pipes;

    @BeforeAll
    static void setUp() {
        pipes = client(PipesClient.builder());
    }

    @Test
    void shouldListPipes() {
        var response = pipes.listPipes(b -> {});

        assertThat(response).isNotNull();
        assertThat(response.pipes()).isNotNull();
    }

    @Test
    void shouldCreateAndDescribePipe() {
        String pipeName = "test-pipe-" + System.currentTimeMillis();

        pipes.createPipe(b -> b
                .name(pipeName)
                .source("arn:aws:sqs:us-east-1:000000000000:test-source")
                .target("arn:aws:sqs:us-east-1:000000000000:test-target")
                .roleArn("arn:aws:iam::000000000000:role/test-role"));

        var response = pipes.describePipe(b -> b.name(pipeName));

        assertThat(response.name()).isEqualTo(pipeName);
    }

}
