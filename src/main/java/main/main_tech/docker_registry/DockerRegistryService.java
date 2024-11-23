package main.main_tech.docker_registry;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DockerRegistryService {
	private final RestTemplate dockerRegistryRestTemplate;
	private final RestTemplate iamRestTemplate;
	@Value("${main_tech.registry.id}")
	private String id;

	public int cleanNotLatest() {
		Images images = getImages();
		List<String> ids = getNotLatestIds(images);
		for (String id : ids) {
			delete(id);
		}
		return ids.size();
	}

	private void delete(String id) {
		dockerRegistryRestTemplate.exchange(
				"/images/" + id,
				HttpMethod.DELETE,
				entityBearerToken(getToken()),
				Void.class
		);
	}

	private Images getImages() {
		return dockerRegistryRestTemplate.exchange(
				"/images?registryId=%s&pageSize=1000".formatted(id),
				HttpMethod.GET,
				entityBearerToken(getToken()),
				Images.class
		).getBody();
	}

	private List<String> getNotLatestIds(Images images) {
		return images.getImages()
				.stream()
				.filter(image -> image.getTags() != null)
				.filter(image -> !image.getTags().isEmpty())
				.filter(image -> !image.getTags().get(0).equals("latest"))
				.map(Images.Image::getId)
				.toList();
	}

	private String getToken() {
		return iamRestTemplate.getForObject("/token", String.class);
	}

	private HttpEntity<?> entityBearerToken(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(headers);
	}
}
