package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.IdentityType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class FlociContainerSesServiceTest extends AbstractFlociContainerServiceTest {

    static SesClient ses;

    @BeforeAll
    static void setUp() {
        ses = client(SesClient.builder());
    }

    @Test
    void shouldVerifyEmailIdentity() {
        String email = "test-" + System.currentTimeMillis() + "@example.com";

        assertThatNoException().isThrownBy(() ->
                ses.verifyEmailIdentity(b -> b.emailAddress(email)));

        List<String> identities = ses.listIdentities(b -> b.identityType(IdentityType.EMAIL_ADDRESS)).identities();

        assertThat(identities).contains(email);
    }

    @Test
    void shouldListIdentities() {
        List<String> identities = ses.listIdentities().identities();

        assertThat(identities).isNotNull();
    }

}
