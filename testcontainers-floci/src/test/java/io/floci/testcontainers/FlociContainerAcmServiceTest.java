package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateSummary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerAcmServiceTest extends AbstractFlociContainerServiceTest {

    static AcmClient acm;

    @BeforeAll
    static void setUp() {
        acm = client(AcmClient.builder());
    }

    @Test
    void shouldListCertificates() {
        List<CertificateSummary> certificates = acm.listCertificates().certificateSummaryList();

        assertThat(certificates).isNotNull();
    }

}
