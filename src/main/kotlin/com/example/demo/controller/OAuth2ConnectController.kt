package com.example.demo.controller

import com.example.demo.services.AuthenticationJwtService
import com.example.demo.services.OAuth2ConnectionService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.net.URI

@Controller
@RequestMapping("/oauth2")
class OAuth2ConnectController(
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val connectionService: OAuth2ConnectionService,
    private val authenticationJwtService: AuthenticationJwtService
) {
    private val resolver: ServerOAuth2AuthorizationRequestResolver =
        DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository)

    @GetMapping("/authorize/{registrationId}")
    suspend fun authorize(
        @PathVariable registrationId: String,
        authentication: Authentication,
        exchange: ServerWebExchange,
    ) {
        val authRequest = resolver.resolve(exchange, registrationId).awaitSingleOrNull()
            ?: return

        val encodedAuth = authenticationJwtService.encodeAuthentication(authentication)
        val customizedAuthRequest = OAuth2AuthorizationRequest.from(authRequest)
            .state(encodedAuth)
            .build()

        exchange.response.statusCode = HttpStatus.FOUND
        exchange.response.headers.location = URI.create(customizedAuthRequest.authorizationRequestUri)
        exchange.response.setComplete().awaitSingleOrNull()
    }

    @GetMapping("/callback/{registrationId}")
    @ResponseBody
    suspend fun callback(
        @PathVariable registrationId: String,
        @RequestParam code: String,
        @RequestParam state: String,
        exchange: ServerWebExchange,
    ) {
        connectionService.exchangeCodeForToken(registrationId, code, state, exchange)
        exchange.response.statusCode = HttpStatus.FOUND
        exchange.response.headers.location = URI.create("/repos")
        exchange.response.setComplete().awaitSingleOrNull()
    }
}
