package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for SQS-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SqsConfig config = SqsConfig.builder()
 *     .defaultVisibilityTimeout(60)
 *     .maxMessageSize(131072)
 *     .clearFifoDeduplicationCacheOnPurge(false)
 *     .build();
 * }</pre>
 */
public class SqsConfig extends AbstractServiceConfig {

    private static final int DEFAULT_VISIBILITY_TIMEOUT = 30;
    private static final int DEFAULT_MAX_MESSAGE_SIZE = 262144;
    private static final boolean DEFAULT_CLEAR_FIFO_DEDUPLICATION_CACHE_ON_PURGE = true;

    private final int defaultVisibilityTimeout;
    private final int maxMessageSize;
    private final boolean clearFifoDeduplicationCacheOnPurge;

    private SqsConfig(Builder builder) {
        super(builder.enabled);
        this.defaultVisibilityTimeout = builder.defaultVisibilityTimeout;
        this.maxMessageSize = builder.maxMessageSize;
        this.clearFifoDeduplicationCacheOnPurge = builder.clearFifoDeduplicationCacheOnPurge;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the default visibility timeout in seconds.
     *
     * @return the default visibility timeout in seconds
     */
    public int getDefaultVisibilityTimeout() {
        return defaultVisibilityTimeout;
    }

    /**
     * Returns the maximum message size in bytes.
     *
     * @return the maximum message size in bytes
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Returns whether the FIFO deduplication cache is cleared on queue purge.
     *
     * @return {@code true} if the FIFO deduplication cache is cleared on purge
     */
    public boolean isClearFifoDeduplicationCacheOnPurge() {
        return clearFifoDeduplicationCacheOnPurge;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_SQS_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            container.withEnv("FLOCI_SERVICES_SQS_DEFAULT_VISIBILITY_TIMEOUT", String.valueOf(defaultVisibilityTimeout));
            container.withEnv("FLOCI_SERVICES_SQS_MAX_MESSAGE_SIZE", String.valueOf(maxMessageSize));
            container.withEnv("FLOCI_SERVICES_SQS_CLEAR_FIFO_DEDUPLICATION_CACHE_ON_PURGE", String.valueOf(clearFifoDeduplicationCacheOnPurge));
        }
    }

    /**
     * Builder for {@link SqsConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private int defaultVisibilityTimeout = DEFAULT_VISIBILITY_TIMEOUT;
        private int maxMessageSize = DEFAULT_MAX_MESSAGE_SIZE;
        private boolean clearFifoDeduplicationCacheOnPurge = DEFAULT_CLEAR_FIFO_DEDUPLICATION_CACHE_ON_PURGE;

        private Builder() {
            // Allow instantiation only via SqsConfig.builder()
        }

        /**
         * Enables or disables the SQS service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the default visibility timeout for messages in seconds.
         *
         * @param defaultVisibilityTimeout the default visibility timeout for messages in seconds (default {@value DEFAULT_VISIBILITY_TIMEOUT})
         * @return this builder
         */
        public Builder defaultVisibilityTimeout(int defaultVisibilityTimeout) {
            this.defaultVisibilityTimeout = defaultVisibilityTimeout;
            return this;
        }

        /**
         * Sets the maximum message size in bytes.
         *
         * @param maxMessageSize the maximum message size in bytes (default {@value DEFAULT_MAX_MESSAGE_SIZE})
         * @return this builder
         */
        public Builder maxMessageSize(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
            return this;
        }

        /**
         * Sets whether the FIFO deduplication cache should be cleared on queue purge.
         *
         * @param clearFifoDeduplicationCacheOnPurge {@code true} to clear the cache on purge (default {@value DEFAULT_CLEAR_FIFO_DEDUPLICATION_CACHE_ON_PURGE})
         * @return this builder
         */
        public Builder clearFifoDeduplicationCacheOnPurge(boolean clearFifoDeduplicationCacheOnPurge) {
            this.clearFifoDeduplicationCacheOnPurge = clearFifoDeduplicationCacheOnPurge;
            return this;
        }

        /**
         * Creates an immutable {@link SqsConfig} from this builder.
         *
         * @return the SQS configuration
         */
        public SqsConfig build() {
            return new SqsConfig(this);
        }
    }
}
