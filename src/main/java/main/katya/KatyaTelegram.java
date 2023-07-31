package main.katya;

import lombok.extern.slf4j.Slf4j;
import main.common.telegram.CustomSpringWebhookBot;
import main.katya.exception.InstagramUnauthorizedException;
import main.katya.exception.InvalidUrl;
import main.katya.exception.UnknownHost;
import main.katya.ig.http.PaidClient;
import org.jooq.lambda.function.Consumer2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static main.katya.KnownHosts.YOUTUBE;

@Slf4j
@Component
public class KatyaTelegram extends CustomSpringWebhookBot {
	public KatyaTelegram(KatyaBotConfig botConfig,
						 PaidClient instagram,
						 SocialApiClient socialApiClient) {
		super(botConfig);
		this.instagram = instagram;
		this.socialApiClient = socialApiClient;
	}

	private final PaidClient instagram;
	private final SocialApiClient socialApiClient;

	@Value("${katya.my.private.chat.id}")
	private long myPrivateChatId;

	@Override
	public void onWebhookUpdate(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			long chatId = update.getMessage().getChat().getId();
			long userId = update.getMessage().getFrom().getId();

			try {
				if (chatId == userId) {
					privateChat(update);
				} else if (chatId == myPrivateChatId) {
					myPrivateChat(update);
				} else {
					groupChat(update);
				}
			} catch (InstagramUnauthorizedException e) {
				sendByUpdate("не получилась авторизация до инстаграма, сходите к @aelaort", update);
			}
		}
	}

	private void myPrivateChat(Update update) {
		String inputText = update.getMessage().getText();
		try {
			if (isItHost(getURL(parseUrlWithSign(inputText)), YOUTUBE)
					&& !inputText.startsWith("!"))
				return;

			groupChat(update);
		} catch (InvalidUrl | UnknownHost ignored) {
		}
	}

	private void groupChat(Update update) {
		String inputText = update.getMessage().getText();
		try {
			URI uri = getURL(parseUrlWithSign(inputText));
			logMessageIfHasUrl(update);
			functionByHost(uri).accept(uri, update);
		} catch (InvalidUrl | UnknownHost ignored) {
		}
	}

	private void privateChat(Update update) {
		String inputText = update.getMessage().getText();
		if (inputText.equals("/start")) {
			sendByUpdate("Привет! Скачаю медиа по ссылке", update);
			return;
		}
		try {
			URI uri = getURL(inputText);
			logMessageIfHasUrl(update);
			functionByHost(uri).accept(uri, update);
		} catch (InvalidUrl e) {
			sendByUpdate("Какая-то неправильная у вас ссылка :(", update);
		} catch (UnknownHost e) {
			sendByUpdate("Неизвестный хост", update);
		}
	}

	private void tiktokUrl(URI uri, Update update) {
		try {
			InputStream file = socialApiClient.getTikTokFile(uri);
			sendVideoByUpdate(update, "", file);
			deleteMessage(update);
		} catch (Exception e) {
			log.error("error handle tiktok - {}", uri, e);
		}
	}

	private void youtubeUrl(URI uri, Update update) {
		try {
			InputStream file = socialApiClient.getYouTubeFile(uri);
			sendVideoByUpdate(update, "", file);
			deleteMessage(update);
		} catch (Exception e) {
			log.error("error handling youtube url - {}", uri, e);
		}
	}

	private void instagramUrl(URI uri, Update update) {
		Optional<String> mediaUrlOp = instagram.getMediaUrl(uri);
		if (mediaUrlOp.isPresent()) {
			log.debug("for input url {} got media url {}", uri, mediaUrlOp.get());
			try {
				sendVideoByUpdate(update, "", mediaUrlOp.get());
				deleteMessage(update);
			} catch (Exception e) {
				log.error("error handling instagram url", e);
			}
		}
	}

	private Consumer2<URI, Update> functionByHost(URI uri) {
		return switch (parseHost(uri)) {
			case INSTAGRAM -> this::instagramUrl;
			case TIKTOK -> this::tiktokUrl;
			case YOUTUBE -> this::youtubeUrl;
		};
	}

	private boolean isItHost(URI uri, KnownHosts host) {
		return parseHost(uri).equals(host);
	}

	private String parseUrlWithSign(String text) {
		return text.startsWith("!") ? text.substring(1) : text;
	}

	private KnownHosts parseHost(URI uri) {
		String cleanedUrl = uri.getHost().replace("www.", "");
		for (KnownHosts knownHost : KnownHosts.values()) {
			for (String host : knownHost.hosts) {
				if (cleanedUrl.contains(host))
					return knownHost;
			}
		}
		throw new UnknownHost();
	}

	private void logMessageIfHasUrl(Update update) {
		try {
			getURL(update.getMessage().getText());
			long chatId = update.getMessage().getChat().getId();
			long userId = update.getMessage().getFrom().getId();
			log.info("new message {} in chat {} from {}", update.getMessage().getText(), chatId, userId);
		} catch (InvalidUrl | UnknownHost ignored) {

		}
	}

	private URI getURL(String url) throws InvalidUrl {
		try {
			return new URL(url).toURI();
		} catch (URISyntaxException | MalformedURLException e) {
			throw new InvalidUrl();
		}
	}
}
