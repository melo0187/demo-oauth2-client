package com.example.demo.controller

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController(
    private val authorizedClientService: ReactiveOAuth2AuthorizedClientService
) {

    @GetMapping("/")
    suspend fun home(model: Model, authentication: Authentication): String {
        val connected = isConnected("github", authentication)
        model.addAttribute("connected", connected)
        model.addAttribute("username", authentication.name)
        return "index"
    }

    private suspend fun isConnected(registrationId: String, authentication: Authentication): Boolean {
        return authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(registrationId, authentication.name)
            .awaitSingleOrNull() != null
    }
}
