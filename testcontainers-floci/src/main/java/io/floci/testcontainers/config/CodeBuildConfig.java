package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for CodeBuild-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * CodeBuildConfig config = CodeBuildConfig.builder()
 *     .build();
 * }</pre>
 */
public class CodeBuildConfig extends AbstractServiceConfig {


    private CodeBuildConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_CODEBUILD_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link CodeBuildConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via CodeBuildConfig.builder()
        }

        /**
         * Enables or disables the CodeBuild service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link CodeBuildConfig} from this builder.
         *
         * @return the CodeBuild configuration
         */
        public CodeBuildConfig build() {
            return new CodeBuildConfig(this);
        }
    }
}
