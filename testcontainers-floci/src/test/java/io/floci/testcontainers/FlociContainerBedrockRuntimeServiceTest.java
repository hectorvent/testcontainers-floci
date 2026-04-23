package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerBedrockRuntimeServiceTest extends AbstractFlociContainerServiceTest {

    static BedrockRuntimeClient bedrockRuntime;

    @BeforeAll
    static void setUp() {
        bedrockRuntime = client(BedrockRuntimeClient.builder());
    }

    @Test
    void shouldInvokeModel() {
        String requestBody = """
                {"inputText": "Hello from Floci Bedrock Runtime!"}
                """;

        InvokeModelResponse response = bedrockRuntime.invokeModel(b -> b
                .modelId("amazon.titan-text-express-v1")
                .contentType("application/json")
                .body(SdkBytes.fromUtf8String(requestBody)));

        assertThat(response).isNotNull();
        assertThat(response.body()).isNotNull();
    }

}
