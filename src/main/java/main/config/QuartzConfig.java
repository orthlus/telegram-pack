package main.config;

import lombok.extern.slf4j.Slf4j;
import main.common.QuartzJobsList;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static main.common.QuartzUtils.scheduleJob;

@Slf4j
@Configuration
public class QuartzConfig {
	@Bean
	public Scheduler scheduler(SchedulerFactoryBean factory, List<QuartzJobsList> jobsClasses)
			throws SchedulerException {
		Scheduler scheduler = factory.getScheduler();

		Set<Tuple2<String, String>> outputData = new HashSet<>();

		for (QuartzJobsList jobsClass : jobsClasses) {
			for (Tuple2<JobDetail, Trigger> job : jobsClass.getJobs()) {
				scheduleJob(scheduler, job);
				outputData.add(new Tuple2<>(
						job.v1.getJobClass().getName().replaceAll("^main.", ""),
						getTriggerInfo(job.v2)
				));
			}
		}

		logJobsToStdout(outputData);

		scheduler.start();
		return scheduler;
	}

	private void logJobsToStdout(Set<Tuple2<String, String>> outputData) {
		int maxLength = outputData.stream()
				.map(s -> s.v1.length())
				.mapToInt(Integer::intValue)
				.max().orElse(50);

		// job - %-50s trigger - %s%n
		String outputPattern = "job - %-" + (maxLength + 1) + "s trigger - %s%n";
		for (Tuple2<String, String> job : outputData) {
			System.out.printf(outputPattern, job.v1, job.v2);
		}
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
