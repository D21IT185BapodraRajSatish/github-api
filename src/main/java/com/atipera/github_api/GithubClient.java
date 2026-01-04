package com.atipera.github_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class GithubClient {

    @Value("${github.api.base-url:https://api.github.com}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    GithubClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    List<Repository> getRepositories(String username) {
        String url = baseUrl+"/users/{username}/repos";
        try {
            Repository[] response =
                    restTemplate.getForObject(url, Repository[].class, username);
            if (response == null) {
                return List.of();
            }
            return Arrays.stream(response).toList();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UserNotFoundException("GitHub user not found");
        }
    }

    List<Branch> getBranches(String owner, String repo) {
        String url = baseUrl+"/repos/{owner}/{repo}/branches";
        Branch[] response =
                restTemplate.getForObject(url, Branch[].class, owner, repo);
        if (response == null) {
            return List.of();
        }
        return Arrays.stream(response).toList();
    }
}
