package org.acurat.tokens.keycloak.resource;

import lombok.extern.slf4j.Slf4j;
import org.acurat.tokens.keycloak.model.TokenGeneratorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestResponseExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler({TokenGeneratorException.class})
    public ResponseEntity<Object> handleAccessDeniedException(
            Exception ex) {
        log.error("Exception handler error --> {}", ex);
        TokenGeneratorException tge = (TokenGeneratorException) ex;
        HttpStatus httpStatus = tge.getHttpStatus() == null ? HttpStatus.BAD_REQUEST : tge.getHttpStatus();
        return new ResponseEntity<>(tge.getMessage(), httpStatus);
    }
}
