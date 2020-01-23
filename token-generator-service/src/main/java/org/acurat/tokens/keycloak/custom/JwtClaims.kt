package org.acurat.tokens.keycloak.custom

import java.time.Instant

class JwtClaims {
    lateinit var audience: String
    lateinit var expirationTime: Instant
    lateinit var issuer: String
    lateinit var subject: String
    lateinit var azp: String
    var additional: Map<String, Any> = mapOf()
}