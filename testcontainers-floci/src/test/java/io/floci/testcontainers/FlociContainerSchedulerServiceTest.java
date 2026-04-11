package io.floci.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindow;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindowMode;
import software.amazon.awssdk.services.scheduler.model.ScheduleGroupSummary;
import software.amazon.awssdk.services.scheduler.model.ScheduleSummary;
import software.amazon.awssdk.services.scheduler.model.Target;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlociContainerSchedulerServiceTest extends AbstractFlociContainerServiceTest {

    static SchedulerClient scheduler;

    @BeforeAll
    static void setUp() {
        scheduler = client(SchedulerClient.builder());
    }

    @Test
    void shouldCreateAndListSchedule() {
        String scheduleName = "test-schedule-" + System.currentTimeMillis();

        scheduler.createSchedule(b -> b
                .name(scheduleName)
                .scheduleExpression("rate(1 hour)")
                .flexibleTimeWindow(FlexibleTimeWindow.builder()
                        .mode(FlexibleTimeWindowMode.OFF)
                        .build())
                .target(Target.builder()
                        .arn("arn:aws:sqs:us-east-1:000000000000:test-queue")
                        .roleArn("arn:aws:iam::000000000000:role/test-role")
                        .build()));

        List<String> scheduleNames = scheduler.listSchedules(b -> {}).schedules().stream()
                .map(ScheduleSummary::name)
                .toList();

        assertThat(scheduleNames).contains(scheduleName);
    }

    @Test
    void shouldCreateAndListScheduleGroup() {
        String groupName = "test-group-" + System.currentTimeMillis();

        scheduler.createScheduleGroup(b -> b.name(groupName));

        List<String> groupNames = scheduler.listScheduleGroups(b -> {}).scheduleGroups().stream()
                .map(ScheduleGroupSummary::name)
                .toList();

        assertThat(groupNames).contains(groupName);
    }

}
