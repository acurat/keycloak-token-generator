package org.acurat.tokens.keycloak.custom

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import org.springframework.stereotype.Service
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.Date
import java.util.UUID
import java.util.stream.Collectors

val claimList = listOf("aud", "exp", "nbf", "iss", "sub", "azp", "iat", "jti")

@Service
class JwtService(private val objectMapper: ObjectMapper) {
    private var rsaKey: RSAKey
    private val signer: RSASSASigner
    private val publicJwkSet: JWKSet
    private val publicKeyPem: String
    
    init {
        
        val path: Path = Paths.get(ClassLoader
                .getSystemResource("jwkset.json").toURI())
        
        val lines = Files.lines(path)
        val data = lines.collect(Collectors.joining("\n"))
        lines.close()
        
        rsaKey = RSAKey.parse(data)
        
        signer = RSASSASigner(rsaKey)
        
        publicJwkSet = JWKSet(rsaKey.toPublicJWK())
        
        val publicKey = rsaKey.toPublicKey()
        val stringWriter = StringWriter()
        val pemWriter = PemWriter(stringWriter)
        val pem = PemObject("RSA PUBLIC KEY", publicKey.encoded)
        pemWriter.writeObject(pem)
        pemWriter.close()
        
        publicKeyPem = stringWriter.toString()
    }
    
    
    fun getKeys() = mapOf<String, Any>(
            "jwk" to publicJwkSet.toJSONObject(),
            "pem" to publicKeyPem
    )
    
    fun mintJwt(jwtClaims: Map<String, Any>): String {
        
        val jwtClaimsSet = JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
        jwtClaimsSet.issueTime(Date())
        jwtClaimsSet.notBeforeTime(Date())
        
        if (jwtClaims.containsKey("aud") && jwtClaims["aud"] is String)
            jwtClaimsSet.audience(jwtClaims["aud"].toString())
        
        if (jwtClaims.containsKey("exp") && jwtClaims["exp"] is Long)
            jwtClaimsSet.expirationTime(Date.from(Instant.ofEpochMilli(jwtClaims["exp"].toString().toLong())))
        
        if (jwtClaims.containsKey("iss") && jwtClaims["iss"] is String)
            jwtClaimsSet.issuer(jwtClaims["iss"].toString())
        
        if (jwtClaims.containsKey("sub") && jwtClaims["sub"] is String)
            jwtClaimsSet.subject(jwtClaims["sub"].toString())
        
        if (jwtClaims.containsKey("azp") && jwtClaims["azp"] is String)
            jwtClaimsSet.claim("azp", jwtClaims["azp"])
        
        jwtClaims.filterKeys { key -> !claimList.contains(key) }.forEach { entry ->
            if (entry.value is String) {
                try {
                    val value = jacksonObjectMapper().readValue(entry.value.toString(), Any::class.java)
                    jwtClaimsSet.claim(entry.key, value)
                } catch (e: Exception) {
                    jwtClaimsSet.claim(entry.key, entry.value)
                }
            } else jwtClaimsSet.claim(entry.key, entry.value)
        }
        
        val signedJWT = SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(rsaKey.keyID).build(),
                jwtClaimsSet.build())
        
        signedJWT.sign(signer)
        
        return signedJWT.serialize()
    }
    
}