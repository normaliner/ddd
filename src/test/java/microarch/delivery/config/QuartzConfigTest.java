package microarch.delivery.config;

import static org.assertj.core.api.Assertions.assertThat;

import microarch.delivery.adapters.in.jobs.AssignOrdersJob;
import org.junit.jupiter.api.Test;
import org.quartz.SimpleTrigger;

class QuartzConfigTest {

    private final QuartzConfig config = new QuartzConfig();

    @Test
    void assignOrdersJobDetail_ShouldUseAssignOrdersJob() {
        // Act
        var jobDetail = config.assignOrdersJobDetail();

        // Assert
        assertThat(jobDetail.getJobClass()).isEqualTo(AssignOrdersJob.class);
        assertThat(jobDetail.getKey().getName()).isEqualTo("assignOrdersJob");
    }

    @Test
    void assignOrdersTrigger_ShouldRepeatEverySecondForever() {
        // Arrange
        var jobDetail = config.assignOrdersJobDetail();

        // Act
        var trigger = config.assignOrdersTrigger(jobDetail);

        // Assert
        assertThat(trigger.getJobKey()).isEqualTo(jobDetail.getKey());
        assertThat(trigger).isInstanceOf(SimpleTrigger.class);
        var simpleTrigger = (SimpleTrigger) trigger;
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(1000L);
        assertThat(simpleTrigger.getRepeatCount()).isEqualTo(SimpleTrigger.REPEAT_INDEFINITELY);
    }
}
