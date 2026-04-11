package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Step Functions-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * StepFunctionsConfig config = StepFunctionsConfig.builder()
 *     .build();
 * }</pre>
 */
public class StepFunctionsConfig extends AbstractServiceConfig {

    private StepFunctionsConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_STEPFUNCTIONS_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link StepFunctionsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via StepFunctionsConfig.builder()
        }

        /**
         * Enables or disables the Step Functions service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link StepFunctionsConfig} from this builder.
         *
         * @return the Step Functions configuration
         */
        public StepFunctionsConfig build() {
            return new StepFunctionsConfig(this);
        }
    }
}
