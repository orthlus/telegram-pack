package main.domains;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class YandexDNSClients {
	@Bean
	public YandexDNSClient dnsClient2(
			@Value("${domains.2.domain.name}") String domain,
			@Value("${domains.2.yandex.dns.zone.id}") String dnsZoneId,
			@Qualifier("yandexDNSWebClient") WebClient client,
			YandexApiTokenService tokenService,
			Repo repo,
			YandexDtoMapper mapper
	) {
		return newYandexDNSClient(client, tokenService, repo, mapper, domain, dnsZoneId);
	}

	private YandexDNSClient newYandexDNSClient(WebClient client,
											   YandexApiTokenService tokenService,
											   Repo repo,
											   YandexDtoMapper mapper,
											   String domain,
											   String dnsZoneId) {
		return new YandexDNSClient(client, tokenService, repo, mapper) {
			@Override
			public String getDomainName() {
				return domain;
			}

			@Override
			public String getDNSZoneId() {
				return dnsZoneId;
			}
		};
	}
}
