package main.main_tech;

import lombok.RequiredArgsConstructor;
import main.common.QuartzJobsList;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static main.common.QuartzUtils.buildJob;

@Component
@RequiredArgsConstructor
public class Jobs implements QuartzJobsList {
	private final Telegram telegram;

	@Override
	public List<Tuple2<JobDetail, Trigger>> getJobs() {
		return List.of(
//				buildJob(DailyJob1.class, "30 29 11 ? * 2-6"),
//				buildJob(DailyCleanJob1.class, "0 35 11 ? * 2-6"),
//				buildJob(DailyJob2.class, "30 29 12 ? * 2-6"),
//				buildJob(DailyCleanJob2.class, "0 35 12 ? * 2-6")
		);
	}

	@Component
	@DisallowConcurrentExecution
	public class DailyJob1 implements Job {
		@Override
		public void execute(JobExecutionContext context) {
			telegram.sendAlarm1("Видеочат (в маленьком чате)");
		}
	}

	@Component
	@DisallowConcurrentExecution
	public class DailyCleanJob1 implements Job {
		@Override
		public void execute(JobExecutionContext context) {
			telegram.deleteLastAlarmMessage1();
		}
	}

	@Component
	@DisallowConcurrentExecution
	public class DailyJob2 implements Job {
		@Override
		public void execute(JobExecutionContext context) {
			telegram.sendAlarm2("Голосовой чат (ссылка в общем чате)");
		}
	}

	@Component
	@DisallowConcurrentExecution
	public class DailyCleanJob2 implements Job {
		@Override
		public void execute(JobExecutionContext context) {
			telegram.deleteLastAlarmMessage2();
		}
	}
}
