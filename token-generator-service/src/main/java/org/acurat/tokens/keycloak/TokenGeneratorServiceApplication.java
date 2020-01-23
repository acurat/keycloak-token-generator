package org.acurat.tokens.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.KeycloakProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.Banner.Mode.OFF;

@Slf4j
@SpringBootApplication
public class TokenGeneratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TokenGeneratorServiceApplication.class);
        application.setBannerMode(OFF);
        application.run(args);
    }

    @Bean
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Bean
    @ConfigurationProperties(prefix = "keycloak.environments")
    public Map<String, KeycloakProperties> keycloakPropertiesMap() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "keycloak.clients.exclude")
    public List<String> excludedClients() {
        return new ArrayList<>();
    }

}