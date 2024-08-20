package main.payments_reminders;

import art.aelaort.telegram.entity.RemindToSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HourlyRemindJob {
	private final PaymentsTelegram telegram;
	private final Repo repo;

	@Scheduled(cron = "0 0 * * * *", zone = "Europe/Moscow")
	@Retryable(backoff = @Backoff(delay = 10_000, maxDelay = 120_000), maxAttempts = 20)
	public void remind() {
		List<RemindToSend> reminds = repo.getRemindsForNow();
		for (RemindToSend remind : reminds) {
			telegram.sendRemind(remind);
			log.info("send remind {}", remind);
		}
	}
}
