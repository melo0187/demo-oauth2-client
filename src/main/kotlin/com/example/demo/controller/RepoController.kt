package com.example.demo.controller

import com.example.demo.services.GitHubApiService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/repos")
class RepoController(
    private val githubApiService: GitHubApiService
) {
    @GetMapping()
    suspend fun listRepos(): List<Map<String, Any>> {
        return githubApiService.listUserRepos()
    }
}
