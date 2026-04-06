package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerEcsServiceTest extends AbstractFlociContainerServiceTest {

    static EcsClient ecs;

    @BeforeAll
    static void setUp() {
        ecs = client(EcsClient.builder());
    }

    @Test
    void shouldCreateAndListCluster() {
        String clusterName = "test-cluster-" + System.currentTimeMillis();

        String clusterArn = ecs.createCluster(b -> b.clusterName(clusterName)).cluster().clusterArn();

        assertThat(clusterArn).isNotBlank();

        List<String> clusterArns = ecs.listClusters().clusterArns();

        assertThat(clusterArns).contains(clusterArn);
    }

    @Test
    void shouldDescribeCluster() {
        String clusterName = "test-describe-" + System.currentTimeMillis();

        ecs.createCluster(b -> b.clusterName(clusterName));

        List<Cluster> clusters = ecs.describeClusters(b -> b.clusters(clusterName)).clusters();

        assertThat(clusters).hasSize(1);
        assertThat(clusters.get(0).clusterName()).isEqualTo(clusterName);
    }

}
