package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for CloudFormation-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * CloudFormationConfig config = CloudFormationConfig.builder()
 *     .build();
 * }</pre>
 */
public class CloudFormationConfig extends AbstractServiceConfig {



    private CloudFormationConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_CLOUDFORMATION_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link CloudFormationConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via CloudFormationConfig.builder()
        }

        /**
         * Enables or disables the CloudFormation service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link CloudFormationConfig} from this builder.
         *
         * @return the CloudFormation configuration
         */
        public CloudFormationConfig build() {
            return new CloudFormationConfig(this);
        }
    }
}
