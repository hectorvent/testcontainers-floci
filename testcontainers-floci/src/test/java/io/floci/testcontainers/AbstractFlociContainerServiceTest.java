package io.floci.testcontainers;

import org.slf4j.event.Level;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

/**
 * Base class for Floci service integration tests. Provides a shared {@link FlociContainer}
 * singleton (started once per JVM) and a convenience method to build pre-configured AWS SDK clients.
 */
abstract class AbstractFlociContainerServiceTest {

    protected static final FlociContainer floci = new FlociContainer().withLogLevel(Level.DEBUG);

    static {
        floci.start();

        // Floci speaks JSON 1.1 — disable CBOR which is used by some service clients (e.g. Kinesis SDK) as default
        System.setProperty("aws.cborEnabled", "false");
    }

    protected static <B extends AwsClientBuilder<B, C>, C> C client(B builder) {
        return builder
                .endpointOverride(URI.create(floci.getEndpoint()))
                .region(Region.of(floci.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
                .build();
    }

}
