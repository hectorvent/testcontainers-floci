package io.floci.testcontainers;

import org.apache.hc.core5.http.HttpHost;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.HealthStatus;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5Transport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
        assertThat(status.endpoint()).isNotBlank();
    }

    @Test
    @Order(3)
    @Disabled
    void shouldWaitForOpenSearchReady() {
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    try (var dataTransport = createTransport()) {
                        var dataClient = new org.opensearch.client.opensearch.OpenSearchClient(dataTransport);
                        HealthStatus status = dataClient.cluster()
                                .health(h -> h.waitForStatus(HealthStatus.Green).timeout(t -> t.time("5s")))
                                .status();
                        assertThat(status).isIn(HealthStatus.Green, HealthStatus.Yellow);
                    }
                });
    }

    @Test
    @Order(4)
    @Disabled
    void shouldIndexDocument() throws Exception {
        try (var dataTransport = createTransport()) {
            var dataClient = new org.opensearch.client.opensearch.OpenSearchClient(dataTransport);

            dataClient.index(i -> i
                    .index("test-index")
                    .id("1")
                    .document(new IndexData("Floci OpenSearch Test",
                            "integration testing with testcontainers")));
        }
    }

    @Test
    @Order(5)
    @Disabled
    void shouldSearchDocument() throws Exception {
        try (var dataTransport = createTransport()) {
            var dataClient = new org.opensearch.client.opensearch.OpenSearchClient(dataTransport);

            dataClient.indices().refresh(r -> r.index("test-index"));

            SearchResponse<IndexData> response = dataClient.search(s -> s
                            .index("test-index")
                            .query(q -> q
                                    .match(m -> m
                                            .field("content")
                                            .query(FieldValue.of("testcontainers")))),
                    IndexData.class);

            List<String> titles = response.hits().hits().stream()
                    .map(Hit::source)
                    .map(IndexData::title)
                    .toList();

            assertThat(titles).contains("Floci OpenSearch Test");
        }
    }

    @Test
    @Order(6)
    void shouldDeleteDomain() {
        openSearch.deleteDomain(b -> b.domainName(domainName));

        List<String> domainNames = openSearch.listDomainNames(b -> {
                }).domainNames().stream()
                .map(DomainInfo::domainName)
                .toList();

        assertThat(domainNames).doesNotContain(domainName);
    }

    private ApacheHttpClient5Transport createTransport() {
        var host = new HttpHost("http", floci.getHost(),
                floci.getMappedPort(floci.getOpenSearchConfig().getProxyBasePort()));

        return ApacheHttpClient5TransportBuilder.builder(host)
                .setMapper(new JacksonJsonpMapper())
                .build();
    }

    private record IndexData(String title, String content) {
    }
}
