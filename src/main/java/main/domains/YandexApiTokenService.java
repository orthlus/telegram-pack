package main.domains;

import lombok.RequiredArgsConstructor;
import main.Main;
import main.domains.common.dto.yandex.auth.IAMTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class YandexApiTokenService {
	@Qualifier("yandexIAMWebClient")
	private final WebClient client;
	private final YandexJwtService jwtService;
	@Value("${yandex.dns.service-account-id}")
	private String serviceAccountId;
	@Value("${yandex.dns.key-id}")
	private String keyId;

	private LocalDateTime tokenReleaseTime = LocalDateTime.now().minusDays(1);
	private String iamToken;

	public String getIAMToken() {
		if (isTokenNotFresh()) {
			updateIAMToken();
		}

		return iamToken;
	}

	private boolean isTokenNotFresh() {
		return tokenReleaseTime.plusMinutes(30).isBefore(LocalDateTime.now(Main.zone));
	}

	private void updateIAMToken() {
		String jwtToken = jwtToken();
		iamToken = iamToken(jwtToken);
		tokenReleaseTime = LocalDateTime.now(Main.zone);
	}

	private String iamToken(String jwtToken) {
		return client.post()
				.uri("/tokens")
				.headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
				.bodyValue("""
						{"jwt": "%s"}""".formatted(jwtToken))
				.retrieve()
				.bodyToMono(IAMTokenResponse.class)
				.blockOptional()
				.orElseThrow(() -> new RuntimeException("empty iam token"))
				.getIamToken();
	}

	private String jwtToken() {
		return jwtService.getToken(serviceAccountId, keyId);
	}
}
