package org.acurat.tokens.keycloak.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WellKnownConfig {

    public WellKnownConfig(List<String> scopes_supported) {
        this.scopes_supported = scopes_supported;
    }

    public WellKnownConfig() {
    }

    private List<String> scopes_supported;
}
