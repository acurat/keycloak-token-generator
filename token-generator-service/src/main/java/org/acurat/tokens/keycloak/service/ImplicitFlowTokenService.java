package org.acurat.tokens.keycloak.service;

import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ImplicitFlowTokenService extends OAuth2FlowService implements TokenService {

    private final RestTemplate restTemplate;

    public ImplicitFlowTokenService(RestTemplate restTemplate) {
        super(restTemplate);
        this.restTemplate = restTemplate;
    }

    public TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest,
                                  KeycloakProperties keycloakProperties,
                                  ClientRepresentation clientRepresentation,
                                  String impersonatorToken) {

        try {

            super.impersonate(keycloakProperties, keycloakTokenRequest.getUsername(), impersonatorToken);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(keycloakProperties.getAuthUrl())
                    .queryParams(buildQueryParamsForAuthCall(clientRepresentation, keycloakTokenRequest.getScope()));
            ResponseEntity<Object> implicitFlow = restTemplate.exchange(builder.build(true).toUri(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY, Object.class);

            return UrlUtil.getTokensFromLocationHeader(implicitFlow.getHeaders().getFirst("Location"));

        } catch (HttpClientErrorException hcee) {
            log.error(hcee.getLocalizedMessage());
            throw TokenGeneratorException.builder()
                    .message("Error fetching token in implicit flow")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MultiValueMap<String, String> buildQueryParamsForAuthCall(ClientRepresentation clientRepresentation,
                                                                      String scope) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("response_type", URLEncoder.encode("token id_token", StandardCharsets.UTF_8));
        map.add("response_mode", "fragment");
        map.add("client_id", clientRepresentation.getClientId());
        map.add("redirect_uri", URLEncoder.encode(clientRepresentation.getRedirectUris().get(0), StandardCharsets.UTF_8));
        map.add("nonce", "nonce");
        map.add("scope", URLEncoder.encode(scope, StandardCharsets.UTF_8));
        return map;
    }
}
