package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.Runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerLambdaServiceTest extends AbstractFlociContainerServiceTest {

    static LambdaClient lambda;

    static String functionName;

    @BeforeAll
    static void setUp() {
        functionName = "test-fn-" + System.currentTimeMillis();
        lambda = client(LambdaClient.builder());
    }

    @Test
    @Order(1)
    void shouldListFunctions() {
        List<FunctionConfiguration> functions = lambda.listFunctions().functions();

        // A fresh Floci instance has no functions
        assertThat(functions).isEmpty();
    }

    @Test
    @Order(2)
    void shouldCreateFunction() throws IOException {
        String handlerCode = """
                export const handler = async (event) => {
                    return { statusCode: 200, body: JSON.stringify({ greeting: "hello from floci lambda" }) };
                };
                """;

        SdkBytes zip = SdkBytes.fromByteArray(createZip("index.mjs", handlerCode));

        lambda.createFunction(b -> b
                .functionName(functionName)
                .runtime(Runtime.NODEJS22_X)
                .role("arn:aws:iam::000000000000:role/lambda-role")
                .handler("index.handler")
                .timeout(30)
                .code(c -> c.zipFile(zip)));

        FunctionConfiguration fn = lambda.getFunction(b -> b.functionName(functionName)).configuration();
        assertThat(fn.functionName()).isEqualTo(functionName);
        assertThat(fn.runtime()).isEqualTo(Runtime.NODEJS22_X);
    }

    @Test
    @Order(3)
    void shouldInvokeFunction() {
        InvokeResponse invokeResponse = lambda.invoke(b -> b
                .functionName(functionName)
                .payload(SdkBytes.fromUtf8String("{\"key\":\"value\"}")));

        assertThat(invokeResponse.statusCode()).isEqualTo(200);
        String payload = invokeResponse.payload().asUtf8String();
        assertThat(payload).contains("hello from floci lambda");
    }

    @Test
    @Order(4)
    void shouldDeleteFunction() {
        lambda.deleteFunction(b -> b.functionName(functionName));

        assertThat(lambda.listFunctions().functions()).isEmpty();
    }

    private static byte[] createZip(String entryName, String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
