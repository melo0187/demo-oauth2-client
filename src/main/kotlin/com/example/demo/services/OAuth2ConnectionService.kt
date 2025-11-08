package com.example.demo.services

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.MediaType
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import kotlin.io.encoding.Base64

@Service
class OAuth2ConnectionService(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val authorizedClientService: ReactiveOAuth2AuthorizedClientService,
    private val webClientBuilder: WebClient.Builder
) {
    suspend fun exchangeCodeForToken(
        registrationId: String,
        code: String,
        state: String,
        exchange: ServerWebExchange
    ): OAuth2AuthorizedClient {
        val principal = AnonymousAuthenticationToken(
            "key",
            Base64.UrlSafe.decode(state).decodeToString(),
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        val registration = clientRegistrationRepository.findByRegistrationId(registrationId).awaitSingleOrNull()
            ?: throw IllegalArgumentException("Unknown registrationId: $registrationId")

        val tokenResponse = requestAccessToken(registration, code, exchange)
        val accessTokenValue = tokenResponse["access_token"] as String
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusSeconds(
            (tokenResponse["expires_in"] as? Number)?.toLong() ?: 3600
        )

        val accessToken = OAuth2AccessToken(TokenType.BEARER, accessTokenValue, issuedAt, expiresAt)
        val refreshTokenValue = tokenResponse["refresh_token"] as? String
        val refreshToken = refreshTokenValue?.let { OAuth2RefreshToken(it, Instant.now()) }

        val authorizedClient = OAuth2AuthorizedClient(
            registration,
            principal.name,
            accessToken,
            refreshToken
        )

        authorizedClientService.saveAuthorizedClient(authorizedClient, principal).awaitSingleOrNull()
        return authorizedClient
    }

    private suspend fun requestAccessToken(
        registration: ClientRegistration,
        code: String,
        exchange: ServerWebExchange
    ): Map<String, Any> {
        val baseUrl = UriComponentsBuilder.fromUri(exchange.request.uri)
            .replacePath(null)
            .replaceQuery(null)
            .build().toUriString()

        val registrationId = registration.registrationId

        val redirectUri = UriComponentsBuilder.fromUriString(registration.redirectUri)
            .buildAndExpand(baseUrl, registrationId)
            .toUriString()

        val form = BodyInserters.fromFormData("grant_type", "authorization_code")
            .with("code", code)
            .with("redirect_uri", redirectUri)
            .with("client_id", registration.clientId)
            .with("client_secret", registration.clientSecret)

        return webClientBuilder.clone().build().post()
            .uri(registration.providerDetails.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(form)
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { it as Map<String, Any> }
            .awaitSingle()
    }
}
