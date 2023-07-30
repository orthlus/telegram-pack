package main.common;

import main.Main;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;

import java.util.TimeZone;

public class QuartzUtils {
	public static void scheduleJob(Scheduler scheduler, Tuple2<JobDetail, Trigger> tuple) throws SchedulerException {
		if (scheduler.checkExists(tuple.v1.getKey())) {
			scheduler.scheduleJob(tuple.v2);
		} else {
			scheduler.scheduleJob(tuple.v1, tuple.v2);
		}
	}

	public static Tuple2<JobDetail, Trigger> buildJob(Class <? extends Job> jobClazz,
												SimpleScheduleBuilder simpleScheduleBuilder) {
		TriggerKey key = getKey(jobClazz);

		JobDetail jobDetail = buildJobDetail(jobClazz);
		SimpleTrigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(simpleScheduleBuilder)
				.withIdentity(key)
				.forJob(jobDetail)
				.build();

		return Tuple.tuple(jobDetail, trigger);
	}

	/**
	 * @see CronExpression
	 */
	public static Tuple2<JobDetail, Trigger> buildJob(Class <? extends Job> jobClazz, String cronExpression) {
		TriggerKey key = getKey(jobClazz);

		JobDetail jobDetail = buildJobDetail(jobClazz);
		CronTrigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder
						.cronSchedule(cronExpression)
						.inTimeZone(TimeZone.getTimeZone(Main.zone)))
				.withIdentity(key)
				.forJob(jobDetail)
				.build();

		return Tuple.tuple(jobDetail, trigger);
	}

	private static JobDetail buildJobDetail(Class<? extends Job> jobClazz) {
		return JobBuilder.newJob()
				.storeDurably()
				.withIdentity(jobClazz.getName())
				.ofType(jobClazz)
				.build();
	}

	private static TriggerKey getKey(Class <? extends Job> jobClazz) {
		return TriggerKey.triggerKey(jobClazz.getName());
	}
}
