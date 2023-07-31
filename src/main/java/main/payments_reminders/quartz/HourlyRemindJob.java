package main.payments_reminders.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.common.QuartzJobsList;
import main.payments_reminders.entity.RemindToSend;
import main.payments_reminders.reminds.Repo;
import main.payments_reminders.telegram.PaymentsTelegram;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static main.common.QuartzUtils.buildJob;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class HourlyRemindJob implements Job, QuartzJobsList {
	private final PaymentsTelegram telegram;
	private final Repo repo;

	private void remind() {
		List<RemindToSend> reminds = repo.getRemindsForNow();
		for (RemindToSend remind : reminds) {
			telegram.sendRemind(remind);
			log.info("send remind {}", remind);
		}
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		remind();
	}

	@Override
	public List<Tuple2<JobDetail, Trigger>> getJobs() {
		return List.of(
				buildJob(HourlyRemindJob.class, "0 0 * * * ?")
		);
	}
}
