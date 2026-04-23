package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.DeliveryStreamType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerFirehoseServiceTest extends AbstractFlociContainerServiceTest {

    static FirehoseClient firehose;

    @BeforeAll
    static void setUp() {
        firehose = client(FirehoseClient.builder());
    }

    @Test
    void shouldCreateAndListDeliveryStream() {
        String streamName = "test-stream-" + System.currentTimeMillis();

        firehose.createDeliveryStream(b -> b
                .deliveryStreamName(streamName)
                .deliveryStreamType(DeliveryStreamType.DIRECT_PUT));

        List<String> streamNames = firehose.listDeliveryStreams(b -> {}).deliveryStreamNames();

        assertThat(streamNames).contains(streamName);
    }

    @Test
    void shouldDescribeDeliveryStream() {
        String streamName = "test-describe-" + System.currentTimeMillis();

        firehose.createDeliveryStream(b -> b
                .deliveryStreamName(streamName)
                .deliveryStreamType(DeliveryStreamType.DIRECT_PUT));

        String arn = firehose.describeDeliveryStream(b -> b.deliveryStreamName(streamName))
                .deliveryStreamDescription()
                .deliveryStreamARN();

        assertThat(arn).contains(streamName);
    }

}
