package org.acurat.tokens.keycloak.model;

import lombok.Data;
import lombok.Getter;

@Data
public class KeycloakProperties {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    @Getter
    private boolean active;
    @Getter
    private boolean tokenExchangeEnabled;

    public String getAdminUrl() {
        return serverUrl + "/admin/realms/" + realm;
    }

    public String getTokenUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public String getAuthUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/auth";
    }

    public String getWellKnownConfigUrl() {
        return serverUrl + "/realms/" + realm + "/.well-known/openid-configuration";
    }

}
