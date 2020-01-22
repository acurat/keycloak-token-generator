package org.acurat.tokens.keycloak.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String idToken;
}
