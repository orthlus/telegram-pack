package main.config;

import lombok.extern.slf4j.Slf4j;
import main.common.QuartzJobsList;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;

import static main.common.QuartzUtils.scheduleJob;

@Slf4j
@Configuration
public class QuartzConfig {
	@Bean
	public Scheduler scheduler(SchedulerFactoryBean factory, List<QuartzJobsList> jobsClasses)
			throws SchedulerException {
		Scheduler scheduler = factory.getScheduler();

		for (QuartzJobsList jobsClass : jobsClasses) {
			for (Tuple2<JobDetail, Trigger> job : jobsClass.getJobs()) {
				scheduleJob(scheduler, job);
				System.out.printf("job - %s, trigger - %s%n", job.v1.getJobClass().getName(), getTriggerInfo(job.v2));
			}
		}

		scheduler.start();
		return scheduler;
	}

	private String getTriggerInfo(Trigger trigger) {
		if (trigger instanceof CronTrigger)
			return ((CronTrigger) trigger).getCronExpression();
		else {
			long intervalMS = ((SimpleTrigger) trigger).getRepeatInterval();
			return "potentially repeat each %s (%d milliseconds)".formatted(roundMS(intervalMS), intervalMS);
		}
	}

	private String roundMS(long intervalMS) {
		if (intervalMS < 3600000)
			return intervalMS/60000 + " minutes";
		else
			return intervalMS/3600000 + " hours";
	}
}
