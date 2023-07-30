package main.common;

import org.jooq.lambda.tuple.Tuple2;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.util.List;

public interface QuartzJobsList {
	List<Tuple2<JobDetail, Trigger>> getJobs();
}
