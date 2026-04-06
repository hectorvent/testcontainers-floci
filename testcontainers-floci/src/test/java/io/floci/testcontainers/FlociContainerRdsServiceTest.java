package io.floci.testcontainers;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlociContainerRdsServiceTest extends AbstractFlociContainerServiceTest {

    private static final String DB_NAME = "testdb";
    private static final String MASTER_USER = "admin";
    private static final String MASTER_PASSWORD = "password123";

    static RdsClient rds;

    static String dbId;

    @BeforeAll
    static void setUp() {
        dbId = "test-pg-" + System.currentTimeMillis();
        rds = client(RdsClient.builder());
    }

    @Test
    @Order(1)
    void shouldDescribeDbInstances() {
        List<DBInstance> instances = rds.describeDBInstances().dbInstances();

        assertThat(instances).isEmpty();
    }

    @Test
    @Order(2)
    void shouldDescribeDbClusters() {
        var clusters = rds.describeDBClusters().dbClusters();

        assertThat(clusters).isNotNull();
    }

    @Test
    @Order(3)
    void shouldCreateDbInstance() {
        rds.createDBInstance(b -> b
                .dbInstanceIdentifier(dbId)
                .dbName(DB_NAME)
                .dbInstanceClass("db.t3.micro")
                .engine("postgres")
                .masterUsername(MASTER_USER)
                .masterUserPassword(MASTER_PASSWORD));

        rds.waiter().waitUntilDBInstanceAvailable(b -> b.dbInstanceIdentifier(dbId),
                c -> c.waitTimeout(Duration.ofSeconds(30)));

        DBInstance instance = rds.describeDBInstances(b -> b.dbInstanceIdentifier(dbId)).dbInstances().get(0);
        assertThat(instance.endpoint()).isNotNull();
        assertThat(instance.engine()).isEqualTo("postgres");
    }

    @Test
    @Order(4)
    void shouldConnectViaJdbc() {
        int pgProxyPort = floci.getMappedPort(floci.getRdsConfig().getProxyBasePort());
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=disable", floci.getHost(), pgProxyPort, DB_NAME);

        for (int attempt = 1; ; attempt++) {
            try (Connection conn = DriverManager.getConnection(jdbcUrl, MASTER_USER, MASTER_PASSWORD);
                 Statement stmt = conn.createStatement()) {

                stmt.execute("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(100))");
                stmt.execute("INSERT INTO test_table (name) VALUES ('hello from floci')");

                try (ResultSet rs = stmt.executeQuery("SELECT name FROM test_table")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("name")).isEqualTo("hello from floci");
                }
                break;
            } catch (Exception e) {
                if (attempt >= 10) {
                    throw new RuntimeException("Failed to connect to RDS PostgreSQL instance via JDBC after 10 attempts", e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    @Order(5)
    void shouldDeleteDbInstance() {
        rds.deleteDBInstance(b -> b.dbInstanceIdentifier(dbId));
        assertThat(rds.describeDBInstances().dbInstances()).isEmpty();
    }
}
