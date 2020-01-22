package org.acurat.tokens.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.config.CacheManager;
import org.acurat.tokens.keycloak.model.*;
import org.infinispan.Cache;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KeycloakService {

    private static final String CLIENT_URL_POSTFIX = "/clients";
    private static final String CLIENTS_CACHE_NAME = "clients";
    private static final String SCOPES_CACHE_NAME = "scopes";
    private static final int TIME_IN_CACHE_IN_SECONDS = 60 * 60 * 24;
    private final CacheManager cacheManager;
    private final RestTemplate restTemplate;
    private final Map<String, KeycloakProperties> keycloakPropertiesMap;
    private final List<String> excludedClients;
    private final ClientCredentialsService clientCredentialsService;

    public KeycloakService(CacheManager cacheManager,
                           RestTemplate restTemplate,
                           Map<String, KeycloakProperties> keycloakPropertiesMap,
                           List<String> excludedClients) {
        this.cacheManager = cacheManager;
        this.restTemplate = restTemplate;
        this.keycloakPropertiesMap = keycloakPropertiesMap;
        this.excludedClients = excludedClients;
        this.clientCredentialsService =
                new ClientCredentialsService(restTemplate, cacheManager, keycloakPropertiesMap);
    }

    public String getAccessToken(String environment) {
        return clientCredentialsService.getAccessToken(environment);
    }

    public void invalidateAccessToken(String environment) {
        clientCredentialsService.removeObjectFromCache(environment);
    }

    public Map<String, KeycloakProperties> getKeycloakPropertiesMap() {
        return keycloakPropertiesMap;
    }

    public List<ClientRepresentation> getClientsForEnvironment(String environment, Boolean refresh) {
        List<ClientRepresentation> listFromCache = lookupCache(CLIENTS_CACHE_NAME, environment);
        if (!CollectionUtils.isEmpty(listFromCache) && !refresh) {
            log.debug("Found clients for environment {} in cache", environment);
            return listFromCache;
        }
        return fetchClientsFromKeycloak(environment);
    }

    public List<String> getScopesForEnvironment(String environment, Boolean refresh) {
        List<String> listFromCache = lookupCache(SCOPES_CACHE_NAME, environment);
        if (!CollectionUtils.isEmpty(listFromCache) && !refresh) {
            log.debug("Found scopes for environment {} in cache", environment);
            return listFromCache;
        }
        return fetchRealmScopesFromKeycloak(environment);
    }

    public ClientRepresentation getClientForEnvironment(String environment, String clientId) {

        List<ClientRepresentation> clientRepresentations = lookupCache(CLIENTS_CACHE_NAME, environment);
        if (CollectionUtils.isEmpty(clientRepresentations)) {
            clientRepresentations = fetchClientsFromKeycloak(environment);
        }
        return clientRepresentations.stream().filter(clientRepresentation ->
                clientId.equalsIgnoreCase(clientRepresentation.getClientId()))
                .findFirst()
                .orElseThrow(() -> TokenGeneratorException.builder().build());
    }

    public boolean isClientIdValid(String environment, String clientId) {
        List<ClientRepresentation> clientList = lookupCache(CLIENTS_CACHE_NAME, environment);
        return clientId != null &&
                excludedClients.stream().noneMatch(clientId::equalsIgnoreCase) &&
                clientList != null &&
                clientList.stream().anyMatch(clientRepresentation ->
                        clientId.equalsIgnoreCase(clientRepresentation.getClientId()));
    }

    private List<String> fetchRealmScopesFromKeycloak(String environment) {

        KeycloakProperties properties = KeycloakUtil.getEnvironmentProperties(environment, keycloakPropertiesMap);

        ResponseEntity<WellKnownConfig> wellKnownConfigResponseEntity =
                restTemplate.exchange(properties.getWellKnownConfigUrl(),
                        HttpMethod.GET, HttpEntity.EMPTY,
                        WellKnownConfig.class);
        log.debug("Well known data fetched");

        WellKnownConfig body = wellKnownConfigResponseEntity.getBody();
        List<String> scopes = new ArrayList<>();
        if (body != null && body.getScopes_supported() != null) {
            if (body.getScopes_supported().size() > 0) {
                scopes = wellKnownConfigResponseEntity.getBody().getScopes_supported();
            }
        }
        putInCache(SCOPES_CACHE_NAME, environment, scopes);
        return scopes;
    }

    private List<ClientRepresentation> fetchClientsFromKeycloak(String environment) {

        String accessToken = getAccessToken(environment);
        KeycloakProperties properties = KeycloakUtil.getEnvironmentProperties(environment, keycloakPropertiesMap);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        var httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<List<ClientRepresentation>> clientsResponseEntity =
                restTemplate.exchange(properties.getAdminUrl() + CLIENT_URL_POSTFIX,
                        HttpMethod.GET, httpEntity,
                        new ParameterizedTypeReference<List<ClientRepresentation>>() {
                        });

        log.debug("Client list fetched");
        var filteredClients = clientsResponseEntity.getBody().stream()
                .filter(ClientRepresentation::isPublicClient)
                .filter(clientRepresentation -> !(!properties.isTokenExchangeEnabled() && clientRepresentation.getRedirectUris().size() == 0))
                .filter(clientRepresentation -> !excludedClients.contains(clientRepresentation.getClientId()))
                .sorted(Comparator.comparing(ClientRepresentation::getClientId))
                .collect(Collectors.toList());

        putInCache(CLIENTS_CACHE_NAME, environment, filteredClients);
        return filteredClients;
    }

    private <T> List<T> lookupCache(String cacheName, String environment) {
        Cache<String, List<T>> cache = cacheManager.getCache(cacheName);
        return cache.get(environment);
    }

    private <T> void putInCache(String cacheName, String environment, List<T> data) {
        Cache<String, List<T>> cache = cacheManager.getCache(cacheName);
        cache.put(environment, data, TIME_IN_CACHE_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Slf4j
    @RequiredArgsConstructor
    static class ClientCredentialsService {

        private static final String CACHE_NAME = "client_credentials";
        private static final int TIME_IN_CACHE_IN_SECONDS = 250;
        private final RestTemplate restTemplate;
        private final CacheManager cacheManager;
        private final Map<String, KeycloakProperties> keycloakPropertiesMap;

        protected String getAccessToken(String environment) {
            String tokenFromCache = lookupCache(environment);
            if (!StringUtils.isEmpty(tokenFromCache)) {
                log.debug("Found token for environment {} in cache", environment);
                return tokenFromCache;
            }
            KeycloakProperties properties = KeycloakUtil.getEnvironmentProperties(environment, keycloakPropertiesMap);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", properties.getClientId());
            map.add("client_secret", properties.getClientSecret());

            HttpEntity<MultiValueMap<String, String>> entity =
                    new HttpEntity<>(map, KeycloakUtil.getHttpHeaders());

            ResponseEntity<KeycloakTokenResponse> exchange = restTemplate.exchange(properties.getTokenUrl(),
                    HttpMethod.POST, entity, KeycloakTokenResponse.class);
            KeycloakTokenResponse response = exchange.getBody();
            log.debug("Client credentials generated");
            putObjectInCache(environment, response.getAccessToken());
            return response.getAccessToken();
        }

        public void removeObjectFromCache(String environment) {
            Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);
            cache.remove(environment);
        }

        private String lookupCache(String environment) {
            Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);
            return cache.get(environment);
        }

        private void putObjectInCache(String environment, String token) {
            Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);
            cache.put(environment, token, TIME_IN_CACHE_IN_SECONDS, TimeUnit.SECONDS);
        }

    }


}
