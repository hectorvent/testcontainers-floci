package io.floci.testcontainers.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import static io.floci.testcontainers.testing.ContainerUtils.genericContainer;
import static org.assertj.core.api.Assertions.assertThat;

class SesConfigTest {

    @Test
    void shouldApplyDefaultSesConfig() {
        SesConfig config = SesConfig.builder().build();
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getSmtpHost()).isNull();
        assertThat(config.getSmtpPort()).isEqualTo(25);
        assertThat(config.getSmtpUser()).isNull();
        assertThat(config.getSmtpPass()).isNull();
        assertThat(config.getSmtpStarttls()).isEqualTo("DISABLED");
    }

    @Test
    void shouldApplyCustomSesConfig() {
        SesConfig config = SesConfig.builder()
                .enabled(false)
                .smtpHost("mail.example.com")
                .smtpPort(587)
                .smtpUser("user")
                .smtpPass("secret")
                .smtpStarttls("REQUIRED")
                .build();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getSmtpHost()).isEqualTo("mail.example.com");
        assertThat(config.getSmtpPort()).isEqualTo(587);
        assertThat(config.getSmtpUser()).isEqualTo("user");
        assertThat(config.getSmtpPass()).isEqualTo("secret");
        assertThat(config.getSmtpStarttls()).isEqualTo("REQUIRED");
    }

    @Test
    void shouldApplyDefaultEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SesConfig.builder().build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SES_ENABLED", "true")
                .containsEntry("FLOCI_SERVICES_SES_SMTP_PORT", "25")
                .containsEntry("FLOCI_SERVICES_SES_SMTP_STARTTLS", "DISABLED")
                .doesNotContainKey("FLOCI_SERVICES_SES_SMTP_HOST")
                .doesNotContainKey("FLOCI_SERVICES_SES_SMTP_USER")
                .doesNotContainKey("FLOCI_SERVICES_SES_SMTP_PASS");
    }

    @Test
    void shouldApplyCustomEnvVarsToContainer() {
        GenericContainer<?> container = genericContainer();
        SesConfig.builder()
                .smtpHost("mail.example.com")
                .smtpPort(587)
                .smtpUser("user")
                .smtpPass("secret")
                .smtpStarttls("REQUIRED")
                .build()
                .applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap())
                .containsEntry("FLOCI_SERVICES_SES_SMTP_HOST", "mail.example.com")
                .containsEntry("FLOCI_SERVICES_SES_SMTP_PORT", "587")
                .containsEntry("FLOCI_SERVICES_SES_SMTP_USER", "user")
                .containsEntry("FLOCI_SERVICES_SES_SMTP_PASS", "secret")
                .containsEntry("FLOCI_SERVICES_SES_SMTP_STARTTLS", "REQUIRED");
    }

    @Test
    void shouldApplyDisabledEnvVarToContainer() {
        GenericContainer<?> container = genericContainer();
        SesConfig.builder().enabled(false).build().applyEnvVarsToContainer(container);

        assertThat(container.getEnvMap()).containsEntry("FLOCI_SERVICES_SES_ENABLED", "false");
    }
}
