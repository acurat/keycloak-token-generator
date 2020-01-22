package org.acurat.tokens.keycloak.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeycloakTokenRequest {

    private String environment;
    private String clientId;
    private String username;
    private String scope;
}
