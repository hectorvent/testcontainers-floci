package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.Database;
import software.amazon.awssdk.services.glue.model.DatabaseInput;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerGlueServiceTest extends AbstractFlociContainerServiceTest {

    static GlueClient glue;

    @BeforeAll
    static void setUp() {
        glue = client(GlueClient.builder());
    }

    @Test
    void shouldCreateAndListDatabase() {
        String dbName = "test_db_" + System.currentTimeMillis();

        glue.createDatabase(b -> b.databaseInput(DatabaseInput.builder()
                .name(dbName)
                .build()));

        List<String> dbNames = glue.getDatabases(b -> {}).databaseList().stream()
                .map(Database::name)
                .toList();

        assertThat(dbNames).contains(dbName);
    }

    @Test
    void shouldGetDatabase() {
        String dbName = "test_get_" + System.currentTimeMillis();

        glue.createDatabase(b -> b.databaseInput(DatabaseInput.builder()
                .name(dbName)
                .build()));

        String name = glue.getDatabase(b -> b.name(dbName))
                .database()
                .name();

        assertThat(name).isEqualTo(dbName);
    }

}
