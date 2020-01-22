package org.acurat.tokens.keycloak.custom

import org.acurat.tokens.keycloak.service.KeycloakService
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomTokenController(val keycloakService: KeycloakService) {


}