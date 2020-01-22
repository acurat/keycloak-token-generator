package org.acurat.tokens.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultTokenExchangeService implements TokenExchangeService {

    private final RestTemplate restTemplate;

    public TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest,
                                  KeycloakProperties keycloakProperties,
                                  String impersonatorToken) {

        KeycloakTokenResponse response;
        try {
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(
                    buildBodyForTokenExchange(keycloakTokenRequest, impersonatorToken),
                    KeycloakUtil.getHttpHeaders());

            ResponseEntity<KeycloakTokenResponse> exchange = restTemplate.exchange(
                    keycloakProperties.getTokenUrl(),
                    HttpMethod.POST,
                    entity,
                    KeycloakTokenResponse.class);

            response = exchange.getBody();
            log.debug("Access token {}", response.getAccessToken());
            log.debug("Id token {}", response.getIdToken());

            return TokenResponse.builder().accessToken(response.getAccessToken())
                    .refreshToken(response.getRefreshToken())
                    .idToken(response.getIdToken())
                    .build();

        } catch (HttpClientErrorException hcee) {
            log.error(hcee.getLocalizedMessage());
            throw TokenGeneratorException.builder()
                    .message("Error generating token")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MultiValueMap<String, String> buildBodyForTokenExchange(KeycloakTokenRequest keycloakTokenRequest,
                                                                    String impersonatorToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("client_id", keycloakTokenRequest.getClientId());
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        map.add("subject_token", impersonatorToken);
        map.add("requested_subject", keycloakTokenRequest.getUsername());
        map.add("scope", keycloakTokenRequest.getScope());
        return map;
    }
}
