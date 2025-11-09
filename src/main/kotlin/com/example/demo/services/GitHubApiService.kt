package com.example.demo.services

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class GitHubApiService(
    private val authorizedClientManager: ReactiveOAuth2AuthorizedClientManager
) {
    private val oauth2Filter = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    
    init {
        oauth2Filter.setDefaultClientRegistrationId("github")
    }
    
    private val webClient = WebClient.builder()
        .baseUrl("https://api.github.com")
        .defaultHeader("Accept", "application/vnd.github.v3+json")
        .filter(oauth2Filter)
        .build()

    suspend fun listUserRepos(): List<Map<String, Any>> {
        return webClient.get()
            .uri("/user/repos")
            /**
             * You'd need the explicit attributes approach when:
             * - Different security context: Using the WebClient outside a web request
             * - Service-to-service calls: Where there's no HTTP security context
             * - Different user context: When you want to use a different user's OAuth2 client than the current authenticated user
             */
            //.attributes { attrs ->
            //    attrs["org.springframework.security.authentication.Authentication"] = authentication
            //}
            .retrieve()
            .bodyToFlux(Map::class.java)
            .map { it as Map<String, Any> }
            .collectList()
            .awaitSingle()
    }
}
