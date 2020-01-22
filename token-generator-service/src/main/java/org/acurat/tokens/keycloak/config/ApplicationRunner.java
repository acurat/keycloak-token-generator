package org.acurat.tokens.keycloak.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.KeycloakProperties;
import org.acurat.tokens.keycloak.model.KeycloakUtil;
import org.acurat.tokens.keycloak.service.KeycloakService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationRunner implements CommandLineRunner {

    private final Map<String, KeycloakProperties> keycloakPropertiesMap;
    private final KeycloakService keycloakService;

    @Override
    public void run(String... args) {

        log.info("Application started...Warming caches");

        KeycloakUtil.getEnvironments(keycloakPropertiesMap)
                .forEach(environment -> {
                    log.info("Loading environment..{}", environment);
                    keycloakService.getClientsForEnvironment(environment, true);
                    keycloakService.getScopesForEnvironment(environment, true);
                });

        log.info("Warm up complete");
    }
}
