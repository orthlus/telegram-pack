package main.main_tech.alarm;

import main.common.QuartzJobsList;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Jobs implements QuartzJobsList {
	@Override
	public List<Tuple2<JobDetail, Trigger>> getJobs() {
		return List.of(
//				buildJob(DailyJob.class, "30 59 10 ? * 2-6"),
//				buildJob(DailyCleanJob.class, "0 45 11 ? * 2-6")
		);
	}
}
