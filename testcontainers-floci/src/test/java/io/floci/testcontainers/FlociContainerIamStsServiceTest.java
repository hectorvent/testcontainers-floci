package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerIamStsServiceTest extends AbstractFlociContainerServiceTest {

    static StsClient sts;

    @BeforeAll
    static void setUp() {
        sts = client(StsClient.builder());
    }

    @Test
    void shouldGetCallerIdentity() {
        GetCallerIdentityResponse identity = sts.getCallerIdentity();

        assertThat(identity.account()).isNotBlank();
        assertThat(identity.arn()).isNotBlank();
    }

}
