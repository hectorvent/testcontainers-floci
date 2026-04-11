package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for API Gateway V2-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * ApiGatewayV2Config config = ApiGatewayV2Config.builder()
 *     .build();
 * }</pre>
 */
public class ApiGatewayV2Config extends AbstractServiceConfig {


    private ApiGatewayV2Config(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_APIGATEWAYV2_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link ApiGatewayV2Config}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via ApiGatewayV2Config.builder()
        }

        /**
         * Enables or disables the API Gateway V2 service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link ApiGatewayV2Config} from this builder.
         *
         * @return the API Gateway V2 configuration
         */
        public ApiGatewayV2Config build() {
            return new ApiGatewayV2Config(this);
        }
    }
}
