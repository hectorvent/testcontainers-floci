package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Vpc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerEc2ServiceTest extends AbstractFlociContainerServiceTest {

    static Ec2Client ec2;

    @BeforeAll
    static void setUp() {
        ec2 = client(Ec2Client.builder());
    }

    @Test
    void shouldCreateAndDescribeVpc() {
        String vpcId = ec2.createVpc(b -> b.cidrBlock("10.0.0.0/16")).vpc().vpcId();

        assertThat(vpcId).isNotBlank();

        List<Vpc> vpcs = ec2.describeVpcs(b -> b.vpcIds(vpcId)).vpcs();

        assertThat(vpcs).hasSize(1);
        assertThat(vpcs.get(0).cidrBlock()).isEqualTo("10.0.0.0/16");
    }

    @Test
    void shouldCreateAndDescribeSecurityGroup() {
        String vpcId = ec2.createVpc(b -> b.cidrBlock("10.1.0.0/16")).vpc().vpcId();

        String groupName = "test-sg-" + System.currentTimeMillis();
        String groupId = ec2.createSecurityGroup(b -> b
                .groupName(groupName)
                .description("Test security group")
                .vpcId(vpcId)).groupId();

        assertThat(groupId).isNotBlank();

        var groups = ec2.describeSecurityGroups(b -> b.groupIds(groupId)).securityGroups();

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).groupName()).isEqualTo(groupName);
    }

}
