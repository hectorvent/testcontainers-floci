package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerOpenSearchServiceTest extends AbstractFlociContainerServiceTest {

    static OpenSearchClient openSearch;
    static String domainName;

    @BeforeAll
    static void setUp() {
        domainName = "test-" + System.currentTimeMillis();
        openSearch = client(OpenSearchClient.builder());
    }

    @Test
    @Order(1)
    void shouldCreateDomain() {
        openSearch.createDomain(b -> b.domainName(domainName));

        List<String> domainNames = openSearch.listDomainNames(b -> {
                }).domainNames().stream()
                .map(DomainInfo::domainName)
                .toList();

        assertThat(domainNames).contains(domainName);
    }

    @Test
    @Order(2)
    void shouldDescribeDomain() {
        DomainStatus status = openSearch.describeDomain(b -> b.domainName(domainName)).domainStatus();

        assertThat(status.domainName()).isEqualTo(domainName);
        assertThat(status.arn()).isNotBlank();
    }


    @Test
    @Order(3)
    void shouldDeleteDomain() {
        openSearch.deleteDomain(b -> b.domainName(domainName));

        List<String> domainNames = openSearch.listDomainNames(b -> {
                }).domainNames().stream()
                .map(DomainInfo::domainName)
                .toList();

        assertThat(domainNames).doesNotContain(domainName);
    }
}
