package io.floci.testcontainers.spring;

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3ClientCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Auto-configuration that customizes Spring Cloud AWS clients when a Floci
 * container is connected via {@code @ServiceConnection}.
 *
 * <p>Enables path-style access on the S3 client, which is required for
 * local AWS emulators like Floci that do not support virtual-hosted-style
 * bucket addressing.
 */
@AutoConfiguration
@ConditionalOnClass({S3ClientCustomizer.class, S3ClientBuilder.class})
@ConditionalOnBean(AwsConnectionDetails.class)
@AutoConfigureAfter(S3AutoConfiguration.class)
public class FlociAwsAutoConfiguration {

    @Bean
    S3ClientCustomizer flociS3PathStyleCustomizer() {
        return builder -> builder.forcePathStyle(true);
    }
}
