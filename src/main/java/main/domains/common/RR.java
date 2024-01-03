package main.domains.common;

import lombok.With;

@With
public record RR(String ip, String domain) {
}
