package com.example.demo.controller

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
    suspend fun home(model: Model): String {
        val connected = isConnected("github")
        model.addAttribute("connected", connected)
        return "index"
    }

    private suspend fun isConnected(registrationId: String): Boolean {
        val principal = AnonymousAuthenticationToken(
            "key",
            "my-encoded-authentication",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        return authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(registrationId, principal.name)
            .awaitSingleOrNull() != null
    }
}
