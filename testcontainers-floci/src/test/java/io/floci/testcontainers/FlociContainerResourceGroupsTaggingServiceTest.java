package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient;
import software.amazon.awssdk.services.resourcegroupstaggingapi.model.GetResourcesResponse;
import software.amazon.awssdk.services.resourcegroupstaggingapi.model.TagFilter;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerResourceGroupsTaggingServiceTest extends AbstractFlociContainerServiceTest {

    static ResourceGroupsTaggingApiClient tagging;

    @BeforeAll
    static void setUp() {
        tagging = client(ResourceGroupsTaggingApiClient.builder());
    }

    @Test
    void shouldGetResources() {
        GetResourcesResponse response = tagging.getResources(b -> {});

        assertThat(response).isNotNull();
        assertThat(response.resourceTagMappingList()).isNotNull();
    }

    @Test
    void shouldGetResourcesWithTagFilter() {
        GetResourcesResponse response = tagging.getResources(b -> b
                .tagFilters(TagFilter.builder()
                        .key("Environment")
                        .values("test")
                        .build()));

        assertThat(response).isNotNull();
        assertThat(response.resourceTagMappingList()).isNotNull();
    }

}
