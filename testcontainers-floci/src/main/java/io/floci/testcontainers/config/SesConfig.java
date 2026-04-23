package io.floci.testcontainers.config;

import org.testcontainers.containers.Container;

/**
 * Configuration for SES-specific container settings.
 *
 * <p>Instances are created via {@link Builder}:
 * <pre>{@code
 * SesConfig config = SesConfig.builder()
 *     .smtpHost("localhost")
 *     .smtpPort(587)
 *     .smtpUser("user")
 *     .smtpPass("pass")
 *     .smtpStarttls("REQUIRED")
 *     .build();
 * }</pre>
 */
public class SesConfig extends AbstractServiceConfig {

    private static final int DEFAULT_SMTP_PORT = 25;
    private static final String DEFAULT_SMTP_STARTTLS = "DISABLED";

    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUser;
    private final String smtpPass;
    private final String smtpStarttls;

    private SesConfig(Builder builder) {
        super(builder.enabled);
        this.smtpHost = builder.smtpHost;
        this.smtpPort = builder.smtpPort;
        this.smtpUser = builder.smtpUser;
        this.smtpPass = builder.smtpPass;
        this.smtpStarttls = builder.smtpStarttls;
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Returns the SMTP server host for email relay, or {@code null} if relay is disabled.
     *
     * @return the SMTP host, or {@code null}
     */
    public String getSmtpHost() {
        return smtpHost;
    }

    /**
     * Returns the SMTP server port.
     *
     * @return the SMTP port
     */
    public int getSmtpPort() {
        return smtpPort;
    }

    /**
     * Returns the SMTP authentication username, or {@code null} if no authentication.
     *
     * @return the SMTP username, or {@code null}
     */
    public String getSmtpUser() {
        return smtpUser;
    }

    /**
     * Returns the SMTP authentication password, or {@code null}.
     *
     * @return the SMTP password, or {@code null}
     */
    public String getSmtpPass() {
        return smtpPass;
    }

    /**
     * Returns the STARTTLS mode: DISABLED, OPTIONAL, or REQUIRED.
     *
     * @return the STARTTLS mode
     */
    public String getSmtpStarttls() {
        return smtpStarttls;
    }

    @Override
    public void applyEnvVarsToContainer(Container<?> container) {
        container.withEnv("FLOCI_SERVICES_SES_ENABLED", String.valueOf(isEnabled()));

        if (isEnabled()) {
            if (smtpHost != null) {
                container.withEnv("FLOCI_SERVICES_SES_SMTP_HOST", smtpHost);
            }
            container.withEnv("FLOCI_SERVICES_SES_SMTP_PORT", String.valueOf(smtpPort));
            if (smtpUser != null) {
                container.withEnv("FLOCI_SERVICES_SES_SMTP_USER", smtpUser);
            }
            if (smtpPass != null) {
                container.withEnv("FLOCI_SERVICES_SES_SMTP_PASS", smtpPass);
            }
            container.withEnv("FLOCI_SERVICES_SES_SMTP_STARTTLS", smtpStarttls);
        }
    }

    /**
     * Builder for {@link SesConfig}.
     */
    public static class Builder {

        private boolean enabled = DEFAULT_ENABLED;
        private String smtpHost;
        private int smtpPort = DEFAULT_SMTP_PORT;
        private String smtpUser;
        private String smtpPass;
        private String smtpStarttls = DEFAULT_SMTP_STARTTLS;

        private Builder() {
            // Allow instantiation only via SesConfig.builder()
        }

        /**
         * Enables or disables the SES service.
         *
         * @param enabled {@code true} to enable (default {@value DEFAULT_ENABLED})
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the SMTP server host for email relay. {@code null} disables relay (emails stored only).
         *
         * @param smtpHost the SMTP host, or {@code null} to disable relay
         * @return this builder
         */
        public Builder smtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
            return this;
        }

        /**
         * Sets the SMTP server port.
         *
         * @param smtpPort the SMTP port (default {@value DEFAULT_SMTP_PORT})
         * @return this builder
         */
        public Builder smtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
            return this;
        }

        /**
         * Sets the SMTP authentication username. {@code null} disables authentication.
         *
         * @param smtpUser the SMTP username, or {@code null}
         * @return this builder
         */
        public Builder smtpUser(String smtpUser) {
            this.smtpUser = smtpUser;
            return this;
        }

        /**
         * Sets the SMTP authentication password.
         *
         * @param smtpPass the SMTP password, or {@code null}
         * @return this builder
         */
        public Builder smtpPass(String smtpPass) {
            this.smtpPass = smtpPass;
            return this;
        }

        /**
         * Sets the STARTTLS mode: DISABLED, OPTIONAL, or REQUIRED.
         *
         * @param smtpStarttls the STARTTLS mode (default {@value DEFAULT_SMTP_STARTTLS})
         * @return this builder
         */
        public Builder smtpStarttls(String smtpStarttls) {
            this.smtpStarttls = smtpStarttls;
            return this;
        }

        /**
         * Creates an immutable {@link SesConfig} from this builder.
         *
         * @return the SES configuration
         */
        public SesConfig build() {
            return new SesConfig(this);
        }
    }
}
