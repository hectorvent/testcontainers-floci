package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.codedeploy.CodeDeployClient;
import software.amazon.awssdk.services.codedeploy.model.ListApplicationsResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerCodeDeployServiceTest extends AbstractFlociContainerServiceTest {

    static CodeDeployClient codeDeploy;

    @BeforeAll
    static void setUp() {
        codeDeploy = client(CodeDeployClient.builder());
    }

    @Test
    void shouldListApplications() {
        ListApplicationsResponse response = codeDeploy.listApplications();

        List<String> applications = response.applications();
        assertThat(applications).isNotNull();
    }

}
