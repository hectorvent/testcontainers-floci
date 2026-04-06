package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerOpenSearchServiceTest extends AbstractFlociContainerServiceTest {

    static OpenSearchClient openSearch;

    @BeforeAll
    static void setUp() {
        openSearch = client(OpenSearchClient.builder());
    }

    @Test
    void shouldCreateAndListDomain() {
        String domainName = "test-" + System.currentTimeMillis();

        openSearch.createDomain(b -> b.domainName(domainName));

        List<String> domainNames = openSearch.listDomainNames(b -> {}).domainNames().stream()
                .map(DomainInfo::domainName)
                .toList();

        assertThat(domainNames).contains(domainName);
    }

    @Test
    void shouldDescribeDomain() {
        String domainName = "describe-" + System.currentTimeMillis();

        openSearch.createDomain(b -> b.domainName(domainName));

        DomainStatus status = openSearch.describeDomain(b -> b.domainName(domainName)).domainStatus();

        assertThat(status.domainName()).isEqualTo(domainName);
    }

}
