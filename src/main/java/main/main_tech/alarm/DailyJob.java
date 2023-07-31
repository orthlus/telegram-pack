package main.main_tech.alarm;

import lombok.RequiredArgsConstructor;
import main.main_tech.Telegram;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;

//@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class DailyJob implements Job {
	private final Telegram telegram;
	@Value("${main_tech.alarm.url}")
	private String alarmUrl;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		telegram.sendAlarm(alarmUrl);
	}
}
