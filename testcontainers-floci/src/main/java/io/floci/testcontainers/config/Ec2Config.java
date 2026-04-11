package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Configuration for EC2-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * Ec2Config config = Ec2Config.builder()
 *     .build();
 * }</pre>
 */
public class Ec2Config extends AbstractServiceConfig {



    private Ec2Config(Builder builder) {
        super(builder.enabled);
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public void applyToContainer(GenericContainer<?> container) {
        container.withEnv("FLOCI_SERVICES_EC2_ENABLED", String.valueOf(isEnabled()));
    }

    /**
     * Builder for {@link Ec2Config}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;

        private Builder() {
            // Allow instantiation only via Ec2Config.builder()
        }

        /**
         * Enables or disables the EC2 service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Creates an immutable {@link Ec2Config} from this builder.
         *
         * @return the EC2 configuration
         */
        public Ec2Config build() {
            return new Ec2Config(this);
        }
    }
}
