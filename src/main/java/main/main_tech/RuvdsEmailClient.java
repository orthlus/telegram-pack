package main.main_tech;

import com.google.common.collect.Sets;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static main.Main.zone;

@Slf4j
@Component
public class RuvdsEmailClient {
	@Value("${main_tech.ruvds.email.host}")
	private String imapHost;
	@Value("${main_tech.ruvds.email.port}")
	private int port;
	@Value("${main_tech.ruvds.email.address}")
	private String user;
	@Value("${main_tech.ruvds.email.password}")
	private String password;

	private Store buildStore() {
		try {
			MailSSLSocketFactory sf = new MailSSLSocketFactory();
			sf.setTrustAllHosts(true);
			Properties properties = new Properties();
			properties.put("mail.imap.ssl.enable", "true");
			properties.put("mail.imap.ssl.trust", "*");
			properties.put("mail.imap.ssl.socketFactory", sf);

			Store store = Session.getDefaultInstance(properties).getStore("imap");
			store.connect(imapHost, port, user, password);
			return store;
		} catch (GeneralSecurityException | MessagingException e) {
			log.error("Error ruvds email client during building Store", e);
			throw new BuildEmailStoreException();
		}
	}

	public String getNewHost() {
		try {
			return getNewHost(getLastNewRuvdsMessage(buildStore()));
		} catch (MessagingException | IOException e) {
			log.error("Error getting ruvds new host", e);
			return "Ошибка получения кредов хоста";
		} catch (NoNewMessagesException e) {
			return "Нет новых сообщений";
		}
	}

	private String getNewHost(Message message) throws MessagingException, IOException {
		String body = message.getContent().toString();
		if (!body.contains("Пароль к серверу:")) throw new NoNewMessagesException();

		String ipPattern1 = "IP-адрес:</td><td style=\"padding-left:10px;\"><div><b>";
		String ipPattern2 = "</b></div></td></tr>";

		String ip = body.split(ipPattern1)[1].split(ipPattern2)[0];

		String passwordPattern1 = "Пароль к серверу:</td><td style=\"padding-left:10px;\"><div><b>";
		String passwordPattern2 = "</b></div></td></tr></tbody></table>";

		String password = body.split(passwordPattern1)[1].split(passwordPattern2)[0];

		return "%s %s".formatted(ip, password);
	}

	public String getCode() {
		try {
			return getCode(getLastNewRuvdsMessage(buildStore()));
		} catch (BuildEmailStoreException | MessagingException | IOException e) {
			log.error("Error getting ruvds code", e);
			return "Ошибка получения кода";
		} catch (NoNewMessagesException e) {
			return "Нет новых сообщений";
		}
	}

	private String getCode(Message message) throws MessagingException, IOException {
		String body = message.getContent().toString();
		if (!body.contains("используйте следующий код подтверждения")) throw new NoNewMessagesException();

		String pattern = "используйте следующий код подтверждения:</div><div style=\"" +
				"font-size:40px;text-align:center;line-height:normal;margin:20px 0px 20px 0px;\">";
		String pattern2 = "</div><div>Внимание!";

		return body.split(pattern)[1].split(pattern2)[0];
	}

	private Message getLastNewRuvdsMessage(Store store) throws MessagingException {
		return getUnreadMessagesFromInbox(store).stream()
				.filter(message -> {
					try {
						String from = ((InternetAddress) message.getFrom()[0]).getAddress();
						return from.equals("info@ruvds.com");
					} catch (MessagingException e) {
						throw new NoNewMessagesException(e);
					}
				})
				.max(Comparator.comparing((Message msg) -> {
					try {
						return msg.getSentDate();
					} catch (MessagingException e) {
						throw new NoNewMessagesException(e);
					}
				}))
				.orElseThrow(NoNewMessagesException::new);
	}

	private Set<Message> getUnreadMessagesFromInbox(Store store) throws MessagingException {
		Folder inbox = store.getFolder("INBOX");

		if (inbox.getUnreadMessageCount() == 0) throw new NoNewMessagesException();
		inbox.open(Folder.READ_ONLY);

		return getUnreadMessages(inbox);
	}

	private Set<Message> getUnreadMessages(Folder inbox) throws MessagingException {
		return Sets.newHashSet(inbox.search(
				new FlagTerm(new Flags(Flags.Flag.SEEN), false)
		));
	}

	private boolean isInLast5Minutes(Date date) {
		return date.toInstant()
				.atZone(zone).toLocalDateTime()
				.isAfter(now(zone).minusMinutes(5));
	}

	private static class NoNewMessagesException extends RuntimeException {
		public NoNewMessagesException() {
		}

		public NoNewMessagesException(Throwable cause) {
			super(cause);
		}
	}
	private static class BuildEmailStoreException extends RuntimeException {}
}
