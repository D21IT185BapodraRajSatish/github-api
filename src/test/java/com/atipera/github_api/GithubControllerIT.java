package com.atipera.github_api;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 0)
public class GithubControllerIT {
    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnRepositoriesForExistingUser() {
        stubFor(get(urlEqualTo("/users/raj/repos"))
                .willReturn(okJson("""
                [
                  {
                    "name": "repo-1",
                    "fork": false,
                    "owner": { "login": "raj" }
                  },
                  {
                    "name": "repo-fork",
                    "fork": true,
                    "owner": { "login": "raj" }
                  }
                ]
            """)));

        stubFor(get(urlEqualTo("/repos/raj/repo-1/branches"))
                .willReturn(okJson("""
                [
                  {
                    "name": "main",
                    "commit": { "sha": "abc123" }
                  }
                ]
            """)));

        ResponseEntity<List<RepositoryResponse>> response = restTemplate.exchange(
                "http://localhost:" + port + "/users/raj/repositories",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<RepositoryResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).anyMatch(r -> r.repositoryName().equals("repo-1"));
        assertThat(body.stream().flatMap(r -> r.branches().stream()).anyMatch(b -> b.name().equals("main"))).isTrue();
        assertThat(body.stream().flatMap(r -> r.branches().stream()).anyMatch(b -> b.lastCommitSha().equals("abc123"))).isTrue();
        assertThat(body.stream().noneMatch(r -> r.repositoryName().equals("repo-fork"))).isTrue();
    }

    @Test
    void shouldReturnEmptyListForUserWithNoRepos() {
        stubFor(get(urlEqualTo("/users/empty/repos"))
                .willReturn(okJson("[]")));

        ResponseEntity<List<RepositoryResponse>> response = restTemplate.exchange(
                "http://localhost:" + port + "/users/empty/repositories",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<RepositoryResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).isEmpty();
    }

    @Test
    void shouldReturn404ForNonExistingUser() {

        stubFor(get(urlEqualTo("/users/ghost/repos"))
                .willReturn(notFound()));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/users/ghost/repositories",
                String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).contains("\"status\":404");
    }
}
