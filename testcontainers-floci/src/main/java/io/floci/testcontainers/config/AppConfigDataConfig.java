package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for AppConfig Data-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * AppConfigDataConfig config = AppConfigDataConfig.builder()
 *     .build();
 * }</pre>
 */
public class AppConfigDataConfig extends AbstractServiceConfig {



    private AppConfigDataConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_APPCONFIGDATA_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link AppConfigDataConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via AppConfigDataConfig.builder()
        }

        /**
         * Enables or disables the AppConfig Data service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link AppConfigDataConfig} from this builder.
         *
         * @return the AppConfig Data configuration
         */
        public AppConfigDataConfig build() {
            return new AppConfigDataConfig(this);
        }
    }
}
