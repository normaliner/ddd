package microarch.delivery.config;

import microarch.delivery.adapters.in.jobs.AssignOrdersJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail assignOrdersJobDetail() {
        return JobBuilder.newJob(AssignOrdersJob.class).withIdentity("assignOrdersJob").storeDurably().build();
    }

    @Bean
    public Trigger assignOrdersTrigger(JobDetail assignOrdersJobDetail) {
        return TriggerBuilder.newTrigger().forJob(assignOrdersJobDetail).withIdentity("assignOrdersTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).repeatForever()).build();
    }

}
