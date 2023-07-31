package main.regru;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.regru.common.RR;
import main.regru.common.RegRuResponseReader;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static main.common.OkHttpUtils.readBody;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegRuClient {
	private OkHttpClient client = new OkHttpClient();
	private final RegRuResponseReader responseReader;

	@Value("${regru.api.url}")
	private String baseUrl;
	@Value("${regru.account.user}")
	private String login;
	@Value("${regru.account.password}")
	private String password;

	private FormBody.Builder basicBody(String domainName) {
		return new FormBody.Builder()
				.add("username", login)
				.add("password", password)
				.add("domain_name", domainName);
	}

	public boolean deleteSubdomain(RR record, String domainName) {
		FormBody.Builder data = basicBody(domainName)
				.add("subdomain", record.domain())
				.add("content", record.ip())
				.add("record_type", "A");
		Request request = new Request.Builder()
				.post(data.build())
				.url(baseUrl + "/zone/remove_record")
				.build();
		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			if (response.code() == 200) {
				String text = readBody(response);
				return responseReader.isDomainAddingOrDeletingSuccess(text);
			} else {
				log.error("http error - 'deleteSubdomain' response code - {}, body - {}",
						response.code(), readBody(response));
				return false;
			}
		} catch (IOException e) {
			log.error("http error - 'deleteSubdomain'", e);
			return false;
		}
	}

	public boolean addSubdomain(RR record, String domainName) {
		FormBody.Builder data = basicBody(domainName)
				.add("subdomain", record.domain())
				.add("ipaddr", record.ip());
		Request request = new Request.Builder()
				.post(data.build())
				.url(baseUrl + "/zone/add_alias")
				.build();
		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			if (response.code() == 200) {
				String text = readBody(response);
				return responseReader.isDomainAddingOrDeletingSuccess(text);
			} else {
				log.error("http error - 'addSubdomain' response code - {}, body - {}",
						response.code(), readBody(response));
				return false;
			}
		} catch (IOException e) {
			log.error("http error - 'addSubdomain'", e);
			return false;
		}
	}

	public List<RR> getSubdomainsList(String domainName) {
		Request request = new Request.Builder()
				.post(basicBody(domainName).build())
				.url(baseUrl + "/zone/get_resource_records")
				.build();
		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			if (response.code() == 200) {
				String text = readBody(response);
				return responseReader.readDomainsList(text);
			} else {
				log.error("http error - 'get subdomains list' response code - {}, body - {}",
						response.code(), readBody(response));
				return List.of();
			}
		} catch (IOException e) {
			log.error("http error - 'get subdomains list'", e);
			return List.of();
		}
	}
}
