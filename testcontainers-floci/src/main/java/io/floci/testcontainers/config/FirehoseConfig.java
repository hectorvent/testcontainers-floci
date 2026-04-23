package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Firehose-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * FirehoseConfig config = FirehoseConfig.builder()
 *     .build();
 * }</pre>
 */
public class FirehoseConfig extends AbstractServiceConfig {


    private FirehoseConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_FIREHOSE_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link FirehoseConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via FirehoseConfig.builder()
        }

        /**
         * Enables or disables the Firehose service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link FirehoseConfig} from this builder.
         *
         * @return the Firehose configuration
         */
        public FirehoseConfig build() {
            return new FirehoseConfig(this);
        }
    }
}
