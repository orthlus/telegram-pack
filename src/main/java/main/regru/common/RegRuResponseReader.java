package main.regru.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import main.regru.common.dto.AddAndDeleteDomainResponse;
import main.regru.common.dto.DomainsList;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RegRuResponseReader {
	private ObjectMapper mapper = new ObjectMapper();

	public boolean isDomainAddingOrDeletingSuccess(String jsonStr) {
		boolean result;

		try {
			AddAndDeleteDomainResponse response = mapper.readValue(jsonStr, AddAndDeleteDomainResponse.class);

			if (!response.isResultSuccess()) {
				log.error("error in isDomainAddingOrDeletingSuccess: {}", jsonStr);
				result = false;
			} else {
				result = true;
			}
		} catch (JsonProcessingException e) {
			log.error("error parsing isDomainAddingOrDeletingSuccess: {}", jsonStr);
			result = false;
		}

		return result;
	}

	public List<RR> readDomainsList(String jsonStr) {
		List<RR> result;

		try {
			DomainsList domainsList = mapper.readValue(jsonStr, DomainsList.class);

			if (!domainsList.isResultSuccess()) {
				log.error("error during getting list domains: {}", jsonStr);
				result = List.of();
			} else {
				result = domainsList.getList().stream()
						.filter(rrDto -> rrDto.rectype.equals("A"))
						.map(rrDto -> new RR(rrDto.content, rrDto.subname))
						.toList();
			}
		} catch (JsonProcessingException e) {
			log.error("error parsing list domains: {}", jsonStr);
			result = List.of();
		}

		return result;
	}
}
