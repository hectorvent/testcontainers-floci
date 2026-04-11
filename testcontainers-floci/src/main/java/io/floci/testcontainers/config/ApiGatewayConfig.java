package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for API Gateway-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * ApiGatewayConfig config = ApiGatewayConfig.builder()
 *     .build();
 * }</pre>
 */
public class ApiGatewayConfig extends AbstractServiceConfig {



    private ApiGatewayConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_APIGATEWAY_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link ApiGatewayConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via ApiGatewayConfig.builder()
        }

        /**
         * Enables or disables the API Gateway service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link ApiGatewayConfig} from this builder.
         *
         * @return the API Gateway configuration
         */
        public ApiGatewayConfig build() {
            return new ApiGatewayConfig(this);
        }
    }
}
