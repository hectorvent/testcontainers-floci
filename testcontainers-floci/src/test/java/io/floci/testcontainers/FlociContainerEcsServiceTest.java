package io.floci.testcontainers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.PortMapping;
import software.amazon.awssdk.services.ecs.model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(OrderAnnotation.class)
@Disabled
class FlociContainerEcsServiceTest extends AbstractFlociContainerServiceTest {

    static EcsClient ecs;
    static String clusterName;
    static String taskFamily;
    static String taskArn;

    @BeforeAll
    static void setUp() {
        clusterName = "test-cluster-" + System.currentTimeMillis();
        taskFamily = "test-task-" + System.currentTimeMillis();
        ecs = client(EcsClient.builder());
    }

    @Test
    @Order(1)
    void shouldCreateCluster() {
        String clusterArn = ecs.createCluster(b -> b.clusterName(clusterName)).cluster().clusterArn();

        assertThat(clusterArn).isNotBlank();

        List<String> clusterArns = ecs.listClusters().clusterArns();
        assertThat(clusterArns).contains(clusterArn);
    }

    @Test
    @Order(2)
    void shouldDescribeCluster() {
        List<Cluster> clusters = ecs.describeClusters(b -> b.clusters(clusterName)).clusters();

        assertThat(clusters).hasSize(1);
        assertThat(clusters.get(0).clusterName()).isEqualTo(clusterName);
    }

    @Test
    @Order(3)
    void shouldRegisterTaskDefinitionAndRunTask() {
        ecs.registerTaskDefinition(b -> b
                .family(taskFamily)
                .containerDefinitions(ContainerDefinition.builder()
                        .name("whoami")
                        .image("traefik/whoami")
                        .portMappings(PortMapping.builder()
                                .containerPort(80)
                                .protocol("tcp")
                                .build())
                        .memory(64)
                        .cpu(64)
                        .build()));

        var result = ecs.runTask(b -> b
                .cluster(clusterName)
                .taskDefinition(taskFamily));

        assertThat(result.tasks()).isNotEmpty();
        taskArn = result.tasks().get(0).taskArn();
        assertThat(taskArn).isNotBlank();
    }

    @Test
    @Order(4)
    void shouldReachRunningState() {
        await().atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    List<Task> tasks = ecs.describeTasks(b -> b
                            .cluster(clusterName)
                            .tasks(taskArn)).tasks();
                    assertThat(tasks).hasSize(1);
                    assertThat(tasks.get(0).lastStatus()).isEqualTo("RUNNING");
                });
    }

    @Test
    @Order(5)
    void shouldConnectToTaskWebServer() {
        int hostPort = ecs.describeTasks(b -> b
                        .cluster(clusterName)
                        .tasks(taskArn))
                .tasks()
                .stream()
                .flatMap(t -> t.containers().stream())
                .flatMap(c -> c.networkBindings().stream())
                .filter(nb -> nb.containerPort() == 80)
                .findFirst()
                .orElseThrow()
                .hostPort();

        URI uri = URI.create("http://localhost:" + hostPort);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpClient httpClient = HttpClient.newHttpClient();

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    HttpResponse<String> response = httpClient.send(request, ofString());
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.body()).contains("Hostname:");
                });
    }

    @Test
    @Order(6)
    void shouldStopTask() {
        ecs.stopTask(b -> b
                .cluster(clusterName)
                .task(taskArn));

        List<Task> tasks = ecs.describeTasks(b -> b
                .cluster(clusterName)
                .tasks(taskArn)).tasks();

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).lastStatus()).isIn("STOPPED", "STOPPING");
    }

    @Test
    @Order(7)
    void shouldReachStoppedState() {
        await().atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    List<Task> tasks = ecs.describeTasks(b -> b
                            .cluster(clusterName)
                            .tasks(taskArn)).tasks();
                    assertThat(tasks).hasSize(1);
                    assertThat(tasks.get(0).lastStatus()).isEqualTo("STOPPED");
                });
    }

    @Test
    @Order(8)
    void shouldDeleteCluster() {
        ecs.deleteCluster(b -> b.cluster(clusterName));

        List<String> clusterArns = ecs.listClusters().clusterArns();
        assertThat(clusterArns).noneMatch(arn -> arn.contains(clusterName));
    }
}
