# Github API Proxy

## Description
A Spring Boot application that fetches GitHub repositories for a given user (excluding forks) and lists branches with the latest commit SHA.

## Requirements
- Java 25
- Spring Boot 4.0
- Maven

## How to Run
1. Clone the repository
2. Run:
   mvn clean install
   mvn spring-boot:run
3. Access API: GET /users/{username}/repositories

## Features
- Lists GitHub repositories (non-forks)
- Lists branches with latest commit SHA
- Returns 404 for non-existing users in JSON format

## Testing
- Integration tests use WireMock
- Run tests:
  mvn test
