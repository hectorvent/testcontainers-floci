package io.floci.testcontainers;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.ClusterStatus;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerEksServiceTest extends AbstractFlociContainerServiceTest {

    static final String NAMESPACE_NAME = "floci-test";

    static EksClient eks;
    static String clusterName;
    static CoreV1Api coreApi;

    @BeforeAll
    static void setUp() {
        clusterName = "eks-k8s-" + System.currentTimeMillis();
        eks = client(EksClient.builder());
    }

    @Test
    @Order(1)
    void shouldCreateCluster() {
        eks.createCluster(b -> b
                .name(clusterName)
                .roleArn("arn:aws:iam::000000000000:role/eks-role"));

        List<String> names = eks.listClusters(b -> {}).clusters();
        assertThat(names).contains(clusterName);
    }

    @Test
    @Order(2)
    @Disabled
    void shouldWaitForClusterActiveAndConnect() {
        await().atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    Cluster c = eks.describeCluster(b -> b.name(clusterName)).cluster();
                    assertThat(c.status()).isEqualTo(ClusterStatus.ACTIVE);
                });

        Cluster cluster = eks.describeCluster(b -> b.name(clusterName)).cluster();
        String endpoint = cluster.endpoint();
        assertThat(endpoint).isNotBlank();

        // Build Kubernetes API client pointing at the k3s API server
        ApiClient k8sApiClient = new ApiClient();
        k8sApiClient.setBasePath(endpoint);
        k8sApiClient.setVerifyingSsl(false);

        // Use cluster CA certificate if provided
        if (cluster.certificateAuthority() != null && cluster.certificateAuthority().data() != null) {
            byte[] caCert = Base64.getDecoder().decode(cluster.certificateAuthority().data());
            k8sApiClient.setSslCaCert(new ByteArrayInputStream(caCert));
        }

        coreApi = new CoreV1Api(k8sApiClient);

        // Wait for the Kubernetes API server to be fully ready
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .ignoreExceptions()
                .untilAsserted(() -> coreApi.listNamespace().execute());
    }

    @Test
    @Order(3)
    @Disabled
    void shouldCreateNamespace() throws Exception {
        V1Namespace namespace = new V1Namespace()
                .metadata(new V1ObjectMeta().name(NAMESPACE_NAME));

        V1Namespace created = coreApi.createNamespace(namespace).execute();
        assertThat(created.getMetadata().getName()).isEqualTo(NAMESPACE_NAME);

        V1NamespaceList nsList = coreApi.listNamespace().execute();
        List<String> nsNames = nsList.getItems().stream()
                .map(ns -> ns.getMetadata().getName())
                .toList();
        assertThat(nsNames).contains(NAMESPACE_NAME, "default");
    }

    @Test
    @Order(4)
    @Disabled
    void shouldCreateAndReadConfigMap() throws Exception {
        V1ConfigMap configMap = new V1ConfigMap()
                .metadata(new V1ObjectMeta().name("test-config"))
                .data(Map.of("greeting", "hello-from-floci", "version", "1.0"));

        V1ConfigMap created = coreApi.createNamespacedConfigMap(NAMESPACE_NAME, configMap).execute();
        assertThat(created.getMetadata().getName()).isEqualTo("test-config");

        V1ConfigMap read = coreApi.readNamespacedConfigMap("test-config", NAMESPACE_NAME).execute();
        assertThat(read.getData())
                .containsEntry("greeting", "hello-from-floci")
                .containsEntry("version", "1.0");
    }

    @Test
    @Order(5)
    void shouldDeleteCluster() {
        eks.deleteCluster(b -> b.name(clusterName));

        List<String> names = eks.listClusters(b -> {}).clusters();
        assertThat(names).doesNotContain(clusterName);
    }

}
