package org.acurat.tokens.keycloak.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KeycloakInfo {
    List<String> clients;
    List<String> scopes;
}
