package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for AppConfig-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * AppConfigConfig config = AppConfigConfig.builder()
 *     .build();
 * }</pre>
 */
public class AppConfigConfig extends AbstractServiceConfig {



    private AppConfigConfig(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_APPCONFIG_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link AppConfigConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via AppConfigConfig.builder()
        }

        /**
         * Enables or disables the AppConfig service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link AppConfigConfig} from this builder.
         *
         * @return the AppConfig configuration
         */
        public AppConfigConfig build() {
            return new AppConfigConfig(this);
        }
    }
}
