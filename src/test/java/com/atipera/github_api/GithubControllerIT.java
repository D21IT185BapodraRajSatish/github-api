package com.atipera.github_api;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 0)
public class GithubControllerIT {
    @LocalServerPort
    int port;

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

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/users/raj/repositories",
                        String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("repo-1");
        assertThat(response.getBody()).contains("main");
        assertThat(response.getBody()).contains("abc123");
        assertThat(response.getBody()).doesNotContain("repo-fork");
    }

    @Test
    void shouldReturn404ForNonExistingUser() {

        stubFor(get(urlEqualTo("/users/ghost/repos"))
                .willReturn(notFound()));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/users/ghost/repositories",
                        String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).contains("\"status\":404");
    }
}
