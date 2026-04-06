package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.ProtocolType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerApiGatewayV2ServiceTest extends AbstractFlociContainerServiceTest {

    static ApiGatewayV2Client apiGatewayV2;

    @BeforeAll
    static void setUp() {
        apiGatewayV2 = client(ApiGatewayV2Client.builder());
    }

    @Test
    void shouldCreateAndListHttpApi() {
        String apiName = "test-http-api-" + System.currentTimeMillis();

        String apiId = apiGatewayV2.createApi(b -> b
                .name(apiName)
                .protocolType(ProtocolType.HTTP)
                .description("Test HTTP API")).apiId();

        assertThat(apiId).isNotBlank();

        List<String> apiNames = apiGatewayV2.getApis().items().stream()
                .map(Api::name)
                .toList();

        assertThat(apiNames).contains(apiName);
    }

    @Test
    void shouldCreateRoute() {
        String apiName = "test-route-api-" + System.currentTimeMillis();

        String apiId = apiGatewayV2.createApi(b -> b
                .name(apiName)
                .protocolType(ProtocolType.HTTP)).apiId();

        String routeId = apiGatewayV2.createRoute(b -> b
                .apiId(apiId)
                .routeKey("GET /test")).routeId();

        assertThat(routeId).isNotBlank();
    }

}
