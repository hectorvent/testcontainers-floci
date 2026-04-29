package io.floci.testcontainers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.Datum;
import software.amazon.awssdk.services.athena.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.athena.model.QueryExecutionState;
import software.amazon.awssdk.services.athena.model.Row;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.Column;
import software.amazon.awssdk.services.glue.model.DatabaseInput;
import software.amazon.awssdk.services.glue.model.SerDeInfo;
import software.amazon.awssdk.services.glue.model.StorageDescriptor;
import software.amazon.awssdk.services.glue.model.TableInput;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerAthenaServiceTest extends AbstractFlociContainerServiceTest {

    static final String BUCKET_NAME = "athena-data-lake-" + System.currentTimeMillis();
    static final String DATABASE_NAME = "analytics_" + System.currentTimeMillis();
    static final String TABLE_NAME = "orders";

    static AthenaClient athena;
    static GlueClient glue;
    static S3Client s3;

    @BeforeAll
    static void setUp() {
        athena = client(AthenaClient.builder());
        glue = client(GlueClient.builder());
        s3 = client(S3Client.builder().forcePathStyle(true));
    }

    @Test
    @Order(1)
    void shouldUploadDataToS3() {
        s3.createBucket(b -> b.bucket(BUCKET_NAME));

        String jsonData = """
                {"id":1,"name":"alice","amount":10.50}
                {"id":2,"name":"bob","amount":20.00}
                {"id":3,"name":"charlie","amount":30.75}
                """;

        s3.putObject(
                b -> b.bucket(BUCKET_NAME).key("orders/data.json"),
                RequestBody.fromString(jsonData)
        );

        String body = s3.getObjectAsBytes(b -> b.bucket(BUCKET_NAME).key("orders/data.json")).asUtf8String();
        assertThat(body).contains("alice");
    }

    @Test
    @Order(2)
    void shouldCreateGlueDatabaseAndTable() {
        glue.createDatabase(b -> b.databaseInput(DatabaseInput.builder()
                .name(DATABASE_NAME)
                .build()));

        glue.createTable(b -> b
                .databaseName(DATABASE_NAME)
                .tableInput(TableInput.builder()
                        .name(TABLE_NAME)
                        .storageDescriptor(StorageDescriptor.builder()
                                .location("s3://" + BUCKET_NAME + "/orders/")
                                .inputFormat("org.apache.hadoop.mapred.TextInputFormat")
                                .outputFormat("org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat")
                                .serdeInfo(SerDeInfo.builder()
                                        .serializationLibrary("org.openx.data.jsonserde.JsonSerDe")
                                        .build())
                                .columns(
                                        Column.builder().name("id").type("int").build(),
                                        Column.builder().name("name").type("string").build(),
                                        Column.builder().name("amount").type("double").build()
                                )
                                .build())
                        .build()));

        var tables = glue.getTables(b -> b.databaseName(DATABASE_NAME)).tableList();
        assertThat(tables).hasSize(1);
        assertThat(tables.get(0).name()).isEqualTo(TABLE_NAME);
    }

    @Test
    @Order(3)
    void shouldQueryAllData() {
        String queryId = executeAndAwait("SELECT * FROM " + TABLE_NAME + " ORDER BY id");

        GetQueryResultsResponse results = athena.getQueryResults(b -> b.queryExecutionId(queryId));

        List<Row> rows = results.resultSet().rows();
        // Header row + 3 data rows
        assertThat(rows).hasSize(4);

        Row headerRow = rows.get(0);
        List<String> headers = headerRow.data().stream().map(Datum::varCharValue).toList();
        assertThat(headers).containsExactly("id", "name", "amount");

        assertThat(rows.get(1).data().stream().map(Datum::varCharValue).toList())
                .containsExactly("1", "alice", "10.5");
        assertThat(rows.get(2).data().stream().map(Datum::varCharValue).toList())
                .containsExactly("2", "bob", "20.0");
        assertThat(rows.get(3).data().stream().map(Datum::varCharValue).toList())
                .containsExactly("3", "charlie", "30.75");
    }

    @Test
    @Order(4)
    void shouldQueryWithAggregation() {
        String queryId = executeAndAwait("SELECT SUM(amount) AS total FROM " + TABLE_NAME);

        GetQueryResultsResponse results = athena.getQueryResults(b -> b.queryExecutionId(queryId));

        List<Row> rows = results.resultSet().rows();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).data().get(0).varCharValue()).isEqualTo("total");
        assertThat(Double.parseDouble(rows.get(1).data().get(0).varCharValue())).isEqualTo(61.25);
    }

    @Test
    @Order(5)
    void shouldQueryWithFilter() {
        String queryId = executeAndAwait(
                "SELECT name, amount FROM " + TABLE_NAME + " WHERE amount > 15 ORDER BY id");

        GetQueryResultsResponse results = athena.getQueryResults(b -> b.queryExecutionId(queryId));

        List<Row> rows = results.resultSet().rows();
        // Header + 2 matching rows
        assertThat(rows).hasSize(3);

        assertThat(rows.get(1).data().stream().map(Datum::varCharValue).toList())
                .containsExactly("bob", "20.0");
        assertThat(rows.get(2).data().stream().map(Datum::varCharValue).toList())
                .containsExactly("charlie", "30.75");
    }

    @Test
    @Order(6)
    void shouldListQueryExecutions() {
        String queryId = executeAndAwait("SELECT COUNT(*) AS cnt FROM " + TABLE_NAME);

        List<String> executionIds = athena.listQueryExecutions(b -> {}).queryExecutionIds();

        assertThat(executionIds).contains(queryId);
    }

    private String executeAndAwait(String sql) {
        String queryId = athena.startQueryExecution(b -> b
                .queryString(sql)
                .queryExecutionContext(ctx -> ctx.database(DATABASE_NAME))).queryExecutionId();

        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    QueryExecutionState state = athena.getQueryExecution(b -> b.queryExecutionId(queryId))
                            .queryExecution()
                            .status()
                            .state();
                    assertThat(state).isNotIn(QueryExecutionState.QUEUED, QueryExecutionState.RUNNING);
                });

        QueryExecutionState finalState = athena.getQueryExecution(b -> b.queryExecutionId(queryId))
                .queryExecution()
                .status()
                .state();
        assertThat(finalState).isEqualTo(QueryExecutionState.SUCCEEDED);

        return queryId;
    }
}
