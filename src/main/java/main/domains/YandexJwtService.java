package main.domains;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;

import static java.security.KeyFactory.getInstance;
import static java.util.Date.from;

@Slf4j
@Service
public class YandexJwtService implements InitializingBean {
	@Value("${yandex.iam.api.url}")
	private String jwtAudience;
	@Value("${yandex.dns.jwt.private-key-file}")
	private String jwtPrivateKeyFile;

	private PemObject privatePemKey;

	@Override
	public void afterPropertiesSet() throws Exception {
		privatePemKey = readKeyToPem();
	}

	private PemObject readKeyToPem() throws Exception {
		try (PemReader reader = new PemReader(new FileReader(jwtPrivateKeyFile))) {
			return reader.readPemObject();
		}
	}

	public String getToken(String serviceAccountId, String keyId) {
		try {
			return getToken0(serviceAccountId, keyId);
		} catch (Exception e) {
			log.error("yandex jwt error", e);
			throw new RuntimeException(e);
		}
	}

	private String getToken0(String serviceAccountId, String keyId) throws Exception {
		PrivateKey privateKey = getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(privatePemKey.getContent()));

		Instant now = Instant.now();

		return Jwts.builder()
				.header()
					.add("kid", keyId)
					.and()
				.issuer(serviceAccountId)
				.audience()
					.add(jwtAudience + "/tokens")
					.and()
				.issuedAt(from(now))
				.expiration(from(now.plusSeconds(3600)))
				.signWith(privateKey, Jwts.SIG.PS256)
				.compact();
	}
}
