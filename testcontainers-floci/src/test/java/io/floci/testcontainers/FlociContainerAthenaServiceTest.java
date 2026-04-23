package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.QueryExecutionState;
import software.amazon.awssdk.services.athena.model.WorkGroupSummary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerAthenaServiceTest extends AbstractFlociContainerServiceTest {

    static AthenaClient athena;

    @BeforeAll
    static void setUp() {
        athena = client(AthenaClient.builder());
    }

    @Test
    void shouldStartAndGetQueryExecution() {
        String queryId = athena.startQueryExecution(b -> b
                .queryString("SELECT count(*) FROM my_table")
                .queryExecutionContext(ctx -> ctx.database("my_db"))).queryExecutionId();

        assertThat(queryId).isNotBlank();

        QueryExecutionState state = athena.getQueryExecution(b -> b.queryExecutionId(queryId))
                .queryExecution()
                .status()
                .state();

        assertThat(state).isEqualTo(QueryExecutionState.SUCCEEDED);
    }

    @Test
    void shouldGetQueryResults() {
        String queryId = athena.startQueryExecution(b -> b
                .queryString("SELECT 1")).queryExecutionId();

        var results = athena.getQueryResults(b -> b.queryExecutionId(queryId));

        assertThat(results.resultSet()).isNotNull();
    }

    @Test
    void shouldListQueryExecutions() {
        String queryId = athena.startQueryExecution(b -> b
                .queryString("SELECT 1")).queryExecutionId();

        List<String> executionIds = athena.listQueryExecutions(b -> {}).queryExecutionIds();

        assertThat(executionIds).contains(queryId);
    }
}
