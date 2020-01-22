package org.acurat.tokens.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenGenerator {

    private final TokenExchangeService tokenExchangeService;

    private final ImplicitFlowTokenService implicitFlowTokenService;

    private final StandardFlowTokenService standardFlowTokenService;

    private final KeycloakService keycloakService;

    public TokenResponse generate(KeycloakTokenRequest keycloakTokenRequest) {

        if (!keycloakService.isClientIdValid(keycloakTokenRequest.getEnvironment(), keycloakTokenRequest.getClientId())) {
            throw TokenGeneratorException.builder()
                    .message("Client not valid")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        KeycloakProperties keycloakProperties =
                KeycloakUtil.getEnvironmentProperties(keycloakTokenRequest.getEnvironment(),
                        keycloakService.getKeycloakPropertiesMap());

        String impersonatorToken = keycloakService.getAccessToken(keycloakTokenRequest.getEnvironment());

        TokenResponse tokenResponse;
        if (keycloakProperties.isTokenExchangeEnabled()) {
            tokenResponse = tokenExchangeService.generate(keycloakTokenRequest,
                    keycloakProperties,
                    impersonatorToken);
        } else {

            try {

                ClientRepresentation client = keycloakService
                        .getClientForEnvironment(keycloakTokenRequest.getEnvironment(), keycloakTokenRequest.getClientId());

                if (client.isImplicitFlowEnabled()) {
                    tokenResponse = implicitFlowTokenService.generate(keycloakTokenRequest,
                            keycloakProperties, client, impersonatorToken);
                } else {
                    tokenResponse = standardFlowTokenService.generate(keycloakTokenRequest,
                            keycloakProperties, client, impersonatorToken);
                }
            } finally {
                // Impersonator token is invalidated in oidc flows, clear from cache
                keycloakService.invalidateAccessToken(keycloakTokenRequest.getEnvironment());
            }


        }

        return tokenResponse;
    }


}
