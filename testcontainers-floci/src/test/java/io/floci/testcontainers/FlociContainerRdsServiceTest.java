package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerRdsServiceTest extends AbstractFlociContainerServiceTest {

    static RdsClient rds;

    @BeforeAll
    static void setUp() {
        rds = client(RdsClient.builder());
    }

    @Test
    void shouldDescribeDbInstances() {
        List<DBInstance> instances = rds.describeDBInstances().dbInstances();

        assertThat(instances).isNotNull();
    }

    @Test
    void shouldDescribeDbClusters() {
        var clusters = rds.describeDBClusters().dbClusters();

        assertThat(clusters).isNotNull();
    }
}
