package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for DynamoDB-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * DynamoDbConfig config = DynamoDbConfig.builder()
 *     .build();
 * }</pre>
 */
public class DynamoDbConfig extends AbstractServiceConfig {



    private DynamoDbConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_DYNAMODB_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link DynamoDbConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via DynamoDbConfig.builder()
        }

        /**
         * Enables or disables the DynamoDB service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link DynamoDbConfig} from this builder.
         *
         * @return the DynamoDB configuration
         */
        public DynamoDbConfig build() {
            return new DynamoDbConfig(this);
        }
    }
}
