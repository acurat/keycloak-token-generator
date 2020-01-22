package org.acurat.tokens.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KeycloakTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
