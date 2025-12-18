package com.atipera.github_api;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubService {
    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<Map<String, Object>> getUserRepositories(String username) {

        return githubClient.getRepositories(username).stream()
                .filter(repo -> !repo.fork)
                .map(repo -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("repositoryName", repo.name);
                    result.put("ownerLogin", repo.owner.login);

                    List<Map<String, String>> branches =
                             githubClient.getBranches(repo.owner.login, repo.name)
                                    .stream()
                                    .map(branch -> Map.of(
                                            "name", branch.name,
                                            "lastCommitSha", branch.commit.sha
                                    ))
                                    .toList();

                    result.put("branches", branches);
                    return result;
                })
                .toList();
    }
}
