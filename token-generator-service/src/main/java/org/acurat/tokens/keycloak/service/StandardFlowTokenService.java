package org.acurat.tokens.keycloak.service;

import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class StandardFlowTokenService extends OAuth2FlowService implements TokenService {

    private final RestTemplate restTemplate;

    public StandardFlowTokenService(RestTemplate restTemplate) {
        super(restTemplate);
        this.restTemplate = restTemplate;
    }

    @Override
    public TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest,
                                  KeycloakProperties keycloakProperties,
                                  ClientRepresentation clientRepresentation,
                                  String impersonatorToken) {

        try {

            super.impersonate(keycloakProperties, keycloakTokenRequest.getUsername(), impersonatorToken);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(keycloakProperties.getAuthUrl())
                    .queryParams(buildQueryParamsForAuthCall(clientRepresentation, keycloakTokenRequest.getScope()));
            ResponseEntity<Object> standardCodeFlow = restTemplate.exchange(builder.build(true).toUri(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY, Object.class);

            String code = UrlUtil.getCodeFromLocationHeader(standardCodeFlow.getHeaders().getFirst("Location"));
            ;

            HttpEntity<MultiValueMap<String, String>> codeToTokenEntity = new HttpEntity<>(
                    buildBodyForCodeExchange(keycloakTokenRequest, clientRepresentation, code),
                    KeycloakUtil.getHttpHeaders());

            KeycloakTokenResponse codeToTokenFlow = restTemplate.exchange(
                    keycloakProperties.getTokenUrl(),
                    HttpMethod.POST,
                    codeToTokenEntity,
                    KeycloakTokenResponse.class).getBody();

            log.debug("Access token {}", codeToTokenFlow.getAccessToken());
            log.debug("Id token {}", codeToTokenFlow.getIdToken());

            return TokenResponse.builder().accessToken(codeToTokenFlow.getAccessToken())
                    .refreshToken(codeToTokenFlow.getRefreshToken())
                    .idToken(codeToTokenFlow.getIdToken())
                    .build();

        } catch (HttpClientErrorException hcee) {
            log.error(hcee.getLocalizedMessage());
            throw TokenGeneratorException.builder()
                    .message("Error fetching token in standard flow")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MultiValueMap<String, String> buildBodyForCodeExchange(KeycloakTokenRequest keycloakTokenRequest,
                                                                   ClientRepresentation clientRepresentation,
                                                                   String code) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("client_id", keycloakTokenRequest.getClientId());
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", clientRepresentation.getRedirectUris().get(0));
        map.add("scope", keycloakTokenRequest.getScope());
        return map;
    }

    private MultiValueMap<String, String> buildQueryParamsForAuthCall(ClientRepresentation clientRepresentation,
                                                                      String scope) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("response_type", URLEncoder.encode("code", StandardCharsets.UTF_8));
        if (!CollectionUtils.isEmpty(clientRepresentation.getRedirectUris())) {
            map.add("redirect_uri", URLEncoder.encode(clientRepresentation.getRedirectUris().get(0), StandardCharsets.UTF_8));
        }
        map.add("nonce", "nonce");
        map.add("scope", URLEncoder.encode(scope, StandardCharsets.UTF_8));
        map.add("client_id", clientRepresentation.getClientId());
        return map;
    }
}
