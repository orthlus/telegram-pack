package art.aelaort.dto.callback.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SomeCallback(@JsonProperty("q") int typeId) implements CallbackData {
}
