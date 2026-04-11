package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StateMachineListItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerStepFunctionsServiceTest extends AbstractFlociContainerServiceTest {

    static SfnClient sfn;

    @BeforeAll
    static void setUp() {
        sfn = client(SfnClient.builder());
    }

    @Test
    void shouldCreateAndListStateMachine() {
        String name = "test-sm-" + System.currentTimeMillis();
        String definition = """
                {
                  "Comment": "Hello World",
                  "StartAt": "Pass",
                  "States": {
                    "Pass": {
                      "Type": "Pass",
                      "End": true
                    }
                  }
                }
                """;

        // IAM role ARN can be a placeholder in the emulator
        String roleArn = "arn:aws:iam::000000000000:role/test-role";

        String stateMachineArn = sfn.createStateMachine(b -> b
                        .name(name)
                        .definition(definition)
                        .roleArn(roleArn))
                .stateMachineArn();

        assertThat(stateMachineArn).contains(name);

        List<String> names = sfn.listStateMachines().stateMachines().stream()
                .map(StateMachineListItem::name)
                .toList();

        assertThat(names).contains(name);
    }

}
