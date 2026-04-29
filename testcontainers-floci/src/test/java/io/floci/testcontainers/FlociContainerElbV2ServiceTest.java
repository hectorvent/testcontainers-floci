package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerElbV2ServiceTest extends AbstractFlociContainerServiceTest {

    static ElasticLoadBalancingV2Client elbV2;

    @BeforeAll
    static void setUp() {
        elbV2 = client(ElasticLoadBalancingV2Client.builder());
    }

    @Test
    void shouldDescribeLoadBalancers() {
        List<LoadBalancer> loadBalancers = elbV2.describeLoadBalancers().loadBalancers();

        assertThat(loadBalancers).isNotNull();
    }

}
