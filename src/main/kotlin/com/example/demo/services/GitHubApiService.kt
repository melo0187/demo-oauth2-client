package com.example.demo.services

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class GitHubApiService(
    private val authorizedClientService: ReactiveOAuth2AuthorizedClientService
) {
    private val webClient = WebClient.builder()
        .baseUrl("https://api.github.com")
        .defaultHeader("Accept", "application/vnd.github.v3+json")
        .build()

    suspend fun listUserRepos(): List<Map<String, Any>> {
        val principal = AnonymousAuthenticationToken(
            "key",
            "my-encoded-authentication",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        val client = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>("github", principal.name)
            .awaitSingleOrNull() ?: throw IllegalStateException("No GitHub client authorized yet")

        return webClient.get()
            .uri("/user/repos")
            .headers { headers ->
                headers.setBearerAuth(client.accessToken.tokenValue)
            }
            .retrieve()
            .bodyToFlux(Map::class.java)
            .map { it as Map<String, Any> }
            .collectList()
            .awaitSingle()
    }
}
