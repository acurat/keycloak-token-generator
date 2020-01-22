package org.acurat.tokens.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class ImplicitFlowTokenService implements TokenService {

    private final RestTemplate restTemplate;

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
            ResponseEntity<Object> implicitFlow = restTemplate.exchange(builder.build(true).toUri(),
                    HttpMethod.GET,
                    entity, Object.class);

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
