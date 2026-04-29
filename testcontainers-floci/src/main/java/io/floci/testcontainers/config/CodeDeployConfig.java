package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for CodeDeploy-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * CodeDeployConfig config = CodeDeployConfig.builder()
 *     .build();
 * }</pre>
 */
public class CodeDeployConfig extends AbstractServiceConfig {


    private CodeDeployConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_CODEDEPLOY_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link CodeDeployConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via CodeDeployConfig.builder()
        }

        /**
         * Enables or disables the CodeDeploy service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link CodeDeployConfig} from this builder.
         *
         * @return the CodeDeploy configuration
         */
        public CodeDeployConfig build() {
            return new CodeDeployConfig(this);
        }
    }
}
