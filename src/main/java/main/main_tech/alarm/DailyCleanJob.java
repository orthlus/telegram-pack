package main.main_tech.alarm;

import lombok.RequiredArgsConstructor;
import main.main_tech.Telegram;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class DailyCleanJob implements Job {
	private final Telegram telegram;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		telegram.deleteLastAlarmMessage();
	}
}
