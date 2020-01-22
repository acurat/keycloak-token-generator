package org.acurat.tokens.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardFlowTokenService implements TokenService {

    private final RestTemplate restTemplate;

    @Override
    public TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest,
                                  KeycloakProperties keycloakProperties,
                                  ClientRepresentation clientRepresentation,
                                  String impersonatorToken) {

        HttpHeaders httpHeaders = KeycloakUtil.getHttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + impersonatorToken);
        try {
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(httpHeaders);
            String userSearchUrl = keycloakProperties.getAdminUrl() + "/users?username=" +
                    keycloakTokenRequest.getUsername();
            ResponseEntity<List<UserRepresentation>> queryUsers = restTemplate.exchange(userSearchUrl,
                    HttpMethod.GET, entity, new ParameterizedTypeReference<List<UserRepresentation>>() {
                    });
            List<UserRepresentation> users = queryUsers.getBody();

            if (users == null || users.size() != 1) {
                throw TokenGeneratorException.builder()
                        .message("More than one user found. Enter exact username")
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build();
            }

            String userId = users.get(0).getId();
            String impersonationUrl = keycloakProperties.getAdminUrl() + "/users/" + userId + "/impersonation";
            ResponseEntity<Object> impersonate = restTemplate.exchange(impersonationUrl,
                    HttpMethod.POST, entity, Object.class);


            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(keycloakProperties.getAuthUrl())
                    .queryParams(buildQueryParamsForAuthCall(clientRepresentation, keycloakTokenRequest.getScope()));
            ResponseEntity<Object> standardCodeFlow = restTemplate.exchange(builder.build(true).toUri(),
                    HttpMethod.GET,
                    entity, Object.class);

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
        map.add("redirect_uri", URLEncoder.encode(clientRepresentation.getRedirectUris().get(0), StandardCharsets.UTF_8));
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
