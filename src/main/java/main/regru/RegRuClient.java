package main.regru;

import feign.Feign;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.regru.common.Add;
import main.regru.common.Basic;
import main.regru.common.Delete;
import main.regru.common.RR;
import main.regru.common.dto.DomainsList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegRuClient {
	private RegRuHttp client;
	private final RRMapper mapper;

	@Value("${regru.api.url}")
	private String baseUrl;
	@Value("${regru.account.user}")
	private String login;
	@Value("${regru.account.password}")
	private String password;

	@PostConstruct
	private void init() {
		client = Feign.builder()
				.encoder(new FormEncoder())
				.decoder(new JacksonDecoder())
				.target(RegRuHttp.class, baseUrl);
	}

	private Basic basic(String domainName) {
		return new Basic(login, password, domainName);
	}

	public boolean deleteSubdomain(RR record, String domainName) {
		try {
			Delete delete = mapper.delete(record, basic(domainName));
			return client.deleteSubdomain(delete).isResultSuccess();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean addSubdomain(RR record, String domainName) {
		try {
			Add add = mapper.add(record, basic(domainName));
			return client.addSubdomain(add).isResultSuccess();
		} catch (Exception e) {
			return false;
		}
	}

	public List<RR> getSubdomainsList(String domainName) {
		DomainsList domainsList = client.subdomains(basic(domainName));
		return extractList(domainsList);
	}

	private List<RR> extractList(DomainsList domainsList) {
		if (domainsList.isResultSuccess()) {
			return domainsList.getList()
					.stream()
					.filter(rrDto -> rrDto.rectype.equals("A"))
					.map(rrDto -> new RR(rrDto.content, rrDto.subname))
					.toList();
		}

		return List.of();
	}
}
