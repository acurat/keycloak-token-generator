package org.acurat.tokens.keycloak.service;

import org.acurat.tokens.keycloak.model.KeycloakProperties;
import org.acurat.tokens.keycloak.model.KeycloakTokenRequest;
import org.acurat.tokens.keycloak.model.TokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;

public interface TokenService {

    TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest,
                           KeycloakProperties keycloakProperties,
                           ClientRepresentation clientRepresentation,
                           String impersonatorToken);
}
