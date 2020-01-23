package org.acurat.tokens.keycloak.custom

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("jwt")
class JwtController(private val jwtService: JwtService) {

    @GetMapping("keys")
    fun getKeys() = jwtService.getKeys()
    
    @PostMapping
    fun createJWT(@RequestBody jwtClaims: JwtClaims) =
            jwtService.mintJwt(jwtClaims)

}