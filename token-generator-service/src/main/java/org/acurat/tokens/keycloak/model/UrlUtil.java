package org.acurat.tokens.keycloak.model;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UrlUtil {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String ID_TOKEN = "id_token";
    private static final List<String> tokenTypes = Arrays.asList(ACCESS_TOKEN, ID_TOKEN);

    private static String getFragment(String uri) {
        return UriComponentsBuilder.fromUriString(uri).build().getFragment();
    }

    private static MultiValueMap<String, String> getQueryParams(String uri) {
        return UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
    }

    public static TokenResponse getTokensFromLocationHeader(String location) {
        String[] fragments = getFragment(location).split("&");
        Map<String, String> tokens = Arrays.stream(fragments)
                .map(f -> f.split("="))
                .filter(f -> f.length == 2 && tokenTypes.contains(f[0]))
                .collect(Collectors.toMap(f -> f[0], f -> f[1]));

        return TokenResponse.builder()
                .accessToken(tokens.get(ACCESS_TOKEN))
                .idToken(tokens.get(ID_TOKEN))
                .build();
    }

    public static String getCodeFromLocationHeader(String location) {
        MultiValueMap<String, String> queryParams = getQueryParams(location);
        return queryParams.getFirst("code");
    }
}
