package org.acurat.tokens.keycloak.service;

import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.KeycloakProperties;
import org.acurat.tokens.keycloak.model.KeycloakUtil;
import org.acurat.tokens.keycloak.model.TokenGeneratorException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
abstract class OAuth2FlowService {

    private RestTemplate restTemplate;

    public OAuth2FlowService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    void impersonate(KeycloakProperties keycloakProperties, String username, String impersonatorToken) {

        try {
            HttpHeaders httpHeaders = KeycloakUtil.getHttpHeaders();
            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + impersonatorToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(httpHeaders);
            String userSearchUrl = keycloakProperties.getAdminUrl() + "/users?username=" + username;
            ResponseEntity<List<UserRepresentation>> queryUsers = restTemplate.exchange(userSearchUrl,
                    HttpMethod.GET, entity, new ParameterizedTypeReference<List<UserRepresentation>>() {
                    });
            List<UserRepresentation> users = queryUsers.getBody();

            if (users == null || users.size() == 0) {
                throw new Exception("No users found");
            } else {
                UserRepresentation user = users.stream()
                        .filter(userRepresentation ->
                                username.equalsIgnoreCase(userRepresentation.getUsername()))
                        .findFirst()
                        .orElse(users.get(0));

                String userId = user.getId();
                String impersonationUrl = keycloakProperties.getAdminUrl() + "/users/" + userId + "/impersonation";
                restTemplate.exchange(impersonationUrl,
                        HttpMethod.POST, entity, Object.class);
            }

        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw TokenGeneratorException.builder()
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
