package org.acurat.tokens.keycloak.resource;

import lombok.RequiredArgsConstructor;
import org.acurat.tokens.keycloak.model.KeycloakProperties;
import org.acurat.tokens.keycloak.model.KeycloakUtil;
import org.acurat.tokens.keycloak.service.KeycloakService;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("ui")
public class UIController {

    private final Map<String, KeycloakProperties> keycloakPropertiesMap;

    private final KeycloakService keycloakService;

    @GetMapping("environments")
    public List<String> getEnvironments() {
        return KeycloakUtil.getEnvironments(keycloakPropertiesMap);
    }

    @GetMapping("clients")
    public Map<String, List<String>> getClients(
            @RequestParam(required = false, name = "refresh", defaultValue = "false") Boolean refresh
    ) {
        return KeycloakUtil
                .getEnvironments(keycloakPropertiesMap)
                .stream().collect(
                        Collectors.toMap(Function.identity(),
                                env -> {
                                    List<ClientRepresentation> clientsForEnvironment =
                                            keycloakService.getClientsForEnvironment(env, refresh);
                                    return clientsForEnvironment.stream()
                                            .map(ClientRepresentation::getClientId).collect(Collectors.toList());
                                }));
    }

    @GetMapping("scopes")
    public Map<String, List<String>> getScopes(
            @RequestParam(required = false, name = "refresh", defaultValue = "false") Boolean refresh
    ) {
        return KeycloakUtil
                .getEnvironments(keycloakPropertiesMap)
                .stream().collect(
                        Collectors.toMap(Function.identity(),
                                env -> keycloakService.getScopesForEnvironment(env, refresh)));
    }
}
