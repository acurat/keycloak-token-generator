package org.acurat.tokens.keycloak.service;

import org.acurat.tokens.keycloak.model.KeycloakProperties;
import org.acurat.tokens.keycloak.model.KeycloakTokenRequest;
import org.acurat.tokens.keycloak.model.TokenResponse;

public interface TokenExchangeService {

    TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest,
                           KeycloakProperties keycloakProperties,
                           String impersonatorToken);
}
