package io.floci.testcontainers.config;

import org.testcontainers.containers.GenericContainer;

/**
 * Base class for Floci service configurations.
 *
 * <p>Every service configuration supports an {@link #isEnabled()} flag and can apply its
 * settings to a container via {@link #applyToContainer(GenericContainer)}.
 */
public abstract class AbstractServiceConfig {

    protected static final boolean DEFAULT_ENABLED = true;

    private final boolean enabled;

    protected AbstractServiceConfig(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether this service is enabled.
     *
     * @return {@code true} if this service is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Applies this service configuration to the given container by setting
     * the appropriate environment variables.
     *
     * @param container the container to configure
     */
    public abstract void applyToContainer(GenericContainer<?> container);
}
