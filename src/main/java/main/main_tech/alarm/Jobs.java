package main.main_tech.alarm;

import main.common.QuartzJobsList;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;

import java.util.List;

import static main.common.QuartzUtils.buildJob;

@Component
public class Jobs implements QuartzJobsList {
	@Override
	public List<Tuple2<JobDetail, Trigger>> getJobs() {
		return List.of(
				buildJob(DailyJob.class, "0 29 11 ? * 2-6"),
				buildJob(DailyCleanJob.class, "0 35 11 ? * 2-6")
		);
	}
}
