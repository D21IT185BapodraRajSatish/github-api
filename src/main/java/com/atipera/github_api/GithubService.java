package com.atipera.github_api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubService {
    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getUserRepositories(String username) {

        return githubClient.getRepositories(username).stream()
                .filter(repo -> !repo.fork())
                .map(repo -> {
                    List<BranchResponse> branches =
                            githubClient.getBranches(repo.owner().login(), repo.name())
                                    .stream()
                                    .map(branch -> new BranchResponse(branch.name(), branch.commit().sha()))
                                    .toList();

                    return new RepositoryResponse(repo.name(), repo.owner().login(), branches);
                })
                .toList();
    }
}
