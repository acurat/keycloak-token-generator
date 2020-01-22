package org.acurat.tokens.keycloak.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class KeycloakUtil {

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
        return headers;
    }

    public static KeycloakProperties getEnvironmentProperties(String environment, Map<String,
            KeycloakProperties> keycloakPropertiesMap) {
        if (environment == null || !keycloakPropertiesMap.containsKey(environment)) {
            String message = environment == null ? "Environment cannot be null" : "Environment not found";
            throw TokenGeneratorException.builder()
                    .message(message)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        return keycloakPropertiesMap.get(environment);
    }

    public static List<String> getEnvironments(Map<String, KeycloakProperties> keycloakPropertiesMap) {
        if (keycloakPropertiesMap == null) {
            throw TokenGeneratorException.builder()
                    .message("No environments configured")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
        return keycloakPropertiesMap.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
