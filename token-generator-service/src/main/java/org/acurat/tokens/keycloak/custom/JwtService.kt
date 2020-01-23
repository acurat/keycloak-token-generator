package org.acurat.tokens.keycloak.custom

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

@Service
class JwtService {
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
    
    fun mintJwt(jwtClaims: JwtClaims): String {
    
        val jwtClaimsSet = JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(jwtClaims.audience)
                .issueTime(Date())
                .notBeforeTime(Date())
                .expirationTime(Date.from(jwtClaims.expirationTime))
                .issuer(jwtClaims.issuer)
                .subject(jwtClaims.subject)
                .claim("azp", jwtClaims.azp)
    
        jwtClaims.additional.forEach{ entry ->
            jwtClaimsSet.claim(entry.key, entry.value)
        }
        
        val signedJWT = SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(rsaKey.keyID).build(),
                jwtClaimsSet.build())
    
        signedJWT.sign(signer)
        
        return signedJWT.serialize()
    }
    
}