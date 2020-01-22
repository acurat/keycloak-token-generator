package org.acurat.tokens.keycloak.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Data
@Builder
@ToString
public class TokenGeneratorException extends RuntimeException {
    private String message;
    private HttpStatus httpStatus;
}
