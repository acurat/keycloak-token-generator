package org.acurat.tokens.keycloak.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.KeycloakTokenRequest;
import org.acurat.tokens.keycloak.model.TokenResponse;
import org.acurat.tokens.keycloak.service.TokenGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenGenerator tokenGenerator;

    @GetMapping
    public TokenResponse generate(@RequestParam("environment") String environment,
                                  @RequestParam("clientId") String clientId,
                                  @RequestParam("username") String username,
                                  @RequestParam("scope") String scope) {
        log.info("Requested for - Environment {}, clientId {}, username {}, scope ({}) -",
                environment, clientId, username, scope);

        return tokenGenerator.generate(KeycloakTokenRequest
                .builder()
                .environment(environment)
                .clientId(clientId)
                .username(username)
                .scope(scope)
                .build()
        );

    }

}
