package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for Resource Groups Tagging-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * ResourceGroupsTaggingConfig config = ResourceGroupsTaggingConfig.builder()
 *     .build();
 * }</pre>
 */
public class ResourceGroupsTaggingConfig extends AbstractServiceConfig {


    private ResourceGroupsTaggingConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_TAGGING_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link ResourceGroupsTaggingConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via ResourceGroupsTaggingConfig.builder()
        }

        /**
         * Enables or disables the Resource Groups Tagging service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link ResourceGroupsTaggingConfig} from this builder.
         *
         * @return the Resource Groups Tagging configuration
         */
        public ResourceGroupsTaggingConfig build() {
            return new ResourceGroupsTaggingConfig(this);
        }
    }
}
