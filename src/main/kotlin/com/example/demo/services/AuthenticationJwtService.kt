package com.example.demo.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class AuthenticationJwtService {
    private val algorithm = Algorithm.HMAC256("your-secret-key-change-in-production")
    private val jWTVerifier = JWT.require(algorithm).build()

    fun encodeAuthentication(authentication: Authentication): String {
        return JWT.create()
            .withSubject(authentication.name)
            .withClaim("authorities", authentication.authorities.map { it.authority })
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
            .sign(algorithm)
    }

    fun decodeAuthentication(token: String): Authentication {
        val jwt = jWTVerifier.verify(token)
        val authorities = jwt.getClaim("authorities").asList(String::class.java).map { SimpleGrantedAuthority(it) }
        return PreAuthenticatedAuthenticationToken(jwt.subject, null, authorities)
    }
}